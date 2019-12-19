import { ConfigModel } from '../../models/config.model';
import { getDocDef } from '../document/document-util';
import { Field } from '../../models/field.model';

/**
 * Create a new mapping using the speciied source and target IDs.
 *
 * @param sourceId
 * @param targetId
 */
export function createMapping(sourceId: string, targetId: string): void {
  const cfg = ConfigModel.getConfig();
  const [uri, docType, docName, panel, uuid] = sourceId.split(':');
  const isSource = (panel === 'source');
  const sourceField = getFieldByUUID(docName, cfg, isSource, uuid);

  if (!sourceField) {
    return;
  }
  cfg.mappingService.addNewMapping(sourceField, false);
  addToMapping(targetId, '');
}

/**
 * Add the specified field to the current mapping.
 *
 * @param fieldId - field identifier
 * @param mappingId - needed for multiple mapping files
 */
export function addToMapping(fieldId: string, _mappingId: string): void {
  const cfg = ConfigModel.getConfig();
  const [uri, docType, docName, panel, uuid] = fieldId.split(':');
  const isSource = (panel === 'source');
  const field = getFieldByUUID(docName, cfg, isSource, uuid);

  if (!field) {
    return;
  }
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