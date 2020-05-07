import {
  InspectionType,
  ConfigModel,
  DocumentDefinition,
  DataMapperUtil,
  ErrorInfo,
  ErrorScope,
  ErrorType,
  ErrorLevel,
} from "@atlasmap/core";
import { ClassNameComponent } from "./custom-classname";

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

/**
 * Enable the specified class package name and collection type in the
 * targetted panel for use in field mapping or custom transformations.
 * The user must have previously imported a JAR file containing the class.
 * The user-defined class will establish either an instance of mappable
 * fields or custom transformation methods.
 *
 * @param selectedClass
 * @param selectedCollection
 * @param isSource
 */
export function enableCustomClass(
  selectedClass: string,
  selectedCollection: string,
  isSource: boolean,
): void {
  const cfg = ConfigModel.getConfig();
  const classNameComponent = new ClassNameComponent(
    selectedClass,
    selectedCollection,
    isSource,
  );
  const docdef = cfg.initializationService.addJavaDocument(
    classNameComponent.userClassName,
    isSource,
    classNameComponent.userCollectionType,
    classNameComponent.userCollectionClassName,
  );
  docdef.name = classNameComponent.userClassName;
  docdef.isSource = isSource;
  docdef.updateFromMappings(cfg.mappings!);

  cfg.documentService
    .fetchClassPath()
    .toPromise()
    .then((classPath: string) => {
      cfg.initCfg.classPath = classPath;
      cfg.documentService
        .fetchDocument(docdef, cfg.initCfg.classPath)
        .toPromise()
        .then(async (doc: DocumentDefinition) => {
          // No fields indicate the user is attempting to enable a custom
          // field action class.  Remove the document from the panel since
          // it has no fields.
          if (doc.fields.length === 0) {
            // Make any custom field actions active.
            await cfg.fieldActionService
              .fetchFieldActions()
              .catch((error: any) => {
                cfg.errorService.addError(
                  new ErrorInfo({
                    message: error,
                    level: ErrorLevel.ERROR,
                    scope: ErrorScope.APPLICATION,
                    type: ErrorType.INTERNAL,
                  }),
                );
              });

            if (doc.isSource) {
              DataMapperUtil.removeItemFromArray(doc, cfg.sourceDocs);
            } else {
              DataMapperUtil.removeItemFromArray(doc, cfg.targetDocs);
            }
          }
          await cfg.mappingService.notifyMappingUpdated();
          await cfg.fileService.exportMappingsCatalog("");
        })
        .catch((error: any) => {
          if (error.status === 0) {
            cfg.errorService.addError(
              new ErrorInfo({
                message: `Unable to fetch the Java class document '${docdef.name}' from the runtime service.`,
                level: ErrorLevel.ERROR,
                scope: ErrorScope.APPLICATION,
                type: ErrorType.INTERNAL,
                object: error,
              }),
            );
          } else {
            cfg.errorService.addError(
              new ErrorInfo({
                message: `Could not load the Java class document '${docdef.id}'`,
                level: ErrorLevel.ERROR,
                scope: ErrorScope.APPLICATION,
                type: ErrorType.INTERNAL,
                object: error,
              }),
            );
          }
        });
    })
    .catch((error: any) => {
      if (error.status === 0) {
        cfg.errorService.addError(
          new ErrorInfo({
            message:
              "Fatal network error: Could not connect to AtlasMap design runtime service.",
            level: ErrorLevel.ERROR,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.INTERNAL,
            object: error,
          }),
        );
      } else {
        cfg.errorService.addError(
          new ErrorInfo({
            message: "Could not load the Java class path.",
            level: ErrorLevel.ERROR,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.INTERNAL,
            object: error,
          }),
        );
      }
    });
}
