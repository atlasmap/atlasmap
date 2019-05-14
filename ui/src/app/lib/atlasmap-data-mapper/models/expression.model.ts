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

export interface ExpressionNode {
  toText(): string;
  toHTML(): string;
}

export class TextNode implements ExpressionNode {
  private str: string;

  constructor(str: string) {
    this.str = str;
  }


  toText(): string {
    return this.str;
  }

  toHTML(): string {
    return this.str.replace(/ /g, '&nbsp;');
  }

}

export class FieldNode implements ExpressionNode {

  constructor(private field?: MappedField, private index?: number, private pair?: FieldMappingPair) {
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

  private _nodes: ExpressionNode[] = [];
  private textCache: string;
  private htmlCache: string;

  constructor(private mappingPair: FieldMappingPair) {}

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

  removeNode(index: number) {
    this._nodes.splice(index, 1);
    this.updateCache();
  }

  removeLastToken() {
    const last = this.getLastNode();
    if (!last) {
      return;
    }
    if (last instanceof FieldNode) {
      this._nodes.pop();
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
  }

  private createNodesFromText(text: string): ExpressionNode[] {
    const answer = [];
    let position = -1;
    while ((position = text.search(/\$\{[0-9]+\}/)) !== -1 ) {
      if (position !== 0) {
        answer.push(new TextNode(text.substring(0, position)));
      }
      const index = text.substring(position + 2, text.indexOf('}'));
      const field = this.mappingPair.getMappedFieldForIndex((parseInt(index, 10) + 1).toString(), true);
      answer.push(new FieldNode(field));
      text = text.substring(text.indexOf('}') + 1);
    }
    if (text.length > 0) {
      answer.push(new TextNode(text));
    }
    return answer;
  }

}

