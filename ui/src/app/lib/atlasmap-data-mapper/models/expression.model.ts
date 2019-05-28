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
import { MappedField, FieldMappingPair } from './mapping.model';
import { Subject } from 'rxjs';

export class ExpressionUpdatedEvent {
  constructor(public node?: ExpressionNode, public offset?: number) {}
}

export abstract class ExpressionNode {
  protected static sequence = 0;
  protected uuid;

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

  constructor(public readonly str: string) {
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

  constructor(public readonly field?: MappedField, private index?: number, private pair?: FieldMappingPair) {
    super(FieldNode.PREFIX);
    if (!field) {
      this.field = pair.getMappedFieldForIndex((index + 1).toString(), true);
    }
  }

  toText(): string {
    const index = this.field ? parseInt(this.field.getFieldIndex(), 10) - 1 : this.index;
    return '${' + index + '}';
  }

  toHTML(): string {
    if (this.field) {
      return `<span contenteditable="false" id="${this.uuid}" title="${this.field.field.docDef.name}:${this.field.field.path}"
        class="expressionFieldLabel label label-default">${this.field.field.name}</span>`;
    } else {
      return `<span contenteditable="false" id="${this.uuid}" title="Field index '${this.index}' is not available"
        class="expressionFieldLabel label label-danger">N/A</span>`;
    }
  }

}

export class ExpressionModel {

  expressionUpdatedSource = new Subject<ExpressionUpdatedEvent>();
  expressionUpdated$ = this.expressionUpdatedSource.asObservable();

  private _nodes: ExpressionNode[] = [];
  private textCache = '';
  private htmlCache = '';

  constructor(private mappingPair: FieldMappingPair) {}

  generateInitialExpression() {
    this.mappingPair.getUserMappedFields(true).forEach(f => this.appendFieldNode(f));
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

  /**
   * Insert text into expression at specified position. It parses the string
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
    // No position was specified - append to the end
    if (!nodeId) {
      const last = this.getLastNode();
      if (!last || last instanceof FieldNode) {
        this._nodes.push(...this.createNodesFromText(str));
      } else if (last instanceof TextNode) {
        const newString = (last as TextNode).toText() + str;
        this._nodes.splice(this.getLastNodeIndex(), 1, ...this.createNodesFromText(newString));
      }
      this.updateCache();
      this.expressionUpdatedSource.next();
      return;
    }

    // Requires position handling
    const newNodes = this.createNodesFromText(str);
    const updatedEvent = new ExpressionUpdatedEvent();
    const targetNode = this._nodes.find(n => n.getUuid() === nodeId);
    const targetNodeIndex = this._nodes.indexOf(targetNode);
    if (targetNode instanceof TextNode) {
      if (offset === undefined || offset === null || offset < 0) {
        offset = targetNode.str.length;
      }
      const pre = targetNode.str.substring(0, offset);
      const post = targetNode.str.substring(offset);
      if (pre.length > 0) {
        if (newNodes[0] instanceof TextNode) {
          newNodes.splice(0, 1, new TextNode(pre + (newNodes[0] as TextNode).str));
        } else {
          newNodes.splice(0, 0, new TextNode(pre));
        }
      }
      if (post.length > 0) {
        const lastNewNodeIndex = newNodes.length - 1;
        if (newNodes[lastNewNodeIndex] instanceof TextNode) {
          newNodes.splice(lastNewNodeIndex, 1, new TextNode((newNodes[lastNewNodeIndex] as TextNode).str + post));
        } else {
          newNodes.push(new TextNode(post));
        }
      }
      this._nodes.splice(targetNodeIndex, 1, ...newNodes);
      const lastAddedIndex = targetNodeIndex + newNodes.length - 1;
      if (this._nodes[lastAddedIndex] instanceof FieldNode
          && this.nodes[lastAddedIndex + 1] instanceof FieldNode) {
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
        updatedEvent.offset = (this._nodes[lastAddedIndex] as TextNode).str.length - post.length;
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
    if (nextNode instanceof TextNode && newNodes[newNodes.length - 1] instanceof TextNode) {
      const concat = new TextNode((newNodes[newNodes.length - 1] as TextNode).str + (nextNode as TextNode).str);
      this._nodes.splice(nextNodeIndex, 1, ...newNodes, concat);
      updatedEvent.node = concat;
      updatedEvent.offset = concat.str.length - (nextNode as TextNode).str.length;
    } else if (nextNode instanceof FieldNode && newNodes[newNodes.length - 1] instanceof FieldNode) {
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
        updatedEvent.offset = (newNodes[newNodes.length - 1] as TextNode).str.length;
      }
    }
    this.updateCache();
    this.expressionUpdatedSource.next(updatedEvent);
  }

  addNode(node: ExpressionNode, index?: number) {
    index == null ? this._nodes.push(node) : this._nodes.splice(index, 0, node);
    this.updateCache();
    this.expressionUpdatedSource.next();
  }

  removeNode(pair: FieldMappingPair, index: number) {
    const removed = this._nodes.splice(index, 1);
    if (removed[0] instanceof FieldNode) {
      pair.removeMappedField((removed[0] as FieldNode).field, true);
    }
    this.updateCache();
    this.expressionUpdatedSource.next();
  }

  removeLastToken(lastFieldRefRemoved: (removed: MappedField) => void) {
    const last = this.getLastNode();
    if (!last) {
      return;
    }
    if (last instanceof FieldNode) {
      const removed = this._nodes.pop() as FieldNode;
      if (!this._nodes.find(n => n instanceof FieldNode && n.field === removed.field)) {
        lastFieldRefRemoved(removed.field);
      }
    } else if (last instanceof TextNode) {
      const str = (last as TextNode).toText();
      this._nodes.pop();
      if (str.length > 1) {
        this.addNode(new TextNode(str.substring(0, str.length - 1)));
      }
    } else {
      throw new Error('Unknown Node type: ' + typeof last);
    }
    this.updateCache();
    this.expressionUpdatedSource.next();
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
    this._nodes.pop();
    this.addNode(new TextNode(last.toText().substring(0, index)));
  }

  /**
   * Reflect mapped source fields to the field references in the expression.
   * Selected source fields are appended to the expression,
   * and unselected source fields are removed from expression.
   *
   * @param pair Corresponding FieldMappingPair object
   */
  updateFieldReference(pair: FieldMappingPair) {
    const mappedFields = pair.getUserMappedFields(true);
    const toAdd: MappedField[] = [];
    const toRemove: MappedField[] = [];
    let fieldNodes = this._nodes.filter(n => n instanceof FieldNode) as FieldNode[];
    // Remove removed field from expression
    for (const node of fieldNodes) {
      if (mappedFields.includes(node.field)) {
        continue;
      }
      const index = this._nodes.indexOf(node);
      this._nodes.splice(index, 1);
      if (this._nodes.length > index && this._nodes[index - 1] instanceof TextNode
          && this._nodes[index] instanceof TextNode) {
        const newStr =
          (this._nodes[index - 1] as TextNode).str + (this._nodes[index] as TextNode).str;
        this._nodes.splice(index - 1, 2, new TextNode(newStr));
      }
    }
    // Append added field into expression
    fieldNodes = this._nodes.filter(n => n instanceof FieldNode) as FieldNode[];
    for (const mfield of mappedFields) {
      if (!fieldNodes.find(n => n.field === mfield)) {
        this.appendFieldNode(mfield);
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
    return this.textCache;
  }

  toHTML() {
    return this.htmlCache;
  }

  private updateCache() {
    let answer = '';
    this._nodes.forEach(node => answer += node.toText());
    this.textCache = answer;
    answer = '';
    this._nodes.forEach(node => answer += node.toHTML());
    this.htmlCache = answer;
  }

  private createNodesFromText(text: string): ExpressionNode[] {
    const answer = [];
    let position = -1;
    while ((position = text.search(/\$\{[0-9]+\}/)) !== -1 ) {
      if (position !== 0) {
        answer.push(new TextNode(text.substring(0, position)));
      }
      const index = parseInt(text.substring(position + 2, text.indexOf('}')), 10);
      answer.push(new FieldNode(null, index, this.mappingPair));
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
    this._nodes.push(new FieldNode(mfield));
  }

}

