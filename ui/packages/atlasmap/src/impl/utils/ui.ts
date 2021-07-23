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
  CommonUtil,
  ConfigModel,
  DocumentDefinition,
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
  ExpressionModel,
  Field,
  FieldAction,
  FieldActionDefinition,
  InitializationService,
  MappedField,
  MappingDefinition,
  MappingModel,
  Multiplicity,
  NamespaceModel,
  TransitionMode,
  TransitionModel,
} from '@atlasmap/core';
import {
  IAtlasmapDocument,
  IAtlasmapField,
  IAtlasmapGroup,
  IAtlasmapMappedField,
  IAtlasmapMapping,
  IAtlasmapNamespace,
  INotification,
} from '../../Views';
import { ITransformationArgument, ITransformationSelectOption } from '../../UI';

import { Observable } from 'rxjs';
import ky from 'ky';

const api = ky.create({ headers: { 'ATLASMAP-XSRF-TOKEN': 'awesome' } });

export function copyToClipboard(text: string) {
  const textArea = document.createElement('textarea');
  textArea.value = text;
  document.body.appendChild(textArea);
  textArea.select();
  try {
    document.execCommand('copy');
  } catch (err) {
    ConfigModel.getConfig().errorService.addError(
      new ErrorInfo({
        message: 'Error copying ' + text + ' to clipboard',
        level: ErrorLevel.ERROR,
        scope: ErrorScope.APPLICATION,
        type: ErrorType.INTERNAL,
      }),
    );
  }
  document.body.removeChild(textArea);
}

export const initializationService: InitializationService =
  new InitializationService(api);
export const configModel = initializationService.cfg;

export function fromFieldToIFieldsGroup(field: Field): IAtlasmapGroup | null {
  const fields = field.children
    .map(fromFieldToIFields)
    .filter((f) => f) as IAtlasmapField[];
  return fields.length > 0 && field.visibleInCurrentDocumentSearch
    ? {
        id: `${field.docDef.uri || field.docDef.type + '-' + field.scope}:${
          field.docDef.isSource ? 'source' : 'target'
        }:${field.path}`,
        name: field.name,
        type: field.type,
        isCollection: field.isCollection,
        isInCollection: field.isInCollection(),
        fields: fields,
        amField: field,
      }
    : null;
}

export function fromFieldToIFieldsNode(field: Field): IAtlasmapField | null {
  const cfg = ConfigModel.getConfig();
  const partOfMapping: boolean = field.partOfMapping;
  const shouldBeVisible = partOfMapping
    ? cfg.showMappedFields
    : cfg.showUnmappedFields && field.visibleInCurrentDocumentSearch;
  return shouldBeVisible
    ? {
        id: `${field.docDef.uri || field.docDef.type + '-' + field.scope}:${
          field.docDef.isSource ? 'source' : 'target'
        }:${field.path}`,
        name: field.getFieldLabel(false, false),
        type: field.type,
        path: field.path,
        scope: field.scope ? field.scope : '',
        value: field.value,
        amField: field,
        mappings:
          (cfg.mappings
            ?.findMappingsForField(field)
            .map(fromMappingModelToImapping)
            .filter((d) => d) as IAtlasmapMapping[]) || [],
        hasTransformations: field.partOfTransformation,
        isAttribute: field.isAttribute,
        isCollection: field.isCollection,
        isInCollection: field.isInCollection(),
        isConnected: partOfMapping,
        isDisabled:
          field.type === 'UNSUPPORTED' ||
          (field.type === 'COMPLEX' && !field.enumeration),
        enumeration: field.enumeration,
      }
    : null;
}

export function fromFieldToIFields(field: Field) {
  return field.children.length > 0
    ? fromFieldToIFieldsGroup(field)
    : fromFieldToIFieldsNode(field);
}

export function fromNamespaceModelToINamespace(namespace: NamespaceModel) {
  return {
    alias: namespace.alias,
    uri: namespace.uri,
    locationUri: namespace.locationUri,
    isTarget: namespace.isTarget,
  };
}

export function fromDocumentDefinitionToFieldGroup(
  def: DocumentDefinition,
): IAtlasmapDocument | null {
  if (!def || !def.fields || def.fields.length === 0) {
    return null;
  }
  const fields = def.fields
    .map(fromFieldToIFields)
    .filter((f) => f) as IAtlasmapField[];
  const namespaces = def.namespaces
    .map(fromNamespaceModelToINamespace)
    .filter((n) => n) as IAtlasmapNamespace[];
  return def.visibleInCurrentDocumentSearch && fields.length > 0
    ? {
        id: def.id,
        fields,
        name: def.name,
        type: def.type,
        namespaces,
      }
    : null;
}

export function fromMappedFieldToIMappingField(
  mappedField: MappedField,
): IAtlasmapMappedField | null {
  if (!mappedField.field || !mappedField.field.docDef) {
    return null;
  }
  return {
    id: `${
      mappedField.field!.docDef.uri ||
      mappedField.field!.docDef.type + '-' + mappedField.field!.scope
    }:${mappedField.field!.docDef.isSource ? 'source' : 'target'}:${
      mappedField.field!.path
    }`,
    name: mappedField.field!.getFieldLabel(false, false),
    type: mappedField.field!.type,
    path: mappedField.field!.path,
    scope: mappedField.field?.scope ? mappedField.field.scope : '',
    value: mappedField.field!.value,
    hasTransformations: false,
    mappings: [],
    isAttribute: false,
    isCollection: false,
    isInCollection: false,
    isConnected: false,
    isDisabled: false,
    enumeration: false,
    amField: mappedField.field,
    transformations: mappedField.actions.map((a) => ({
      name: a.name,
      options: getMappingActions(mappedField.isSource()).map(
        (a): ITransformationSelectOption => ({
          name: CommonUtil.toDisplayable(a.name),
          value: a.name,
        }),
      ),
      arguments: a.definition!.arguments.map(
        (av, idx): ITransformationArgument => ({
          label:
            av.serviceObject?.title?.length > 0
              ? av.serviceObject.title
              : CommonUtil.toDisplayable(av.name),
          type: av.type,
          name: av.name,
          value: a.argumentValues[idx].value,
          options: av.values
            ? av.values.map(
                (avv): ITransformationSelectOption => ({
                  name: avv,
                  value: avv,
                }),
              )
            : undefined,
        }),
      ),
    })),
  };
}

export function errorLevelToVariant(
  level: ErrorLevel,
): INotification['variant'] {
  switch (level) {
    case ErrorLevel.INFO:
      return 'info';
    case ErrorLevel.WARN:
      return 'warning';
    case ErrorLevel.ERROR:
      return 'danger';
    default:
      return 'default';
  }
}

export function errorMessageToString(message: any): string {
  switch (typeof message) {
    case 'string':
      return message;
    default:
      return message?.message || JSON.stringify(message);
  }
}

export function errorTypeToString(type: ErrorType): string {
  switch (type) {
    case ErrorType.INTERNAL:
      return 'Internal';
    case ErrorType.USER:
      return 'User';
    case ErrorType.VALIDATION:
      return 'Validation';
    case ErrorType.PREVIEW:
      return 'Preview';
    case ErrorType.FORM:
      return 'Form';
  }
}

export function errorInfoToNotification(e: ErrorInfo): INotification {
  return {
    variant: errorLevelToVariant(e.level),
    title:
      e.mapping && e.type !== ErrorType.PREVIEW
        ? `${errorTypeToString(
            e.type,
          )}: "${e.mapping.transition.getPrettyName()}"`
        : errorTypeToString(e.type),
    description: errorMessageToString(e.message),
    id: e.identifier,
    mappingId: e.mapping?.uuid,
  };
}

export function fromMappingModelToImapping(
  m: MappingModel | null | undefined,
): IAtlasmapMapping | null {
  return m
    ? {
        id: m.uuid,
        name: m.transition.getPrettyName(),
        sourceFields: m
          .getUserMappedFields(true)
          .map(fromMappedFieldToIMappingField)
          .filter((f) => f) as IAtlasmapMappedField[],
        targetFields: m
          .getUserMappedFields(false)
          .map(fromMappedFieldToIMappingField)
          .filter((f) => f) as IAtlasmapMappedField[],
        mapping: m,
      }
    : null;
}

export function fromMappingDefinitionToIMappings(
  def: MappingDefinition | null,
): IAtlasmapMapping[] {
  return def
    ? (def.mappings
        .map(fromMappingModelToImapping)
        .filter((d) => d) as IAtlasmapMapping[])
    : [];
}

export function executeFieldSearch(
  searchFilter: string,
  isSource: boolean,
): string[][] {
  return initializationService.cfg.expressionService.executeFieldSearch(
    initializationService.cfg,
    searchFilter,
    isSource,
  );
}

export function getField(fieldPath: string, isSource: boolean): Field | null {
  let field: Field | null = null;
  for (const doc of initializationService.cfg.getDocs(isSource)) {
    field = Field.getField(fieldPath, doc.getAllFields());
    if (field) {
      break;
    }
  }
  return field;
}

export function mappingExpressionAddField(
  selectedDocId: string,
  selectedField: string,
  newTextNode: any,
  atIndex: number,
  isTrailer: boolean,
) {
  const mapping = initializationService.cfg.mappings!.activeMapping;
  if (!mapping || !selectedDocId || !selectedField) {
    return;
  }
  configModel.expressionService.addFieldToExpression(
    mapping,
    selectedDocId,
    selectedField,
    newTextNode,
    atIndex,
    isTrailer,
  );
  initializationService.cfg.mappingService.notifyMappingUpdated();
}

export function mappingExpressionInit() {
  if (
    !initializationService.cfg.mappings ||
    !initializationService.cfg.mappings.activeMapping
  ) {
    return;
  }
  const mapping = initializationService.cfg.mappings!.activeMapping!;

  // Filter out padding fields for expression mapping.
  mapping
    .getMappedFields(true)
    .filter((mf) => mf.isPadField())
    .forEach((mf) => mapping.removeMappedField(mf));

  if (!mapping.transition.expression) {
    mapping.transition.expression = new ExpressionModel(
      mapping,
      initializationService.cfg,
    );
    mapping.transition.expression.generateInitialExpression();
  } else {
    mapping.transition.expression.setConfigModel(initializationService.cfg);
  }
  mapping.transition.expression.updateFieldReference(mapping);
}
export function mappingExpressionClearText(
  nodeId?: string,
  startOffset?: number,
  endOffset?: number,
) {
  const uuidNode =
    initializationService.cfg.mappings!.activeMapping!.transition.expression!.clearText(
      nodeId!,
      startOffset,
      endOffset,
    );
  initializationService.cfg.mappingService.notifyMappingUpdated();
  return uuidNode;
}
export function mappingExpressionInsertText(
  str: string,
  nodeId?: string,
  offset?: number,
) {
  initializationService.cfg.mappings!.activeMapping!.transition.expression!.insertText(
    str,
    nodeId,
    offset,
  );
  initializationService.cfg.mappingService.notifyMappingUpdated();
}
export function mappingExpressionObservable(): Observable<any> | null {
  if (
    !initializationService.cfg.mappings?.activeMapping?.transition?.expression
  ) {
    return null;
  }
  return initializationService.cfg.mappings.activeMapping.transition.expression
    .expressionUpdated$;
}
export function mappingExpressionRemoveField(
  tokenPosition?: string,
  offset?: number,
  removeNext?: boolean,
) {
  initializationService.cfg.mappings!.activeMapping!.transition.expression!.removeToken(
    tokenPosition,
    offset,
    removeNext,
  );
  initializationService.cfg.mappingService.notifyMappingUpdated();
}
export function onFieldPreviewChange(field: IAtlasmapField, value: string) {
  field.amField.value = value;
  initializationService.cfg.mappingService.notifyMappingUpdated();
}

export function toggleExpressionMode() {
  initializationService.cfg.expressionService.toggleExpressionMode();
  initializationService.cfg.mappingService.notifyMappingUpdated();
}

export function getMappingActions(isSource: boolean) {
  return initializationService.cfg.fieldActionService.getActionsAppliesToField(
    initializationService.cfg.mappings!.activeMapping!,
    isSource,
    Multiplicity.ONE_TO_ONE,
  );
}
export function getMultiplicityActions(mapping: MappingModel) {
  if (mapping.transition.mode === TransitionMode.ONE_TO_MANY) {
    return initializationService.cfg.fieldActionService.getActionsAppliesToField(
      mapping,
      true,
      Multiplicity.ONE_TO_MANY,
    );
  } else if (mapping.transition.mode === TransitionMode.MANY_TO_ONE) {
    return initializationService.cfg.fieldActionService.getActionsAppliesToField(
      mapping,
      true,
      Multiplicity.MANY_TO_ONE,
    );
  } else {
    return [];
  }
}
export function getMultiplicityActionDelimiters() {
  return TransitionModel.delimiterModels;
}

export function handleActionChange(
  action: FieldAction,
  definition: FieldActionDefinition,
) {
  action.argumentValues = []; // Invalidate the previously selected field action arguments.
  definition.populateFieldAction(action);

  // If the field action configuration predefines argument values then populate the fields with
  // default values.  Needed to support pull-down menus in action argument definitions.
  if (
    action.argumentValues.values() &&
    action.argumentValues.length > 0 &&
    definition.arguments[0] &&
    definition.arguments[0].values &&
    definition.arguments[0].values.length > 0
  ) {
    for (let i = 0; i < action.argumentValues.length; i++) {
      action.argumentValues[i].value = definition.arguments[i].values![i];
    }
  }
  initializationService.cfg.mappingService.notifyMappingUpdated();
}

/**
 * Process a mapped field index change.  The source field is represented by
 * currentIndex.  The target index is represented either by a number or by a
 * mapped field (depending on whether the index was physically modified or
 * d&d modified).
 *
 * @param isSource
 * @param currentIndex
 * @param target
 */
export function handleIndexChange(
  isSource: boolean,
  currentIndex: number,
  target: number | Field,
) {
  const cfg = ConfigModel.getConfig();
  const activeMapping = cfg.mappings?.activeMapping;
  if (!activeMapping) {
    return;
  }
  const sourceField = activeMapping.getMappedFieldForIndex(
    '' + (currentIndex + 1),
    isSource,
  );
  if (!sourceField) {
    return;
  }
  let newIndex: number | null = 0;

  // If the target is an actual index value then check for the need to add padding.
  if (typeof target === 'number') {
    newIndex = target;
    if (target <= 0 || !activeMapping) {
      return;
    }
    const mappedFields = activeMapping.getMappedFields(isSource);
    const maxIndex = mappedFields.length;
    if (target > maxIndex) {
      // Add place-holders for each index value between the previous max index
      // and the insertion index.
      cfg.mappingService.addPlaceholders(
        target - mappedFields.length,
        activeMapping,
        maxIndex,
        isSource,
      );
    }
    // If the target is a dropped field then extract the mapped field to determine the index.
  } else {
    const field = activeMapping.getMappedFieldForField(target!);
    if (!field) {
      return;
    }
    newIndex = activeMapping.getIndexForMappedField(field);
  }
  cfg.mappingService.moveMappedFieldTo(activeMapping, sourceField, newIndex!);
}

export function handleNewTransformation(isSource: boolean, index: number) {
  const cfg = ConfigModel.getConfig();
  const activeMapping = cfg.mappings?.activeMapping;
  if (!activeMapping) {
    return;
  }
  const field = activeMapping.getMappedFieldForIndex(
    '' + (index + 1),
    isSource,
  );
  if (!field) {
    return;
  }
  const action: FieldAction = new FieldAction();
  const availableActions = getMappingActions(isSource);
  availableActions[0].populateFieldAction(action);
  field.actions.push(action);
  cfg.mappingService.notifyMappingUpdated();
}

export function handleTransformationChange(
  isSource: boolean,
  index: number,
  currentTransformationIndex: number,
  newTransformationName: string,
) {
  const cfg = ConfigModel.getConfig();
  const activeMapping = cfg.mappings?.activeMapping;
  if (!activeMapping) {
    return;
  }
  const field = activeMapping.getMappedFieldForIndex(
    '' + (index + 1),
    isSource,
  );
  if (!field) {
    return;
  }
  const action = field.actions[currentTransformationIndex];
  const newAction = getMappingActions(isSource).find(
    (a) => a.name === newTransformationName,
  );

  if (action && newAction) {
    action.argumentValues = []; // Invalidate the previously selected field action arguments.
    newAction.populateFieldAction(action);

    // If the field action configuration predefines argument values then populate the fields with
    // default values.  Needed to support pull-down menus in action argument definitions.
    if (
      action.argumentValues.values() &&
      action.argumentValues.length > 0 &&
      newAction.arguments[0] &&
      newAction.arguments[0].values &&
      newAction.arguments[0].values.length > 0
    ) {
      for (let i = 0; i < action.argumentValues.length; i++) {
        action.argumentValues[i].value = newAction.arguments[i].values![i];
      }
    }
    cfg.mappingService.notifyMappingUpdated();
  }
}

export function handleTransformationArgumentChange(
  isSource: boolean,
  index: number,
  transformationIndex: number,
  argumentName: string,
  argumentValue: string,
) {
  const cfg = ConfigModel.getConfig();
  const activeMapping = cfg.mappings?.activeMapping;
  if (!activeMapping) {
    return;
  }
  const field = activeMapping.getMappedFieldForIndex(
    '' + (index + 1),
    isSource,
  );
  if (!field) {
    return;
  }
  const action = field.actions[transformationIndex];
  if (action) {
    action.setArgumentValue(argumentName, argumentValue);
    cfg.mappingService.notifyMappingUpdated();
  }
}

export function handleRemoveTransformation(
  isSource: boolean,
  index: number,
  transformationIndex: number,
) {
  const cfg = ConfigModel.getConfig();
  const activeMapping = cfg.mappings?.activeMapping;
  if (!activeMapping) {
    return;
  }
  const field = activeMapping.getMappedFieldForIndex(
    '' + (index + 1),
    isSource,
  );
  if (!field) {
    return;
  }
  const action = field.actions[transformationIndex];
  if (action) {
    CommonUtil.removeItemFromArray(action, field.actions);
    cfg.mappingService.notifyMappingUpdated();
  }
}

export function handleMultiplicityChange(action: FieldAction, name: string) {
  const cfg = ConfigModel.getConfig();
  const activeMapping = cfg.mappings?.activeMapping;
  if (!activeMapping) {
    return;
  }

  const newAction = getMultiplicityActions(activeMapping).find(
    (a) => a.name === name,
  );

  if (action && newAction) {
    action.argumentValues = []; // Invalidate the previously selected field action arguments.
    newAction.populateFieldAction(action);

    // If the field action configuration predefines argument values then populate the fields with
    // default values.  Needed to support pull-down menus in action argument definitions.
    if (
      action.argumentValues.values() &&
      action.argumentValues.length > 0 &&
      newAction.arguments[0] &&
      newAction.arguments[0].values &&
      newAction.arguments[0].values.length > 0
    ) {
      for (let i = 0; i < action.argumentValues.length; i++) {
        action.argumentValues[i].value = newAction.arguments[i].values![i];
      }
    }
    initializationService.cfg.mappingService.notifyMappingUpdated();
  }
}
export function handleMultiplicityArgumentChange(
  multiplicityFieldAction: FieldAction,
  argumentName: string,
  argumentValue: string,
) {
  const cfg = ConfigModel.getConfig();

  multiplicityFieldAction.setArgumentValue(argumentName, argumentValue);
  cfg.mappingService.notifyMappingUpdated();
}

export function selectMapping(mapping: IAtlasmapMapping) {
  initializationService.cfg.mappingService.selectMapping(mapping.mapping);
}

export function deselectMapping() {
  initializationService.cfg.mappingService.deselectMapping();
}
