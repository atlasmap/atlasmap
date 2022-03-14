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
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
} from '../models/error.model';
import { MappedField, MappingModel } from '../models/mapping.model';
import { TransitionMode, TransitionModel } from '../models/transition.model';

import { CommonUtil } from '../utils/common-util';
import { ConfigModel } from '../models/config.model';
import { ExpressionModel } from '../models/expression.model';
import { Field } from '../models/field.model';
import { FieldActionArgumentValue } from '../models/field-action.model';
import { FieldType } from '../contracts/common';
import { IExpressionNode } from '../contracts/expression';
import { MappingUtil } from '../utils/mapping-util';
import { Multiplicity } from '../contracts/field-action';
import { Subscription } from 'rxjs';

/**
 * Manages conditional mapping expression.
 */
export class MappingExpressionService {
  cfg!: ConfigModel;

  private mappingUpdatedSubscription?: Subscription;

  willClearOutSourceFieldsOnTogglingExpression() {
    if (this.cfg.mappings?.activeMapping?.transition.enableExpression) {
      return (
        this.cfg.mappings.activeMapping.getFirstCollectionField(true) != null
      );
    } else {
      return false;
    }
  }

  isExpressionEnabledForActiveMapping(): boolean {
    return !!this.cfg.mappings?.activeMapping?.transition?.enableExpression;
  }

  toggleExpressionMode() {
    if (
      !this.cfg.mappings ||
      !this.cfg.mappings.activeMapping ||
      !this.cfg.mappings.activeMapping.transition
    ) {
      this.cfg.errorService.addError(
        new ErrorInfo({
          message: 'Please select a mapping first.',
          level: ErrorLevel.INFO,
          scope: ErrorScope.MAPPING,
          type: ErrorType.USER,
        })
      );
      return;
    }
    const activeMapping = this.cfg.mappings.activeMapping;
    if (activeMapping.transition.mode === TransitionMode.ONE_TO_MANY) {
      this.cfg.errorService.addError(
        new ErrorInfo({
          message: `Cannot establish a conditional mapping expression when multiple target fields are selected.
        Please select only one target field and try again.`,
          level: ErrorLevel.WARN,
          scope: ErrorScope.MAPPING,
          type: ErrorType.USER,
          mapping: activeMapping,
        })
      );
      return;
    }

    if (this.willClearOutSourceFieldsOnTogglingExpression()) {
      // Clear out source fields, if the mapping contains a source collection
      activeMapping.sourceFields.splice(0, activeMapping.sourceFields.length);
    }

    activeMapping.transition.enableExpression =
      !activeMapping.transition.enableExpression;
    if (activeMapping.transition.enableExpression) {
      activeMapping.transition.mode = TransitionMode.EXPRESSION;
      activeMapping.transition.transitionFieldAction = null;
      this.mappingUpdatedSubscription =
        this.cfg.mappingService.mappingUpdated$.subscribe(() => {
          if (
            !this.cfg ||
            !this.cfg.mappings ||
            !this.cfg.mappings.activeMapping
          ) {
            return;
          }
          if (this.cfg.mappings.activeMapping.isFullyMapped()) {
            this.updateExpression(this.cfg.mappings.activeMapping);
          }
        });
      if (activeMapping.transition.expression) {
        this.cfg.mappings.activeMapping.transition.expression.expressionUpdatedSource.next();
      }
    } else {
      this.mappingUpdatedSubscription?.unsubscribe();
      activeMapping.transition.mode = TransitionMode.ONE_TO_ONE;
      this.cfg.mappingService.updateActiveMappingTransition();
    }
  }

  updateExpression(mapping: MappingModel, position?: string, offset?: number) {
    // Update conditional expression field references.
    mapping.transition.expression?.updateFieldReference(
      mapping,
      position,
      offset
    );
  }

  /**
   * Add the specified document ID/field path to the specified mapping/ expression.
   *
   * @param mapping
   * @param docId
   * @param fieldPath
   * @param newTextNode
   * @param atIndex
   * @param isTrailer
   */
  addFieldToExpression(
    mapping: MappingModel,
    docId: string,
    fieldPath: string,
    newTextNode: IExpressionNode,
    atIndex: number,
    isTrailer: boolean
  ) {
    let mappedField = mapping.getMappedFieldByPath(fieldPath, true, docId);

    if (!mappedField) {
      // If the selected field was not part of the original mapping
      // and is complex then add it as a reference node.
      mappedField = mapping.getReferenceField(docId, fieldPath);
      if (!mappedField) {
        // Try adding the selected field to the active mapping.
        const docDef = this.cfg.getDocForIdentifier(docId, true);
        const field = Field.getField(fieldPath, docDef?.getAllFields()!);
        if (field) {
          this.updateExpression(
            mapping,
            newTextNode.getUuid(),
            isTrailer ? newTextNode.toText().length : atIndex
          );
          this.cfg.mappingService.addFieldToActiveMapping(field);
        }
        mappedField = mapping.getMappedFieldByPath(fieldPath, true, docId);
        if (!mappedField) {
          return;
        }
      }
    }
    mapping.transition!.expression?.addConditionalExpressionNode(
      mappedField,
      newTextNode.getUuid(),
      isTrailer ? newTextNode.str.length : atIndex
    );
  }

  /**
   * Return an array of strings representing display names of mapping fields based on the
   * specified filter.
   * @todo ATM this is specialized for listing up candidate fields for adding into expression.
   * We will need to consolidate with field filter on the Document tree when we implement server
   * side field search - https://github.com/atlasmap/atlasmap/issues/603
   *
   * @param filter
   */
  executeFieldSearch(
    configModel: ConfigModel,
    filter: string,
    isSource: boolean
  ): string[][] {
    const activeMapping = configModel.mappings!.activeMapping;
    if (!activeMapping) {
      return [];
    }
    const formattedFields: string[][] = [];
    let fields: Field[] = [];
    for (const docDef of configModel.getDocs(isSource)) {
      fields = docDef.getTerminalFields();
      fields = fields.concat(docDef.getComplexFields());
      Field.alphabetizeFields(fields);
      let documentName = '';
      let fieldCount = -1;

      for (const field of fields) {
        const formattedField: string[] = [''];
        let displayName =
          field == null ? '' : field.getFieldLabel(configModel.showTypes, true);

        if (
          filter == null ||
          filter === '' ||
          displayName.toLowerCase().indexOf(filter.toLowerCase()) !== -1
        ) {
          if (
            !configModel.mappingService.isFieldSelectable(
              activeMapping,
              field
            ) &&
            field.type !== FieldType.COMPLEX
          ) {
            continue;
          }
          if (documentName !== field.docDef.name) {
            if (fieldCount === 0) {
              formattedFields.pop();
              continue;
            } else {
              const documentField = [''];
              documentName = field.docDef.name;
              documentField[0] = documentName;
              documentField[1] = field.docDef.id;
              fieldCount = 0;
              formattedFields.push(documentField);
            }
          }
          displayName = CommonUtil.extractDisplayPath(field.path, 100);
          formattedField[0] = field.docDef.id;
          formattedField[1] = field.path;
          fieldCount++;
          formattedFields.push(formattedField);
        }
        if (formattedFields.length > 500) {
          const endField: string[] = [''];
          endField[0] = '** Displayable field limit reached **';
          endField[1] = '';
          fieldCount++;
          formattedFields.push(endField);
          break;
        }
      }
    }
    return formattedFields;
  }

  /**
   * Return a string, in either text or HTML form, representing the
   * expression mapping of either the optionally specified mapping or
   * the active mapping if it exists, empty string otherwise.
   * @todo avoid any, use typed mapping object - https://github.com/atlasmap/atlasmap/issues/2975
   * @param asHTML
   * @param mapping
   */
  getMappingExpressionStr(asHTML: boolean, mapping?: any): string {
    if (!mapping && !MappingUtil.activeMapping(this.cfg)) {
      return '';
    }
    if (!mapping) {
      mapping = this.cfg.mappings?.activeMapping;
    }
    if (!mapping.transition.expression) {
      if (
        mapping.transition.enableExpression &&
        MappingUtil.hasFieldAction(mapping.sourceFields)
      ) {
        this.createMappingExpression(mapping);
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
  createMappingExpression(mapping: MappingModel): string {
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
        mapping.transition.transitionFieldAction?.definition?.multiplicity ===
          Multiplicity.MANY_TO_ONE)
    ) {
      expr = 'Concatenate (';
      expr += this.fieldActionsToExpression(mapping);
      expr +=
        ", '" +
        TransitionModel.delimiterModels[mapping.transition.delimiter]
          .actualDelimiter +
        "')";
    } else if (
      (targetMappedFields.length > 1 || targetMappedCollection) &&
      mapping.transition.transitionFieldAction?.definition?.multiplicity ===
        Multiplicity.ONE_TO_MANY
    ) {
      expr = 'Split (';
      expr += this.fieldActionsToExpression(mapping);
      expr += ')';
    } else {
      expr += this.fieldActionsToExpression(mapping);
    }
    mapping.transition.expression = new ExpressionModel(mapping, this.cfg);
    mapping.transition.expression.insertText(expr);
    return expr;
  }

  private qualifiedExpressionRef(mappedField: MappedField): string {
    if (mappedField.field?.path !== null) {
      return (
        '${' +
        mappedField.field?.docDef?.id +
        ':' +
        mappedField.field?.path +
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
  private fieldActionArgumentToExpression(
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
  private fieldActionToExpression(
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
            action.definition!.arguments[actionArgIndex].type
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
  private fieldActionsToExpression(mapping: MappingModel): string {
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
