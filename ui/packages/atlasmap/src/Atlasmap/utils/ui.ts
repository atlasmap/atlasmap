import ky from "ky";
import { Observable } from "rxjs";

import {
  ConfigModel,
  DataMapperUtil,
  DocumentDefinition,
  DocumentManagementService,
  ErrorHandlerService,
  ErrorInfo,
  ErrorLevel,
  ErrorType,
  ExpressionModel,
  Field,
  FieldAction,
  FieldActionDefinition,
  FieldActionService,
  FileManagementService,
  InitializationService,
  MappedField,
  MappingDefinition,
  MappingManagementService,
  MappingModel,
  Multiplicity,
  NamespaceModel,
  TransitionMode,
  TransitionModel,
} from "@atlasmap/core";

import { ITransformationArgument, ITransformationSelectOption } from "../../UI";
import {
  IAtlasmapDocument,
  IAtlasmapField,
  IAtlasmapGroup,
  IAtlasmapMappedField,
  IAtlasmapMapping,
  IAtlasmapNamespace,
  INotification,
} from "../../Views";

const api = ky.create({ headers: { "ATLASMAP-XSRF-TOKEN": "awesome" } });

export const initializationService = new InitializationService(
  new DocumentManagementService(api),
  new MappingManagementService(api),
  new ErrorHandlerService(),
  new FieldActionService(api),
  new FileManagementService(api),
);

export function fromFieldToIFieldsGroup(field: Field): IAtlasmapGroup | null {
  const fields = field.children
    .map(fromFieldToIFields)
    .filter((f) => f) as IAtlasmapField[];
  return fields.length > 0 && field.visibleInCurrentDocumentSearch
    ? {
        id: `${field.docDef.uri || field.docDef.type}:${
          field.docDef.isSource ? "source" : "target"
        }:${field.path}`,
        name: field.name,
        type: field.type,
        isCollection: field.isCollection,
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
        id: `${field.docDef.uri || field.docDef.type}:${
          field.docDef.isSource ? "source" : "target"
        }:${field.path}`,
        name: field.getFieldLabel(false, false),
        type: field.type,
        path: field.path,
        amField: field,
        previewValue: field.value,
        mappings:
          (cfg.mappings
            ?.findMappingsForField(field)
            .map(fromMappingModelToImapping)
            .filter((d) => d) as IAtlasmapMapping[]) || [],
        hasTransformations: field.partOfTransformation,
        isCollection: field.isCollection,
        isConnected: partOfMapping,
        isDisabled: field.type === "COMPLEX" && !field.enumeration,
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
    id: `${mappedField.field!.docDef.uri || mappedField.field!.docDef.type}:${
      mappedField.field!.docDef.isSource ? "source" : "target"
    }:${mappedField.field!.path}`,
    name: mappedField.field!.getFieldLabel(false, false),
    type: mappedField.field!.type,
    path: mappedField.field!.path,
    previewValue: mappedField.field!.value,
    hasTransformations: false,
    mappings: [],
    isCollection: false,
    isConnected: false,
    isDisabled: false,
    amField: mappedField.field,
    transformations: mappedField.actions.map((a) => ({
      name: a.name,
      options: getMappingActions(mappedField.isSource()).map(
        (a): ITransformationSelectOption => ({
          name: DataMapperUtil.toDisplayable(a.name),
          value: a.name,
        }),
      ),
      arguments: a.definition.arguments.map(
        (av, idx): ITransformationArgument => ({
          label: DataMapperUtil.toDisplayable(av.name),
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
): INotification["variant"] {
  switch (level) {
    case ErrorLevel.INFO:
      return "info";
    case ErrorLevel.WARN:
      return "warning";
    case ErrorLevel.ERROR:
      return "danger";
    default:
      return "default";
  }
}

export function errorMessageToString(message: any): string {
  switch (typeof message) {
    case "string":
      return message;
    default:
      return message?.message || JSON.stringify(message);
  }
}

export function errorTypeToString(type: ErrorType): string {
  switch (type) {
    case ErrorType.INTERNAL:
      return "Internal";
    case ErrorType.USER:
      return "User";
    case ErrorType.VALIDATION:
      return "Validation";
    case ErrorType.PREVIEW:
      return "Preview";
    case ErrorType.FORM:
      return "Form";
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
  return initializationService.cfg.mappingService.executeFieldSearch(
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
  selectedField: string,
  newTextNode: any,
  atIndex: number,
  isTrailer: boolean,
) {
  const mapping = initializationService.cfg.mappings!.activeMapping;
  if (!mapping || !selectedField) {
    return;
  }
  let mappedField = mapping.getMappedFieldByName(selectedField, true);

  if (!mappedField) {
    // If the selected field was not part of the original mapping
    // and is complex then add it as a reference node.
    mappedField = mapping.getReferenceField(selectedField);

    if (mappedField) {
      mapping.transition!.expression!.addConditionalExpressionNode(
        mappedField,
        newTextNode.getUuid(),
        isTrailer ? newTextNode.str.length : atIndex,
      );
    } else {
      // Try adding the selected field to the active mapping.
      let field: Field | null = null;
      for (const doc of initializationService.cfg.getDocs(true)) {
        field = Field.getField(selectedField, doc.getAllFields());
        if (field) {
          break;
        }
      }
      if (field) {
        initializationService.cfg.mappingService.fieldSelected(
          field,
          true,
          newTextNode.getUuid(),
          isTrailer ? newTextNode.toText().length : atIndex,
        );
      }
    }
  } else {
    mapping.transition!.expression!.addConditionalExpressionNode(
      mappedField,
      newTextNode.getUuid(),
      isTrailer ? newTextNode.str.length : atIndex,
    );
  }
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
  const uuidNode = initializationService.cfg.mappings!.activeMapping!.transition.expression!.clearText(
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
) {
  initializationService.cfg.mappings!.activeMapping!.transition.expression!.removeToken(
    tokenPosition,
    offset,
  );
  initializationService.cfg.mappingService.notifyMappingUpdated();
}
export function onFieldPreviewChange(field: IAtlasmapField, value: string) {
  field.amField.value = value;
  initializationService.cfg.mappingService.notifyMappingUpdated();
}

export function toggleExpressionMode() {
  initializationService.cfg.mappingService.toggleExpressionMode();
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
    action.argumentValues.values &&
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

export function handleIndexChange(
  isSource: boolean,
  currentIndex: number,
  newIndex: number,
) {
  const cfg = ConfigModel.getConfig();
  const activeMapping = cfg.mappings?.activeMapping;
  if (newIndex <= 0 || !activeMapping) {
    return;
  }
  const field = activeMapping.getMappedFieldForIndex(
    "" + (currentIndex + 1),
    isSource,
  );
  if (!field) {
    return;
  }
  const mappedFields = activeMapping.getMappedFields(isSource);
  const targetIndex = mappedFields.length;
  if (newIndex > targetIndex) {
    // Add place-holders for each index value between the previous max index
    // and the insertion index.
    cfg.mappingService.addPlaceholders(
      newIndex - mappedFields.length,
      activeMapping,
      targetIndex,
      isSource,
    );
  }
  cfg.mappingService.moveMappedFieldTo(activeMapping, field, newIndex);
}

export function handleNewTransformation(isSource: boolean, index: number) {
  const cfg = ConfigModel.getConfig();
  const activeMapping = cfg.mappings?.activeMapping;
  if (!activeMapping) {
    return;
  }
  const field = activeMapping.getMappedFieldForIndex(
    "" + (index + 1),
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
  currentTransformationName: string,
  newTransformationName: string,
) {
  const cfg = ConfigModel.getConfig();
  const activeMapping = cfg.mappings?.activeMapping;
  if (!activeMapping) {
    return;
  }
  const field = activeMapping.getMappedFieldForIndex(
    "" + (index + 1),
    isSource,
  );
  if (!field) {
    return;
  }
  const action = field.actions.find(
    (a) => a.name === currentTransformationName,
  );
  const newAction = getMappingActions(isSource).find(
    (a) => a.name === newTransformationName,
  );

  if (action && newAction) {
    action.argumentValues = []; // Invalidate the previously selected field action arguments.
    newAction.populateFieldAction(action);

    // If the field action configuration predefines argument values then populate the fields with
    // default values.  Needed to support pull-down menus in action argument definitions.
    if (
      action.argumentValues.values &&
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

export function handleTransformationArgumentChange(
  isSource: boolean,
  index: number,
  transformationName: string,
  argumentName: string,
  argumentValue: string,
) {
  const cfg = ConfigModel.getConfig();
  const activeMapping = cfg.mappings?.activeMapping;
  if (!activeMapping) {
    return;
  }
  const field = activeMapping.getMappedFieldForIndex(
    "" + (index + 1),
    isSource,
  );
  if (!field) {
    return;
  }
  const action = field.actions.find((a) => a.name === transformationName);
  if (action) {
    action.setArgumentValue(argumentName, argumentValue);
    cfg.mappingService.notifyMappingUpdated();
  }
}

export function handleRemoveTransformation(
  isSource: boolean,
  index: number,
  transformationName: string,
) {
  const cfg = ConfigModel.getConfig();
  const activeMapping = cfg.mappings?.activeMapping;
  if (!activeMapping) {
    return;
  }
  const field = activeMapping.getMappedFieldForIndex(
    "" + (index + 1),
    isSource,
  );
  if (!field) {
    return;
  }
  const action = field.actions.find((a) => a.name === transformationName);
  if (action) {
    DataMapperUtil.removeItemFromArray(action, field.actions);
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
      action.argumentValues.values &&
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
