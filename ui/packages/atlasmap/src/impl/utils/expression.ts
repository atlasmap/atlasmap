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
import {
  ConfigModel,
  ExpressionModel,
  IExpressionModel,
  MappedField,
  MappingModel,
} from '@atlasmap/core';

function activeMapping(): boolean {
  const cfg = ConfigModel.getConfig();
  return !!cfg?.mappings?.activeMapping;
}

export abstract class ExpressionNode {
  protected static sequence = 0;
  protected uuid: string;
  constructor(prefix: string) {
    this.uuid = prefix + ExpressionNode.sequence++;
  }
  getUuid() {
    return this.uuid;
  }
  abstract toText(): string;
}

export class FieldNode extends ExpressionNode {
  protected field: MappedField | null | undefined;
  static readonly PREFIX = 'expression-field-';

  constructor(
    private mapping: MappingModel,
    public mfield?: MappedField,
    index?: number,
  ) {
    super(FieldNode.PREFIX);
    if (!mfield && index) {
      this.field = mapping.getMappedFieldForIndex((index + 1).toString(), true);
    }
  }

  toText(): string {
    if (!this.mapping || !this.field) {
      return '';
    }
    return '${' + (this.mapping.getIndexForMappedField(this.field)! - 1) + '}';
  }
}

export function getExpression(): ExpressionModel | null | undefined {
  const cfg = ConfigModel.getConfig();
  if (!activeMapping() || !cfg.mappings?.activeMapping) {
    return null;
  }
  const mapping = cfg.mappings!.activeMapping;
  let expression: IExpressionModel = mapping!.transition.expression!;
  if (!expression) {
    mapping!.transition.expression = new ExpressionModel(mapping!, cfg);
    expression = mapping!.transition.expression;
    expression.generateInitialExpression();
    expression.updateFieldReference(mapping!);
  } else {
    if (mapping!.transition.expression) {
      (mapping!.transition.expression as IExpressionModel).setConfigModel(cfg);
    }
  }
  return mapping!.transition.expression;
}

export function getMappingExpression(): string {
  const cfg = ConfigModel.getConfig();
  if (!activeMapping()) {
    return '';
  }
  if (!cfg.mappings?.activeMapping?.transition?.expression) {
    if (!getExpression()) {
      return '';
    }
  }
  return cfg.mappings!.activeMapping!.transition.expression &&
    cfg.mappings!.activeMapping!.transition.enableExpression
    ? (
        cfg.mappings!.activeMapping!.transition.expression as IExpressionModel
      ).toText()
    : '';
}
