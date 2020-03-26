import { ConfigModel } from '../../models/config.model';
import { DocumentDefinition } from '../../models/document-definition.model';
import { Field } from '../../models/field.model';
import { propertyTypes, constantTypes } from '../../common/config.types';

export function createConstant(constValue: string, constType: string): void {
  const cfg = ConfigModel.getConfig();
  let field = cfg.constantDoc.getField(constValue);
  field = (!field) ? new Field() : field.copy();
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
  const field = cfg.constantDoc.getField(cfg.constantDoc.pathSeparator + constValue);
  if (!field) {
    return;
  }
  cfg.mappingService.removeFieldFromAllMappings(field);
  cfg.constantDoc.removeField(field);
  cfg.mappingService.notifyMappingUpdated();
}

export function editConstant(
  origValue: string,
  constValue: string,
  constType: string
  ): void
{
  const cfg = ConfigModel.getConfig();
  let field = cfg.constantDoc.getField(cfg.constantDoc.pathSeparator + origValue);
  if (!field) {
    return;
  }
  deleteConstant(origValue);
  createConstant(constValue, constType);
}

export function getConstantTypeIndex(constVal: string): number {
  const cfg = ConfigModel.getConfig();
  const field = cfg.constantDoc.getField(cfg.constantDoc.pathSeparator + constVal);
  if (!field) {
    return 0;
  }
  for (let i=0; i<constantTypes.length; i++) {
    if (constantTypes[i].includes(field.type)) {
      return i;
    }
  }
  return 0;
}

export function createProperty(propName: string, propValue: string, propType: string): void {
  const cfg = ConfigModel.getConfig();
  let field = cfg.propertyDoc.getField(propName);
  field = (!field) ? new Field() : field.copy();
  field.name = propName;
  field.value = propValue;
  field.type = propType;
  field.docDef = cfg.propertyDoc;
  field.userCreated = true;
  cfg.propertyDoc.addField(field);
  cfg.mappingService.notifyMappingUpdated();
}

export function deleteProperty(propName: string): void {
  const cfg = ConfigModel.getConfig();
  const field = cfg.propertyDoc.getField(cfg.propertyDoc.pathSeparator + propName.split(' ')[0]);
  if (!field) {
    return;
  }
  cfg.mappingService.removeFieldFromAllMappings(field);
  cfg.propertyDoc.removeField(field);
  cfg.mappingService.notifyMappingUpdated();
}

export function editProperty(propName: string, propValue: string, propType: string): void {
  const cfg = ConfigModel.getConfig();
  const field = cfg.propertyDoc.getField(cfg.propertyDoc.pathSeparator + propName.split(' ')[0]);
  if (!field) {
    return;
  }
  field.value = propValue;
  field.type = propType;
  cfg.propertyDoc.updateField(field, '');
  cfg.mappingService.notifyMappingUpdated();
}

export function getPropertyValue(propName: string): string {
  const cfg = ConfigModel.getConfig();
  const field = cfg.propertyDoc.getField(cfg.propertyDoc.pathSeparator + propName.split(' ')[0]);
  if (!field) {
    return '';
  }
  return field.value;
}

export function getPropertyTypeIndex(propName: string): number {
  const cfg = ConfigModel.getConfig();
  const field = cfg.propertyDoc.getField(cfg.propertyDoc.pathSeparator + propName);
  if (!field) {
    return 0;
  }
  for (let i=0; i<propertyTypes.length; i++) {
    if (propertyTypes[i].includes(field.type)) {
      return i;
    }
  }
  return 0;
}

/**
 * Create a new mapping using the specified source and target IDs.
 *
 * @param source
 * @param target
 */
export function createMapping(source: Field, target: Field): void {
  const cfg = ConfigModel.getConfig();
  cfg.mappingService.addNewMapping(source, false);
  addToCurrentMapping(target);
}

/**
 * Create a new mapping using the specified source and target IDs.
 *
 * @param source
 * @param target
 */
export function newMapping(): void {
  const cfg = ConfigModel.getConfig();
  cfg.mappingService.newMapping();
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
 * Return the Field object associated with the specified UUID in the specified document.
 *
 * @param docName
 * @param cfg
 * @param isSource
 * @param uuid
 */
export function getFieldByUUID(docName: string, cfg: ConfigModel, isSource: boolean,
  uuid: string): Field | undefined
{
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