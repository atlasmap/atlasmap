/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import { ErrorInfo, ErrorLevel, ErrorScope, ErrorType } from './error.model';
import { MappedField, MappingModel } from './mapping.model';
import { Position, Token, editor } from 'monaco-editor';

import { CommonUtil } from '../utils/common-util';
import { ConfigModel } from './config.model';
import { IExpressionNode } from '../contracts/expression';

export abstract class ExpressionNode implements IExpressionNode {
  protected static sequence = 0;
  public readonly uuid: string;
  public position: Position;
  public str: string;

  constructor(prefix: string) {
    this.uuid = prefix + ExpressionNode.sequence++;
  }

  getUuid() {
    return this.uuid;
  }

  getPosition(): Position {
    return this.position;
  }

  abstract toText(): string;
  abstract toSimpleText(): string;
}

export class TextNode extends ExpressionNode {
  static readonly PREFIX = 'expression-text-';

  constructor(public str: string) {
    super(TextNode.PREFIX);
  }

  appendExprText(aStr: string) {
    this.str += aStr;
  }

  toText(): string {
    return this.str;
  }

  toSimpleText(): string {
    return this.str;
  }
}

export class FieldNode extends ExpressionNode {
  static readonly PREFIX = 'expression-field-';

  constructor(
    private mapping: MappingModel,
    public position: Position,
    public mappedField?: MappedField | null,
    public metaStr?: string,
    index: number = 0,
    public collectionContextPath?: string
  ) {
    super(FieldNode.PREFIX);
    if (!mappedField) {
      if (metaStr) {
        const fieldParts = metaStr.split(':');

        // Relative paths will not have the full field path in the meta data.
        if (fieldParts.length === 1) {
          if (collectionContextPath) {
            this.mappedField = mapping.getMappedFieldByPath(
              collectionContextPath + fieldParts[0],
              true
            )!;
          } else {
            this.mappedField = mapping.getMappedFieldByPath(
              fieldParts[0],
              true
            )!;
          }
        } else {
          this.mappedField = mapping.getMappedFieldByPath(
            fieldParts[1],
            true,
            fieldParts[0]
          )!;
        }
        if (!this.mappedField) {
          this.mappedField = mapping.getReferenceField(
            fieldParts[0],
            fieldParts[1]
          );
        }
      } else {
        this.mappedField = mapping.getMappedFieldForIndex(
          (index + 1)?.toString(),
          true
        )!;
      }
      mappedField = this.mappedField;
    }
  }

  /**
   * Return a JSON field reference name for the current mapped field.
   *
   * @returns
   */
  toText(): string {
    if (!this.mappedField || !this.mappedField.field) {
      return '';
    }
    if (this.mappedField.field.enumeration) {
      // Convert enumeration field/index pairs into a string literal.
      const enumIdxVal = this.mappedField.field.enumIndexValue
        ? this.mappedField.field.enumIndexValue
        : 0;
      return '"' + this.mappedField.field.enumValues[enumIdxVal].name + '"';
    } else {
      let textStr = '${';

      // If the mapped field's parent is a complex reference field then only use the leaf.
      if (
        this.mappedField.field.parentField &&
        this.mappedField.field.parentField ===
          this.mapping.referenceFields[0]?.field
      ) {
        textStr += '/' + this.mappedField.field.name + '}';
      } else {
        textStr +=
          this.mappedField.field.docDef.id +
          ':' +
          this.mappedField.field.path +
          '}';
      }
      return textStr;
    }
  }

  /**
   * Return the most simple name version string of the mapped field name.
   *
   * @returns
   */
  toSimpleText(): string {
    if (this.mappedField && this.mappedField.field) {
      let mappedFieldName = this.mappedField.field.name;
      if (this.mappedField.field.enumeration) {
        const enumIdxVal = this.mappedField.field.enumIndexValue
          ? this.mappedField.field.enumIndexValue
          : 0;
        mappedFieldName +=
          '.' + this.mappedField.field.enumValues[enumIdxVal].name;
      }
      return mappedFieldName;
    } else {
      return `'${
        this.mapping.getIndexForMappedField(this.mappedField!)! - 1
      }' is not available"`;
    }
  }

  hasComplexField(): boolean {
    return (
      this.mappedField?.field?.documentField.fieldType === 'COMPLEX' &&
      (this.mappedField?.field?.documentField.status === 'SUPPORTED' ||
        this.mappedField?.field?.documentField.status === 'CACHED')
    );
  }
}

export class ExpressionModel {
  simpleExpression = '';

  private _hasComplexField: boolean;
  private _nodes: ExpressionNode[] = [];
  private lineOffsets: number[] = [0];
  private textCache = '';

  constructor(private mapping: MappingModel, private cfg: ConfigModel) {}
  /**
   * Extract all identifiers out of a specified text string.  Adjust the corresponding
   * field node positions based on the tokenized identifier offset.
   *
   * @param text - user source expression (all lines)
   */
  private adjustFieldNodes(text: string) {
    // Extract identifiers from the raw source expression.
    const tokens = editor?.tokenize(text, this.cfg.atlasmapLanguageID);
    if (!tokens || tokens[0].length === 0) {
      return;
    }
    const lines = text.split('\n');
    let lineTokens: Token[] | undefined;
    let lineNumber = 1;
    let nodeIndex = 0;
    let i = 0;
    let idIndex = 0;
    this.lineOffsets = [0];

    // Support multiple lines.
    while ((lineTokens = tokens.shift()) !== undefined) {
      let idTokens = lineTokens?.filter(
        (t) => t.type === 'identifier'
      ) as Token[];

      // Adjust the expression field nodes based on the tokenized identifier line
      // number and offsets.
      for (i = 0; i < idTokens!.length; i++) {
        idIndex = nodeIndex + i + 1;
        if (idIndex >= this._nodes.length) {
          break;
        }
        this._nodes[idIndex].position = new Position(
          lineNumber,
          idTokens[i].offset + 1
        );
      }
      // Accumulated offset.
      this.lineOffsets.push(
        lines[lineNumber - 1].length + this.lineOffsets[lineNumber - 1] + 1
      );
      lineNumber++;
      nodeIndex += i;
    }
  }

  /**
   * Create a new field node derived from the specified mapped field and push it
   * onto the end of the master nodes array.  If the last field element is a field
   * node and has no trailing text then prepend a '+' to make a legal expression.
   *
   * @param mfield
   */
  private appendFieldNode(mfield: MappedField) {
    const textNode = this.getTextNode();
    const lastNode = this.getLastNode();
    if (lastNode instanceof FieldNode) {
      if (
        lastNode.position.column + lastNode.mappedField?.field?.name.length! >=
        textNode.str.length
      ) {
        textNode.str = textNode.str.concat(' + ');
      }
    }
    const position = new Position(1, textNode.str.length + 1);
    textNode.str += mfield.field?.name;
    this._nodes.push(new FieldNode(this.mapping, position, mfield));
    this.updateCache();
  }

  /**
   * Create an expression string containing fully expanded doc/path related field
   * references.
   *
   * @returns
   */
  private decorateExpressionIdentifiers(): string {
    let answer = this._nodes[0].toSimpleText();
    if (this._nodes.length === 1) {
      return answer;
    }
    this.adjustFieldNodes(answer);

    for (let i = this._nodes.length - 1; i > 0; i--) {
      let fn = this._nodes[i] as FieldNode;
      let nodePath = fn.toText();
      let nodePosition = fn.getPosition();
      answer = CommonUtil.replaceAt(
        answer,
        nodePath,
        this.lineOffsets[nodePosition.lineNumber - 1] + nodePosition.column - 1,
        fn.mappedField?.field?.name.length!
      );
    }
    this.textCache = answer;
    return answer;
  }

  private updateCache() {
    if (this._nodes.length > 0) {
      this.textCache = this.decorateExpressionIdentifiers();
      this.simpleExpression = this.getTextNode().toSimpleText(); // trigger expr box render
    }
  }

  generateInitialExpression() {
    this.insertText('');
    this.mapping
      .getUserMappedFields(true)
      .forEach((f) => this.appendFieldNode(f));
  }

  get hasComplexField(): boolean {
    return this._hasComplexField;
  }

  set hasComplexField(value: boolean) {
    this._hasComplexField = value;
  }

  get nodes(): ReadonlyArray<ExpressionNode> {
    return this._nodes;
  }

  getLastNodeIndex() {
    return this._nodes.length - 1;
  }

  getLastNode() {
    return this._nodes[this.getLastNodeIndex()];
  }

  getNode(nodeId?: string): any {
    if (!nodeId) {
      return this.getLastNode();
    }
    return this._nodes.find((n) => n.getUuid() === nodeId);
  }

  getTextNode() {
    return this._nodes[0];
  }

  findNode(position: Position): any | null {
    if (!position) {
      return this.getLastNode();
    }
    let fieldNode = null;
    for (let i = 1; i < this._nodes.length; i++) {
      let fnPosition = this._nodes[i].getPosition();
      if (
        fnPosition.lineNumber === position.lineNumber &&
        fnPosition.column === position.column
      ) {
        fieldNode = this._nodes[i];
        break;
      }
    }
    return fieldNode;
  }

  setConfigModel(cfg: ConfigModel) {
    this.cfg = cfg;
  }

  getFieldNodes(): FieldNode[] {
    return this._nodes.filter((n) => n instanceof FieldNode) as FieldNode[];
  }

  /**
   * Insert text into the expression at the specified position. If nodeId is not
   * specified, it will be added to the end of expression.
   *
   * @param newText - string to insert
   * @param jsonText - fully expanded JSON text string
   */
  async insertText(newText: string, jsonText?: boolean): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.removeExprTextNode();
      this.insertNodes(this.createNodesFromExpr(newText));
      if (!jsonText) {
        this.adjustFieldNodes(newText);
      }
      this.updateCache();
      resolve(true);
    });
  }

  /**
   * Insert an array of ExpressionNodes at the specified position. If insertPosition is
   * not specified the nodes will be appended to the end of the expression.
   *
   * @param newNodes - an array of ExpressionNode to add
   * @param insertPosition - line number/ offset to insert the nodes
   */
  insertNodes(newNodes: ExpressionNode[], insertPosition?: Position) {
    let textNode = this.getTextNode();

    // No position was specified - append to the end
    if (!insertPosition) {
      if (newNodes[0] instanceof TextNode) {
        this._nodes.splice(0, 0, ...newNodes);
      } else {
        const lastNode = this.getLastNode();
        textNode = this.getTextNode();

        if (lastNode instanceof FieldNode) {
          const fn = newNodes[0] as FieldNode;
          fn.position = new Position(
            fn.position.lineNumber,
            fn.position.column + 3
          );
          textNode.str = textNode.str.concat(' + ');
        }
        this._nodes.push(...newNodes);
      }
      return;
    }

    // Insert the field nodes in position order.
    let insertionIndex = 1;
    for (; insertionIndex <= this.getLastNodeIndex(); insertionIndex++) {
      let fn = this._nodes[insertionIndex];
      if (fn.position.lineNumber > insertPosition.lineNumber) {
        this._nodes.splice(insertionIndex, 0, ...newNodes);
        break;
      } else if (
        insertPosition.lineNumber === fn.position.lineNumber &&
        insertPosition.column <= fn.position.column
      ) {
        this._nodes.splice(insertionIndex, 0, ...newNodes);
        break;
      }
    }

    let adjustedPosition = insertPosition.column;

    // Insertion position indicates it's last - append the field nodes(s).
    if (insertionIndex === this._nodes.length) {
      const lastNode = this.getLastNode() as FieldNode;
      if (
        lastNode &&
        lastNode.position?.column + lastNode.mappedField?.field?.name.length! >=
          insertPosition.column
      ) {
        const fn = newNodes[0] as FieldNode;
        fn.position = new Position(
          fn.position.lineNumber,
          fn.position.column + 3
        );
        adjustedPosition += 3;
        textNode.str = CommonUtil.replaceAt(
          textNode.str,
          ' + ',
          insertPosition.column - 1,
          0
        );
      }
      this._nodes.push(...newNodes);
    }
    const fieldNodeName = (newNodes[0] as FieldNode).mappedField?.field?.name;
    textNode.str = CommonUtil.replaceAt(
      textNode.str,
      fieldNodeName!,
      this.lineOffsets[insertPosition.lineNumber - 1] + adjustedPosition - 1,
      0
    );
  }

  /**
   * Remove an expression node from the specified position.
   *
   * @param position
   */
  removeNodeAtPosition(position: Position) {
    for (let i = 1; i < this._nodes.length; i++) {
      if (
        this._nodes[i].position.lineNumber === position.lineNumber &&
        this._nodes[i].position.column === position.column
      ) {
        const removed = this._nodes.splice(i, 1);
        const targetFieldNode: FieldNode = removed[0] as FieldNode;
        if (
          !this._nodes.find(
            (n) =>
              n instanceof FieldNode &&
              n.mappedField === targetFieldNode.mappedField
          )
        ) {
          this.mapping.removeField(targetFieldNode.mappedField!.field!);
          this.cfg.mappingService.updateMappedField(this.mapping);
          break;
        }
      }
    }
  }

  /**
   * Remove the field node/ text node at the specified index.
   *
   * @param idIndex - if not supplied remove the last node
   *
   */
  async removeToken(idPosition?: Position): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      // No position was specified - remove from the end
      if (idPosition === undefined) {
        const last = this.getLastNode();
        if (!last) {
          resolve(false);
          return;
        }
        if (last instanceof FieldNode) {
          const removed = this._nodes.pop() as FieldNode;
          if (
            !this._nodes.find(
              (n) =>
                n instanceof FieldNode && n.mappedField === removed.mappedField
            )
          ) {
            this.mapping.removeField(removed.mappedField!.field!);
            this.cfg.mappingService.updateMappedField(this.mapping);
          }
        } else if (last instanceof TextNode) {
          if (last.str.length > 0) {
            last.str = last.str.substring(0, last.str.length - 1);
          }
          if (last.str.length === 0) {
            this._nodes.pop();
          }
        }
        resolve(true);
        return;
      }
      this.removeNodeAtPosition(idPosition);
      resolve(true);
    });
  }

  /**
   * Remove the expression text node at index 0.
   */
  removeExprTextNode() {
    if (this._nodes.length > 0) {
      this._nodes.splice(0, 1);
    }
  }

  /**
   * Reflect mapped source fields to the field references in the expression.
   * Selected source fields are inserted into or appended to the expression,
   * and unselected source fields are removed from expression.
   *
   * @param mapping Corresponding MappingModel object
   * @param insertPosition
   */
  updateFieldReference(mapping: MappingModel, insertPosition?: Position) {
    const mappedFields = mapping.getUserMappedFields(true);
    const referenceFields = mapping.getReferenceMappedFields();
    let fieldNodes = this._nodes.filter(
      (n) => n instanceof FieldNode
    ) as FieldNode[];

    // Remove non-reference fields from the expression if unmapped.
    for (const node of fieldNodes) {
      // TODO: check this non null operator
      if (
        mappedFields.includes(node.mappedField!) ||
        referenceFields.includes(node.mappedField!) ||
        node.hasComplexField()
      ) {
        continue;
      }
      const index = this._nodes.indexOf(node);
      this._nodes.splice(index, 1);
    }

    // Add any non-reference mapped fields into the expression - append if no insert
    // position is specified.
    fieldNodes = this.getFieldNodes();
    for (const mfield of mappedFields) {
      if (
        !fieldNodes.find((n) => n.mappedField === mfield) &&
        !referenceFields.find((r) => r.field === mfield.field)
      ) {
        if (insertPosition) {
          this.insertNodes(
            [new FieldNode(this.mapping, insertPosition, mfield)],
            insertPosition
          );
        } else {
          this.appendFieldNode(mfield);
        }
      }
    }
    this.updateCache();
  }

  clear() {
    this._nodes = [];
    this.lineOffsets = [0];
    this.updateCache();
  }

  toText() {
    if (this.textCache.length === 0) {
      this.updateCache();
    }
    return this.textCache;
  }

  addConditionalExpressionNode(
    mappedField: MappedField,
    insertPosition: Position
  ): void {
    this.insertNodes(
      [new FieldNode(this.mapping, insertPosition, mappedField)],
      insertPosition
    );
    this.updateCache();
  }

  /**
   * Establish a single text node and corresponding field nodes based on the specified
   * expression field syntax.  The specified expression text may be JSON extended format
   * or the simple user-defined text.
   *
   *   (i.e. ${DOC.Properties.578580:/JSONSchemaSource/prop-city} ).
   *
   * @param text - Raw JSON expression or simple text - can be multiple lines.
   * @returns - An expression node array based on the supplied text string.
   */
  createNodesFromExpr(text: string): ExpressionNode[] {
    const answer: ExpressionNode[] = [];
    let idPosition = -1;
    let collectionContextFieldNode = null;
    const lines = text.split('\n');
    let lineNumber = 1;
    let lineDelta = 0;
    let lineIndex = 0;
    let lineText = '';

    while (lineIndex < lines.length) {
      lineText = lines[lineIndex];

      while (lineText.search(/\$\{[a-zA-Z0-9.:/<>[\]_-]+\}/) !== -1) {
        idPosition = lineText.search(/\$/);
        const nodeMetaVal = lineText.substring(
          idPosition + 2,
          lineText.indexOf('}')
        );
        let fn = null;
        const fieldPosition: Position = new Position(
          lineNumber,
          idPosition + 1
        );

        if (isNaN(Number(nodeMetaVal))) {
          fn = collectionContextFieldNode
            ? new FieldNode(
                this.mapping,
                fieldPosition,
                undefined,
                nodeMetaVal,
                undefined,
                collectionContextFieldNode.mappedField?.field?.path!
              )
            : new FieldNode(
                this.mapping,
                fieldPosition,
                undefined,
                nodeMetaVal
              );
        } else {
          const index = parseInt(nodeMetaVal, 10);
          fn = new FieldNode(
            this.mapping,
            fieldPosition,
            undefined,
            undefined,
            index
          );
        }

        if (!fn || !fn.mappedField) {
          this.cfg.errorService.addError(
            new ErrorInfo({
              message: `Unable to map expression element '${nodeMetaVal}' to a field node.`,
              level: ErrorLevel.ERROR,
              scope: ErrorScope.MAPPING,
              type: ErrorType.INTERNAL,
              mapping: this.mapping,
            })
          );
          break;
        } else {
          if (fn.mappedField?.field?.isCollection) {
            collectionContextFieldNode = fn;
          }
          answer.push(fn);
        }
        const identifierNameSeg = nodeMetaVal.split('/');
        if (identifierNameSeg) {
          let replacementText = '';
          if (collectionContextFieldNode) {
            replacementText = fn.mappedField.field!.name;
          } else {
            replacementText = identifierNameSeg[identifierNameSeg.length - 1];
          }
          text = CommonUtil.replaceAt(
            text,
            replacementText,
            idPosition + lineDelta,
            nodeMetaVal.length + 3
          );
          lineText = CommonUtil.replaceAt(
            lineText,
            replacementText,
            idPosition,
            nodeMetaVal.length + 3
          );
        }
      }
      // Adjust the line delta - consider the newline character.
      lineDelta += lineText.length + 1;
      lineNumber++;
      lineIndex++;
    }
    answer.splice(0, 0, new TextNode(text));
    return answer;
  }
}
