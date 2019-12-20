import { ConfigModel } from '../../models/config.model';
import { Field } from '../../models/field.model';

/**
 * Create a new mapping using the speciied source and target IDs.
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