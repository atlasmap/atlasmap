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

export interface ExpressionNode {
  toText(): string;
  toHTML(): string;
}

export class TextNode implements ExpressionNode {

  constructor(public readonly str: string) {}

  toText(): string {
    return this.str;
  }

  toHTML(): string {
    return this.str.replace(/ /g, '&nbsp;');
  }

}

export class FieldNode implements ExpressionNode {

  constructor(public readonly field?: MappedField, private index?: number, private pair?: FieldMappingPair) {
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
      return `<span title="${this.field.field.docDef.name}:${this.field.field.path}"
        class="inline-block label label-default">${this.field.field.name}</span>`;
    } else {
      return `<span title="Field index '${this.index}' is not available"
        class="inline-block label label-danger">N/A</span>`;
    }
  }

}

export class ExpressionModel {

  expressionUpdatedSource = new Subject<void>();
  expressionUpdated$ = this.expressionUpdatedSource.asObservable();

  private _nodes: ExpressionNode[] = [];
  private textCache = '';
  private htmlCache = '';

  constructor(private mappingPair: FieldMappingPair) {
    mappingPair.getUserMappedFields(true).forEach(f => this.appendFieldNode(f));
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

  addText(str: string) {
    const last = this.getLastNode();
    if (!last || last instanceof FieldNode) {
      this._nodes.push(...this.createNodesFromText(str));
    } else if (last instanceof TextNode) {
      const newString = (last as TextNode).toText() + str;
      this._nodes.splice(this.getLastNodeIndex(), 1, ...this.createNodesFromText(newString));
    } else {
      throw Error('Unsupported node type: ' + typeof last);
    }
    this.updateCache();
  }

  addNode(node: ExpressionNode, index?: number) {
    index == null ? this._nodes.push(node) : this._nodes.splice(index, 0, node);
    this.updateCache();
  }

  removeNode(pair: FieldMappingPair, index: number) {
    const removed = this._nodes.splice(index, 1);
    if (removed[0] instanceof FieldNode) {
      pair.removeMappedField((removed[0] as FieldNode).field, true);
    }
    this.updateCache();
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
  }

  clear() {
    this._nodes = [];
    this.updateCache();
  }

  toText() {
    return this.textCache;
  }

  toHTML() {
    return this.htmlCache;
  }

  private updateCache() {
    let answer = '';
    this.nodes.forEach(node => answer += node.toText());
    this.textCache = answer;
    answer = '';
    this.nodes.forEach(node => answer += node.toHTML());
    this.htmlCache = answer;
    this.expressionUpdatedSource.next();
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
    if (lastNode instanceof TextNode) {
      this._nodes.push(new TextNode(lastNode.str + ' '));
    } else if (lastNode) {
        this._nodes.push(lastNode, new TextNode(' '));
    }
    this._nodes.push(new FieldNode(mfield));
  }

}

