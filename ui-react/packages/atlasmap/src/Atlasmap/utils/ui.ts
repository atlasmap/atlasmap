import ky from "ky";
import { Observable } from "rxjs";

import {
  ConfigModel,
  DocumentDefinition,
  DocumentManagementService,
  ErrorHandlerService,
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
  TransitionMode,
  TransitionModel,
} from "@atlasmap/core";

import {
  IAtlasmapDocument,
  IAtlasmapMappedField,
  IAtlasmapMapping,
  IAtlasmapGroup,
  IAtlasmapField,
} from "../../Views";

const api = ky.create({ headers: { "ATLASMAP-XSRF-TOKEN": "awesome" } });

export const initializationService = new InitializationService(
  new DocumentManagementService(api),
  new MappingManagementService(api),
  new ErrorHandlerService(),
  new FieldActionService(api),
  new FileManagementService(api),
);

function fromFieldToIFieldsGroup(field: Field): IAtlasmapGroup | null {
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

function fromFieldToIFieldsNode(field: Field): IAtlasmapField | null {
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
      }
    : null;
}

function fromFieldToIFields(field: Field) {
  return field.children.length > 0
    ? fromFieldToIFieldsGroup(field)
    : fromFieldToIFieldsNode(field);
}

export function fromDocumentDefinitionToFieldGroup(
  def: DocumentDefinition,
): IAtlasmapDocument | null {
  const fields = def.fields
    .map(fromFieldToIFields)
    .filter((f) => f) as IAtlasmapField[];
  return def.visibleInCurrentDocumentSearch && fields.length > 0
    ? {
        id: def.id,
        fields: fields,
        name: def.name,
        type: def.type,
      }
    : null;
}

function fromMappedFieldToIMappingField(
  field: MappedField,
): IAtlasmapMappedField | null {
  if (!field.field) {
    return null;
  }
  return {
    id: `${field.field!.docDef.uri || field.field!.docDef.type}:${
      field.field!.docDef.isSource ? "source" : "target"
    }:${field.field!.path}`,
    name: field.field!.getFieldLabel(false, false),
    type: field.field!.type,
    previewValue: "",
    mappedField: field,
    hasTransformations: false,
    mappings: [],
    isCollection: false,
    isConnected: false,
    amField: field.field,
  };
}

export function fromMappingModelToImapping(m: MappingModel | null | undefined) {
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
): void {
  initializationService.cfg.mappingService.executeFieldSearch(
    initializationService.cfg,
    searchFilter,
    isSource,
  );
}

export function mappingExpressionAddField(
  selectedField: any,
  newTextNode: any,
  atIndex: number,
  isTrailer: boolean,
) {
  const mapping = initializationService.cfg.mappings!.activeMapping;
  if (!mapping || !selectedField) {
    return;
  }
  const mappedField = mapping.getMappedFieldForField(selectedField);

  // If the selected field was not part of the original mapping then add
  // it to the active mapping.
  if (mappedField === null) {
    initializationService.cfg.mappingService.fieldSelected(
      selectedField,
      true,
      newTextNode.getUuid(),
      isTrailer ? newTextNode.toText().length : atIndex,
    );
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
    !initializationService.cfg.mappings ||
    !initializationService.cfg.mappings!.activeMapping
  ) {
    return null;
  }
  return initializationService.cfg.mappings!.activeMapping.transition
    .expression!.expressionUpdated$;
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
export function onConditionalMappingExpressionEnabled() {
  return initializationService.cfg.mappingService.conditionalMappingExpressionEnabled();
}

export function onToggleExpressionMode() {
  initializationService.cfg.mappingService.toggleExpressionMode();
}
export function getMappingActions(isSource: boolean) {
  return initializationService.cfg.fieldActionService.getActionsAppliesToField(
    initializationService.cfg.mappings!.activeMapping!,
    isSource,
    Multiplicity.ONE_TO_ONE,
  );
}
export function getMultiplicityActions(mapping: any) {
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
export function selectMapping(mapping: IAtlasmapMapping) {
  initializationService.cfg.mappingService.selectMapping(mapping.mapping);
}
export function deselectMapping() {
  initializationService.cfg.mappingService.deselectMapping();
}
