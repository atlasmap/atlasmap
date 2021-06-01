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
  FieldActionArgumentValue,
  Multiplicity,
} from '../models/field-action.model';
import { MappedField, MappingModel } from '../models/mapping.model';

import { ConfigModel } from '../models/config.model';
import { ExpressionModel } from '../models/expression.model';
import { MappingUtil } from './mapping-util';
import { TransitionModel } from '../models/transition.model';

/**
 * Static routines for handling the expression.
 */
export class ExpressionUtil {
  /**
   * Return a string, in either text or HTML form, representing the
   * expression mapping of either the optionally specified mapping or
   * the active mapping if it exists, empty string otherwise.
   *
   * @param asHTML
   * @param mapping
   */
  static getMappingExpressionStr(
    cfg: ConfigModel,
    asHTML: boolean,
    mapping?: any
  ): string {
    if (!mapping && !MappingUtil.activeMapping(cfg)) {
      return '';
    }
    if (!mapping) {
      mapping = cfg.mappings?.activeMapping;
    }
    if (!mapping.transition.expression) {
      if (
        mapping.transition.enableExpression &&
        MappingUtil.hasFieldAction(mapping.sourceFields)
      ) {
        ExpressionUtil.createMappingExpression(cfg, mapping);
      } else {
        return '';
      }
    }

    if (mapping.transition.expression && mapping.transition.enableExpression) {
      return asHTML
        ? mapping.transition.expression.expressionHTML
        : mapping.transition.expression.toText(true);
    }
    return '';
  }

  /**
   * Create a conditional mapping expression from the specified mapping model.  Start
   * with a multiplicity action if applicable, then any field-specific field actions.
   *
   * @param mapping
   */
  static createMappingExpression(
    cfg: ConfigModel,
    mapping: MappingModel
  ): string {
    let expr = '';
    const sourceMappedFields = mapping.getMappedFields(true);
    const sourceMappedCollection = MappingUtil.hasMappedCollection(
      mapping,
      true
    );
    const targetMappedFields = mapping.getMappedFields(false);
    const targetMappedCollection = MappingUtil.hasMappedCollection(
      mapping,
      false
    );

    if (
      sourceMappedFields.length > 1 ||
      (sourceMappedCollection &&
        mapping.transition.transitionFieldAction &&
        mapping.transition.transitionFieldAction.definition.multiplicity ===
          Multiplicity.MANY_TO_ONE)
    ) {
      expr = 'Concatenate (';
      expr += ExpressionUtil.fieldActionsToExpression(mapping);
      expr +=
        ", '" +
        TransitionModel.delimiterModels[mapping.transition.delimiter]
          .actualDelimiter +
        "')";
    } else if (
      (targetMappedFields.length > 1 || targetMappedCollection) &&
      mapping.transition.transitionFieldAction &&
      mapping.transition.transitionFieldAction.definition.multiplicity ===
        Multiplicity.ONE_TO_MANY
    ) {
      expr = 'Split (';
      expr += ExpressionUtil.fieldActionsToExpression(mapping);
      expr += ')';
    } else {
      expr += ExpressionUtil.fieldActionsToExpression(mapping);
    }
    mapping.transition.expression = new ExpressionModel(mapping, cfg);
    mapping.transition.expression.insertText(expr);
    return expr;
  }

  private static qualifiedExpressionRef(mappedField: MappedField): string {
    if (mappedField.parsedData.parsedPath !== null) {
      return (
        '${' +
        mappedField.parsedData.parsedDocID +
        ':' +
        mappedField.parsedData.parsedPath +
        '}'
      );
    } else {
      return 'null';
    }
  }

  /**
   * Create a conditional expression fragment based on the specified field action
   * argument and type.
   *
   * @param actionArgument
   * @param actionArgType
   */
  private static fieldActionArgumentToExpression(
    actionArgument: FieldActionArgumentValue,
    actionArgType: string
  ): string {
    if (actionArgType === 'string') {
      return "'" + actionArgument.value + "'";
    } else {
      return actionArgument.value;
    }
  }

  /**
   * Create a conditional expression fragment based on a single field action
   * and its arguments if any.
   *
   * @param mappedField
   * @param mfActionIndex
   */
  private static fieldActionToExpression(
    mappedField: MappedField,
    mfActionIndex: number
  ): string {
    let action = mappedField.actions[mfActionIndex];
    let expression = action.name + ' (';
    if (mfActionIndex < mappedField.actions.length - 1) {
      mfActionIndex++;
      expression += this.fieldActionToExpression(mappedField, mfActionIndex);
    } else {
      expression += this.qualifiedExpressionRef(mappedField);
    }
    if (action.argumentValues.length > 0) {
      for (
        let actionArgIndex = 0;
        actionArgIndex < action.argumentValues.length;
        actionArgIndex++
      ) {
        expression +=
          ', ' +
          this.fieldActionArgumentToExpression(
            action.argumentValues[actionArgIndex],
            action.definition.arguments[actionArgIndex].type
          );
      }
    }
    expression += ')';
    return expression;
  }

  /**
   * Create a conditional expression fragment based on the field actions of the specified
   * mapping model and the root field reference.
   *
   * @param mapping
   */
  private static fieldActionsToExpression(mapping: MappingModel): string {
    let expression = '';
    let mappedField: MappedField;

    for (
      let mappedFieldIndex = 0;
      mappedFieldIndex < mapping.sourceFields.length;
      mappedFieldIndex++
    ) {
      mappedField = mapping.sourceFields[mappedFieldIndex];
      if (mappedField.actions.length > 0) {
        let mfActionIndex = 0;
        expression += this.fieldActionToExpression(mappedField, mfActionIndex);
      } else {
        expression += this.qualifiedExpressionRef(mappedField);
      }
      if (mappedFieldIndex !== mapping.sourceFields.length - 1) {
        expression += ', ';
      }
    }
    return expression;
  }
}
