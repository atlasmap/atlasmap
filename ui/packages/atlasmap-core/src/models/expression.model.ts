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

import { ConfigModel } from './config.model';
import { IExpressionNode } from '../contracts/expression';
import { Subject } from 'rxjs';

export class ExpressionUpdatedEvent {
  constructor(public node?: ExpressionNode, public offset?: number) {}
}

export abstract class ExpressionNode implements IExpressionNode {
  protected static sequence = 0;
  public readonly uuid: string;
  public readonly str: string;

  constructor(prefix: string) {
    this.uuid = prefix + ExpressionNode.sequence++;
  }

  getUuid() {
    return this.uuid;
  }

  abstract toText(): string;
  abstract toHTML(): string;
}

export class TextNode extends ExpressionNode {
  static readonly PREFIX = 'expression-text-';

  constructor(public str: string) {
    super(TextNode.PREFIX);
  }

  toText(): string {
    return this.str;
  }

  toHTML(): string {
    return `<span id="${this.uuid}">${this.str.replace(/ /g, '&nbsp;')}</span>`;
  }
}

export class FieldNode extends ExpressionNode {
  static readonly PREFIX = 'expression-field-';

  constructor(
    private mapping: MappingModel,
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

  toHTML(): string {
    if (this.mappedField && this.mappedField.field) {
      let titleAddendum = '';
      let mappedFieldName = this.mappedField.field.name;
      if (this.mappedField.field.enumeration) {
        const enumIdxVal = this.mappedField.field.enumIndexValue
          ? this.mappedField.field.enumIndexValue
          : 0;
        mappedFieldName +=
          '.' + this.mappedField.field.enumValues[enumIdxVal].name;
        titleAddendum = ':  Click to select an enumeration value.';
      }
      return `<span style="font-weight:bold" contenteditable="false" id="${this.uuid}" title="${this.mappedField.field.docDef.name}:${this.mappedField.field.path}${titleAddendum}"
        class="expressionFieldLabel label label-default">${mappedFieldName}</span>`;
    } else {
      // TODO: check this non null operator
      return `<span contenteditable="false" id="${this.uuid}"
        title="Field index '${
          this.mapping.getIndexForMappedField(this.mappedField!)! - 1
        }' is not available"
        class="expressionFieldLabel label label-danger">N/A</span>`;
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
  expressionHTML = '';
  expressionUpdatedSource = new Subject<ExpressionUpdatedEvent>();
  expressionUpdated$ = this.expressionUpdatedSource.asObservable();

  private _hasComplexField: boolean;
  private _nodes: ExpressionNode[] = [];
  private textCache = '';

  constructor(private mapping: MappingModel, private cfg: ConfigModel) {}

  generateInitialExpression() {
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

  setConfigModel(cfg: ConfigModel) {
    this.cfg = cfg;
  }

  /**
   * Clear all text from the specified TextNode offset range or from the '@' to
   * the end of the text node if no node ID is specified.
   *
   * Return the new UUID position indicator string or null.
   *
   * @param nodeId
   * @param startOffset
   * @param endOffset
   */
  clearText(
    nodeId?: string,
    startOffset?: number,
    endOffset?: number
  ): TextNode | null {
    let targetNode: TextNode | null = null;
    if (!nodeId) {
      const lastNode = this.getLastNode();
      if (!(lastNode instanceof TextNode)) {
        return null;
      }
      const keyPos = lastNode.str.indexOf('@');
      if (keyPos !== -1) {
        targetNode = lastNode;
        targetNode.str = targetNode.str.substring(0, keyPos);
      }
    } else {
      const node = this._nodes.find((n) => n.getUuid() === nodeId);
      if (!(node instanceof TextNode) || !endOffset) {
        return null;
      }
      targetNode = node;
      // TODO: check this non null operator
      const cleanStr = targetNode.str.replace(
        targetNode.str.substring(startOffset!, endOffset),
        ''
      );
      targetNode.str = cleanStr;
    }
    this.updateCache();
    this.expressionUpdatedSource.next();
    return targetNode;
  }

  /**
   * Insert text into expression at specified position. If nodeId is not specified,
   * it will be added to the end of expression. It parses the string
   * and insert a set of TextNode & FieldNode if it contains field reference like ${0},
   * otherwise just one TextNode.
   * This emits ExpressionUpdatedEvent which contains the latest node and offset it
   * worked on, so that the subscriber can determine where to put the caret in
   * the expression input widget. If ExpressionUpdatedEvent is undefined, it means that
   * it worked on the end of the expression.
   * @param str string to insert
   * @param nodeId target node to insert the string
   * @param offset position offset in the target node to insert the string
   */
  insertText(str: string, nodeId?: string, offset?: number) {
    this.insertNodes(this.createNodesFromText(str), nodeId, offset);
  }

  /**
   * Insert an array of ExpressionNodes at the specified position. If insertPosition is
   * not specified the nodes will be appended to the end of the expression.
   * This emits an ExpressionUpdatedEvent which contains the latest node and offset it
   * worked on, so that the subscriber can determine where to put the caret in
   * the expression input widget. If ExpressionUpdatedEvent is undefined, it means that
   * it worked on the end of the expression.
   *
   * @param newNodes an array of ExpressionNode to add
   * @param insertPosition target node to insert the string
   * @param offset position offset in the target node to insert the string
   */
  insertNodes(
    newNodes: ExpressionNode[],
    insertPosition?: string,
    offset?: number
  ) {
    // No position was specified - append to the end
    if (!insertPosition) {
      const last = this.getLastNode();
      if (!last) {
        this._nodes.push(...newNodes);
      } else if (last instanceof TextNode && newNodes[0] instanceof TextNode) {
        (last as TextNode).str += (newNodes[0] as TextNode).str;
        newNodes.splice(0, 1, last);
        this._nodes.splice(this.getLastNodeIndex(), 1, ...newNodes);
      } else if (
        last instanceof FieldNode &&
        newNodes[0] instanceof FieldNode
      ) {
        this._nodes.splice(
          this.getLastNodeIndex(),
          0,
          new TextNode(' + '),
          ...newNodes
        );
      } else {
        this._nodes.push(...newNodes);
      }
      this.updateCache();
      this.expressionUpdatedSource.next();
      return;
    }

    // Requires position handling
    const updatedEvent = new ExpressionUpdatedEvent();
    const targetNode = this._nodes.find((n) => n.getUuid() === insertPosition);
    // TODO: check this non null operator
    const targetNodeIndex = this._nodes.indexOf(targetNode!);

    if (targetNode instanceof TextNode) {
      if (offset === undefined || offset === null || offset < 0) {
        offset = targetNode.str.length;
      }
      const pre = targetNode.str.substring(0, offset);
      const post = targetNode.str.substring(offset);
      if (pre.length > 0) {
        if (newNodes[0] instanceof TextNode) {
          targetNode.str = pre + (newNodes[0] as TextNode).str;
          newNodes.splice(0, 1, targetNode);
        } else {
          targetNode.str = pre;
          newNodes.splice(0, 0, targetNode);
        }
      }
      if (post.length > 0) {
        const lastNewNodeIndex = newNodes.length - 1;
        if (newNodes[lastNewNodeIndex] instanceof TextNode) {
          let mergedTextNode: TextNode;
          if (pre.length > 0) {
            mergedTextNode = newNodes[lastNewNodeIndex] as TextNode;
            mergedTextNode.str += post;
          } else {
            mergedTextNode = targetNode;
            mergedTextNode.str =
              (newNodes[lastNewNodeIndex] as TextNode).str + post;
          }
          newNodes.splice(lastNewNodeIndex, 1, mergedTextNode);
        } else {
          if (pre.length > 0) {
            newNodes.push(new TextNode(post));
          } else {
            targetNode.str = post;
            newNodes.push(targetNode);
          }
        }
      }
      this._nodes.splice(targetNodeIndex, 1, ...newNodes);
      const lastAddedIndex = targetNodeIndex + newNodes.length - 1;
      if (
        this._nodes[lastAddedIndex] instanceof FieldNode &&
        this.nodes[lastAddedIndex + 1] instanceof FieldNode
      ) {
        // insert a glue in between FieldNodes so that it won't break syntax and caret can go into
        const space = new TextNode(' + ');
        this._nodes.splice(lastAddedIndex + 1, 0, space);
        updatedEvent.node = space;
        updatedEvent.offset = 1;
      } else if (this._nodes[lastAddedIndex] instanceof FieldNode) {
        updatedEvent.node = this._nodes[lastAddedIndex + 1];
        updatedEvent.offset = 0;
      } else {
        updatedEvent.node = this._nodes[lastAddedIndex];
        updatedEvent.offset =
          (this._nodes[lastAddedIndex] as TextNode).str.length - post.length;
      }
      this.updateCache();
      this.expressionUpdatedSource.next(updatedEvent);
      return;
    }

    // targetNode is a FieldNode - insert the text before it if offset is 0, otherwise after it
    if (offset !== 0 && newNodes[0] instanceof FieldNode) {
      // insert a glue in between FieldNodes so that it won't break syntax and caret can go into
      newNodes.splice(0, 0, new TextNode(' + '));
    }
    const nextNodeIndex = offset === 0 ? targetNodeIndex : targetNodeIndex + 1;
    const nextNode = this._nodes[nextNodeIndex];
    if (
      nextNode instanceof TextNode &&
      newNodes[newNodes.length - 1] instanceof TextNode
    ) {
      updatedEvent.offset = (
        newNodes[newNodes.length - 1] as TextNode
      ).str.length;
      nextNode.str =
        (newNodes[newNodes.length - 1] as TextNode).str +
        (nextNode as TextNode).str;
      newNodes.pop();
      this._nodes.splice(nextNodeIndex, 1, ...newNodes);
      updatedEvent.node = nextNode;
    } else if (
      nextNode instanceof FieldNode &&
      newNodes[newNodes.length - 1] instanceof FieldNode
    ) {
      // insert a glue in between FieldNodes so that it won't break syntax and caret can go into
      const space = new TextNode(' + ');
      this._nodes.splice(nextNodeIndex, 0, ...newNodes, space);
      updatedEvent.node = space;
      updatedEvent.offset = 1;
    } else {
      this._nodes.splice(nextNodeIndex, 0, ...newNodes);
      if (nextNode instanceof TextNode) {
        updatedEvent.node = nextNode;
        updatedEvent.offset = 0;
      } else {
        updatedEvent.node = newNodes[newNodes.length - 1];
        updatedEvent.offset = (
          newNodes[newNodes.length - 1] as TextNode
        ).str.length;
      }
    }
    this.updateCache();
    this.expressionUpdatedSource.next(updatedEvent);
  }

  removeToken(tokenPosition?: string, offset?: number, removeNext?: boolean) {
    // No position was specified - remove from the end
    if (!tokenPosition) {
      const last = this.getLastNode();
      if (!last) {
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
          // TODO: check this non null operator
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
      this.updateCache();
      this.expressionUpdatedSource.next();
      return;
    }

    // Requires position handling
    let updatedEvent: ExpressionUpdatedEvent | undefined =
      new ExpressionUpdatedEvent();
    let targetNode = this._nodes.find((n) => n.getUuid() === tokenPosition);
    // TODO: check this non null operator
    let targetNodeIndex = this._nodes.indexOf(targetNode!);
    if (removeNext) {
      targetNodeIndex++;
      targetNode = this._nodes[targetNodeIndex];
    }
    if (!targetNode || offset === -1) {
      if (targetNodeIndex < 1) {
        return;
      }
      targetNode = this._nodes[--targetNodeIndex];
      offset =
        targetNode instanceof TextNode
          ? (targetNode as TextNode).str.length
          : 1;
    }
    if (targetNode instanceof FieldNode) {
      const removed = this._nodes.splice(targetNodeIndex, 1);
      const targetFieldNode: FieldNode = removed[0] as FieldNode;
      if (
        !this._nodes.find(
          (n) =>
            n instanceof FieldNode &&
            n.mappedField === targetFieldNode.mappedField
        )
      ) {
        // TODO: check this non null operator
        this.mapping.removeField(targetFieldNode.mappedField!.field!);
        this.cfg.mappingService.updateMappedField(this.mapping);
      }
      if (this._nodes.length > targetNodeIndex) {
        if (
          this._nodes[targetNodeIndex - 1] instanceof TextNode &&
          this._nodes[targetNodeIndex] instanceof TextNode
        ) {
          const newOffset = (this._nodes[targetNodeIndex - 1] as TextNode).str
            .length;
          (this._nodes[targetNodeIndex - 1] as TextNode).str += (
            this._nodes[targetNodeIndex] as TextNode
          ).str;
          this._nodes.splice(targetNodeIndex, 1);
          updatedEvent.node = this._nodes[targetNodeIndex - 1];
          updatedEvent.offset = newOffset;
        } else if (
          this._nodes[targetNodeIndex - 1] instanceof FieldNode &&
          this._nodes[targetNodeIndex] instanceof FieldNode
        ) {
          const glue = new TextNode(' + ');
          this._nodes.splice(targetNodeIndex, 0, glue);
          updatedEvent.node = glue;
          updatedEvent.offset = 3;
        } else if (this._nodes[targetNodeIndex - 1] instanceof TextNode) {
          updatedEvent.node = this._nodes[targetNodeIndex - 1];
          updatedEvent.offset = (
            this._nodes[targetNodeIndex - 1] as TextNode
          ).str.length;
        } else if (this._nodes[targetNodeIndex] instanceof TextNode) {
          updatedEvent.node = this._nodes[targetNodeIndex];
          updatedEvent.offset = 0;
        }
      } else {
        // end of line
        updatedEvent = undefined;
      }
    } else {
      const targetString = (targetNode as TextNode).str;

      // Remove empty string text node.
      if (targetString.length === 1) {
        this._nodes.splice(targetNodeIndex, 1);
      } else {
        (targetNode as TextNode).str =
          offset === 0
            ? targetString.substr(1)
            : targetString.substring(0, offset) +
              targetString.substring(offset! + 1);
        updatedEvent.node = targetNode;
        updatedEvent.offset = offset;
      }
    }
    this.updateCache();
    this.expressionUpdatedSource.next(updatedEvent);
  }

  /**
   * Replace the content of the last text node with a substring terminating at the
   * specified index.
   *
   * @param index
   */
  clearToEnd(index: number): void {
    const last = this.getLastNode();
    if (!(last instanceof TextNode)) {
      return;
    }
    last.str = last.str.substring(0, index);
  }

  /**
   * Reflect mapped source fields to the field references in the expression.
   * Selected source fields are inserted into or appended to the expression,
   * and unselected source fields are removed from expression.
   *
   * @param mapping Corresponding MappingModel object
   * @param insertPosition
   * @param offset
   */
  updateFieldReference(
    mapping: MappingModel,
    insertPosition?: string,
    offset?: number
  ) {
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
      if (
        this._nodes.length > index &&
        this._nodes[index - 1] instanceof TextNode &&
        this._nodes[index] instanceof TextNode
      ) {
        (this._nodes[index - 1] as TextNode).str += (
          this._nodes[index] as TextNode
        ).str;
        this._nodes.splice(index, 1);
      }
    }

    // Add any non-reference mapped fields into the expression - append if no insert
    // position is specified.
    fieldNodes = this._nodes.filter(
      (n) => n instanceof FieldNode
    ) as FieldNode[];
    for (const mfield of mappedFields) {
      if (
        !fieldNodes.find((n) => n.mappedField === mfield) &&
        !referenceFields.find((r) => r.field === mfield.field)
      ) {
        if (insertPosition) {
          this.insertNodes(
            [new FieldNode(this.mapping, mfield)],
            insertPosition,
            offset
          );
        } else {
          this.appendFieldNode(mfield);
        }
      }
    }
    this.updateCache();
    this.expressionUpdatedSource.next();
  }

  clear() {
    this._nodes = [];
    this.updateCache();
    this.expressionUpdatedSource.next();
  }

  toText() {
    if (this.textCache.length === 0) {
      this.updateCache();
    }
    return this.textCache;
  }

  toHTML() {
    if (this.expressionHTML.length === 0) {
      this.updateCache();
    }
    return this.expressionHTML;
  }

  addConditionalExpressionNode(
    mappedField: MappedField,
    nodeId: string,
    offset: number
  ): void {
    this.insertNodes(
      [new FieldNode(this.mapping, mappedField)],
      nodeId,
      offset
    );
  }

  private updateCache() {
    let answer = '';
    this._nodes.forEach((node) => (answer += node.toText()));
    this.textCache = answer;
    answer = '';
    this._nodes.forEach((node) => (answer += node.toHTML()));
    this.expressionHTML = answer; // trigger expr box render
  }

  /**
   * Translate an expression string into an array of text nodes and field nodes.  Handle
   * relative paths for collection-based field references.
   *
   * @param text
   */
  private createNodesFromText(text: string): ExpressionNode[] {
    const answer = [];
    let position = -1;
    let collectionContextFieldNode = null;

    while (text.search(/\$\{[a-zA-Z0-9.:/<>[\]_-]+\}/) !== -1) {
      position = text.search(/\$/);
      if (position !== 0) {
        answer.push(new TextNode(text.substring(0, position)));
      }
      const nodeMetaVal = text.substring(position + 2, text.indexOf('}'));
      let fn = null;
      if (isNaN(Number(nodeMetaVal))) {
        fn = collectionContextFieldNode
          ? new FieldNode(
              this.mapping,
              undefined,
              nodeMetaVal,
              undefined,
              collectionContextFieldNode.mappedField?.field?.path!
            )
          : new FieldNode(this.mapping, undefined, nodeMetaVal);
      } else {
        const index = parseInt(nodeMetaVal, 10);
        fn = new FieldNode(this.mapping, undefined, undefined, index);
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
      } else {
        if (fn.mappedField?.field?.isCollection) {
          collectionContextFieldNode = fn;
        }
        answer.push(fn);
      }
      text = text.substring(text.indexOf('}') + 1);
    }
    if (text.length > 0) {
      answer.push(new TextNode(text));
    }
    return answer;
  }

  private appendFieldNode(mfield: MappedField) {
    const lastNode = this._nodes.pop();
    if (lastNode instanceof FieldNode) {
      this._nodes.push(lastNode, new TextNode(' + '));
    } else if (lastNode instanceof TextNode) {
      if (lastNode.str.length === 0) {
        this._nodes.push(new TextNode(' + '));
      } else {
        this._nodes.push(lastNode);
      }
    }
    this._nodes.push(new FieldNode(this.mapping, mfield));
  }
}
