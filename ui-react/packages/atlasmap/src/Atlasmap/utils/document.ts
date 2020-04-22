import {
  InspectionType,
  ConfigModel,
  DocumentDefinition,
  DataMapperUtil,
} from "@atlasmap/core";

/**
 * Import the specified user-defined document.
 *
 * @param selectedFile
 * @param cfg
 * @param isSource
 */
async function importDoc(
  selectedFile: any,
  cfg: ConfigModel,
  isSource: boolean,
): Promise<boolean> {
  return new Promise<boolean>(async (resolve) => {
    cfg.initCfg.initialized = false;
    cfg.initializationService.updateLoadingStatus(
      "Importing Document " + selectedFile.name,
    );
    cfg.documentService
      .processDocument(selectedFile, InspectionType.UNKNOWN, isSource)
      .then(() => {
        cfg.fileService.exportMappingsCatalog("");
        resolve(true);
      });
  });
}

/**
 * Remove a document from the UI and backend service.
 *
 * @param docDef
 * @param cfg
 */
export async function removeDocumentRef(
  docDef: DocumentDefinition,
  cfg: ConfigModel,
): Promise<boolean> {
  return new Promise<boolean>(async (resolve) => {
    cfg.mappingService.removeDocumentReferenceFromAllMappings(docDef.id);
    if (docDef.isSource) {
      DataMapperUtil.removeItemFromArray(docDef, cfg.sourceDocs);
    } else {
      DataMapperUtil.removeItemFromArray(docDef, cfg.targetDocs);
    }
    await cfg.mappingService.notifyMappingUpdated();
    await cfg.fileService.exportMappingsCatalog("");
    resolve(true);
  });
}

/**
 * Return the document definition associated with the specified document name.
 *
 * @param docName
 * @param cfg
 * @param isSource
 */
export function getDocDef(
  docName: string,
  cfg: ConfigModel,
  isSource: boolean,
): DocumentDefinition {
  for (const docDef of cfg.getDocs(isSource)) {
    const candidateDocName =
      docDef.getName(false) + "." + docDef.type.toLowerCase();
    if (candidateDocName.match(docName)) {
      return docDef;
    }
  }
  return (null as unknown) as DocumentDefinition;
}

/**
 * Import an instance or schema document into either the Source panel or Target
 * panel (JSON, XML, XSD).
 *
 * @param selectedFile
 * @param cfg
 * @param isSource
 */
export async function importInstanceSchema(
  selectedFile: File,
  cfg: ConfigModel,
  isSource: boolean,
) {
  const docDef = getDocDef(selectedFile.name, cfg, isSource);
  if (docDef) {
    await removeDocumentRef(docDef, cfg);
  }
  await importDoc(selectedFile, cfg, isSource);
}
