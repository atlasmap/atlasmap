import { initializationService } from "./ui";
import {
  ConfigModel,
  Field,
  constantTypes,
  propertyTypes,
  MappingModel,
  MappedField,
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
} from "@atlasmap/core";

export function createConstant(constValue: string, constType: string): void {
  const cfg = ConfigModel.getConfig();
  let field = cfg.constantDoc.getField(constValue);
  if (!field) {
    field = new Field();
  }
  field.name = constValue;
  field.value = constValue;
  field.type = constType;
  field.docDef = cfg.constantDoc;
  field.userCreated = true;
  cfg.constantDoc.addField(field);
  cfg.mappingService.notifyMappingUpdated();
}

export function deleteConstant(constValue: string): void {
  const cfg = ConfigModel.getConfig();
  const field = cfg.constantDoc.getField(
    cfg.constantDoc.pathSeparator + constValue,
  );
  if (!field) {
    return;
  }
  cfg.mappingService.removeFieldFromAllMappings(field);
  cfg.constantDoc.removeField(field);
  const activeMapping = cfg.mappings?.activeMapping;
  const expression = activeMapping?.transition?.expression;
  if (activeMapping && expression) {
    expression.updateFieldReference(activeMapping);
  }
  cfg.mappingService.notifyMappingUpdated();
}

export function editConstant(
  origValue: string,
  constValue: string,
  constType: string,
): void {
  const cfg = ConfigModel.getConfig();
  let field = cfg.constantDoc.getField(
    cfg.constantDoc.pathSeparator + origValue,
  );
  if (!field) {
    return;
  }
  deleteConstant(origValue);
  createConstant(constValue, constType);
}

export function getConstantType(constVal: string): string {
  const cfg = ConfigModel.getConfig();
  const field = cfg.constantDoc.getField(
    cfg.constantDoc.pathSeparator + constVal,
  );
  if (!field) {
    return "";
  }
  return field.type;
}

export function getConstantTypeIndex(constVal: string): number {
  const cfg = ConfigModel.getConfig();
  const field = cfg.constantDoc.getField(
    cfg.constantDoc.pathSeparator + constVal,
  );
  if (!field) {
    return 0;
  }
  for (let i = 0; i < constantTypes.length; i++) {
    if (constantTypes[i].includes(field.type)) {
      return i;
    }
  }
  return 0;
}

export function createProperty(
  propName: string,
  propType: string,
  propScope: string,
  isSource: boolean,
): void {
  const cfg = ConfigModel.getConfig();
  let field = isSource
    ? cfg.sourcePropertyDoc.getField(
        cfg.sourcePropertyDoc.pathSeparator + propName,
        propScope,
      )
    : cfg.targetPropertyDoc.getField(
        cfg.sourcePropertyDoc.pathSeparator + propName,
        propScope,
      );
  if (!field) {
    field = new Field();
  }
  field.name = propName;
  field.type = propType;
  field.scope = propScope;
  field.userCreated = true;

  if (isSource) {
    field.docDef = cfg.sourcePropertyDoc;
    cfg.sourcePropertyDoc.addField(field);
  } else {
    field.docDef = cfg.targetPropertyDoc;
    cfg.targetPropertyDoc.addField(field);
  }
  cfg.mappingService.notifyMappingUpdated();
}

export function deleteProperty(
  propName: string,
  propScope: string,
  isSource: boolean,
): void {
  const cfg = ConfigModel.getConfig();
  const field = isSource
    ? cfg.sourcePropertyDoc.getField(
        cfg.sourcePropertyDoc.pathSeparator + propName,
        propScope,
      )
    : cfg.targetPropertyDoc.getField(
        cfg.sourcePropertyDoc.pathSeparator + propName,
        propScope,
      );
  if (!field) {
    return;
  }
  cfg.mappingService.removeFieldFromAllMappings(field);
  if (isSource) {
    cfg.sourcePropertyDoc.removeField(field);
  } else {
    cfg.targetPropertyDoc.removeField(field);
  }
  const activeMapping = cfg.mappings?.activeMapping;
  const expression = activeMapping?.transition?.expression;
  if (activeMapping && expression) {
    expression.updateFieldReference(activeMapping);
  }
  cfg.mappingService.notifyMappingUpdated();
}

/**
 * When editing a property, the propName/propScope is needed to fetch the
 * existing field.  The newName and newScope may or may not be specified.
 *
 * @param propName
 * @param propType
 * @param propScope
 * @param isSource
 * @param newName
 * @param newScope
 */
export function editProperty(
  propName: string,
  propType: string,
  propScope: string,
  isSource: boolean,
  newName?: string,
  newScope?: string,
): void {
  const cfg = ConfigModel.getConfig();
  let field = isSource
    ? cfg.sourcePropertyDoc.getField(
        cfg.sourcePropertyDoc.pathSeparator + propName,
        propScope,
      )
    : cfg.targetPropertyDoc.getField(
        cfg.targetPropertyDoc.pathSeparator + propName,
        propScope,
      );
  if (!field) {
    return;
  }
  if (newName) {
    field.name = newName;
  }
  if (newScope) {
    field.scope = newScope;
  }
  field.type = propType;
  let originalKey = "";
  if (propScope.length > 0) {
    originalKey =
      cfg.targetPropertyDoc.pathSeparator + propName + "-" + propScope;
  }

  if (isSource) {
    cfg.sourcePropertyDoc.updateField(field, originalKey);
  } else {
    cfg.targetPropertyDoc.updateField(field, originalKey);
  }
  cfg.mappingService.notifyMappingUpdated();
}

export function getPropertyType(
  propName: string,
  propScope: string,
  isSource: boolean,
): string {
  const cfg = ConfigModel.getConfig();
  const field = isSource
    ? cfg.sourcePropertyDoc.getField(
        cfg.sourcePropertyDoc.pathSeparator + propName,
        propScope,
      )
    : cfg.targetPropertyDoc.getField(
        cfg.targetPropertyDoc.pathSeparator + propName,
        propScope,
      );
  if (!field) {
    return "";
  }
  return field.type;
}

export function getPropertyTypeIndex(
  propName: string,
  propScope: string,
  isSource: boolean,
): number {
  const cfg = ConfigModel.getConfig();
  const field = isSource
    ? cfg.sourcePropertyDoc.getField(
        cfg.sourcePropertyDoc.pathSeparator + propName,
        propScope,
      )
    : cfg.targetPropertyDoc.getField(
        cfg.targetPropertyDoc.pathSeparator + propName,
        propScope,
      );
  if (!field) {
    return 0;
  }
  for (let i = 0; i < propertyTypes.length; i++) {
    if (propertyTypes[i].includes(field.type)) {
      return i;
    }
  }
  return 0;
}

/**
 * Create a new mapping or modify the existing active mapping using the specified source and target IDs.
 *
 * @param source
 * @param target
 */
export function createMapping(source: Field | undefined, target?: Field): void {
  const cfg = ConfigModel.getConfig();
  const ms = initializationService.cfg.mappingService;

  if (target) {
    if (
      !cfg.mappings?.activeMapping &&
      (source?.partOfMapping || target.partOfMapping)
    ) {
      cfg.errorService.addError(
        new ErrorInfo({
          message: `Unable to map '${source!.name}/${
            target.name
          }'.  Please select a mapping before adding to it.`,
          level: ErrorLevel.INFO,
          scope: ErrorScope.MAPPING,
          type: ErrorType.USER,
        }),
      );
      return;
    }
    if (cfg.mappings?.activeMapping) {
      const exclusionReason = ms.getFieldSelectionExclusionReason(
        cfg.mappings?.activeMapping!,
        target,
      );
      if (exclusionReason !== null) {
        cfg.errorService.addError(
          new ErrorInfo({
            message: `The field '${target.name}' cannot be selected, ${exclusionReason}.`,
            level: ErrorLevel.ERROR,
            mapping: cfg.mappings?.activeMapping!,
            scope: ErrorScope.MAPPING,
            type: ErrorType.USER,
          }),
        );
        return;
      }
      if (
        source &&
        target.partOfMapping &&
        cfg.mappings.activeMapping.targetFields[0]!.field!.path === target!.path
      ) {
        addToCurrentMapping(source);
        return;
      }
      if (
        source &&
        source.partOfMapping &&
        cfg.mappings.activeMapping.sourceFields[0]!.field!.path === source!.path
      ) {
        addToCurrentMapping(target);
        return;
      }
    }
  }
  if (source) {
    cfg.mappingService.addNewMapping(source, false);
  } else {
    cfg.mappingService.newMapping();
  }
  if (target) {
    addToCurrentMapping(target);
  }
}

/**
 * Create a new mapping.
 */
export function newMapping(): void {
  const cfg = ConfigModel.getConfig();
  cfg.mappingService.newMapping();
}

/**
 * Removes a mapping.
 */
export function removeMapping(mappingModel: MappingModel): void {
  ConfigModel.getConfig().mappingService.removeMapping(mappingModel);
}

/**
 * Add the specified field to the current mapping.
 *
 * @param field
 */
export function addToCurrentMapping(field: Field): void {
  const cfg = ConfigModel.getConfig();
  cfg.mappingService.fieldSelected(field);
}

/**
 * Remove the specified field from the current mapping.
 *
 * @param field
 */
export function removeFromCurrentMapping(field: Field): void {
  const cfg = ConfigModel.getConfig();
  const mapping = cfg.mappings?.activeMapping;
  if (mapping) {
    const mappedField = mapping.getMappedFieldForField(field);
    if (mappedField) {
      mapping.removeMappedField(mappedField);
      cfg.mappingService.updateMappedField(mapping);
    }
  }
}

/**
 * Add the specified field to the current mapping.
 *
 * @param field
 */
export function removeMappedFieldFromCurrentMapping(field: MappedField): void {
  const cfg = ConfigModel.getConfig();
  const mapping = cfg.mappings?.activeMapping;
  if (mapping && field) {
    mapping.removeMappedField(field);
    cfg.mappingService.updateMappedField(mapping);
  }
}

/**
 * Return the Field object associated with the specified UUID in the specified document.
 *
 * @param docName
 * @param cfg
 * @param isSource
 * @param uuid
 */
export function getFieldByUUID(
  docName: string,
  cfg: ConfigModel,
  isSource: boolean,
  uuid: string,
): Field | undefined {
  const docDef = cfg.getDocForIdentifier(docName, isSource);
  if (docDef === null) {
    return;
  }
  for (const field of docDef.getAllFields()) {
    if (field.uuid === uuid) {
      return field;
    }
  }
  return undefined;
}
