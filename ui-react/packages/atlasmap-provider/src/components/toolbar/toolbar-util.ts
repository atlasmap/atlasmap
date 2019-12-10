import { InspectionType } from '../../common/config.types';
import { ConfigModel } from '../../models/config.model';
import {
  ErrorScope,
  ErrorType,
  ErrorInfo,
  ErrorLevel,
} from '../../models/error.model';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { importInstanceSchema, getDocDef, removeDocumentRef } from '../../components/document/document-util';

/**
 * Delete the specified source or target document.
 *
 * @param fileName - document name to delete
 * @param isSource - source or target panel
 */
export async function deleteAtlasFile(fileName: string, isSource: boolean) {
  const cfg = ConfigModel.getConfig();
  const docDef = getDocDef(fileName, cfg, isSource);
  await removeDocumentRef(docDef, cfg);
  cfg.initializationService.updateInitComplete();
}

/**
 * The user has requested their current mappings be exported.  Use the mapping management
 * service to establish the file content and to push it down to the server.
 *
 * @param fileName - user-specified catalog file name
 */
export function exportAtlasFile(fileName: string) {
  const cfg = ConfigModel.getConfig();

  // Tack on a .adm suffix if one wasn't already specified.
  if (fileName.split('.').pop() !== 'adm') {
    fileName = fileName.concat('.adm');
  }
  cfg.fileService.exportMappingsCatalog(fileName);
}

/**
 * A user has selected a compressed mappings catalog file to be imported into the canvas.
 *
 * @param selectedFile - user-specified ADM catalog file.
 * @param cfg
 */
async function processMappingsCatalog(selectedFile: any, cfg: ConfigModel) {
  cfg.initializationService.updateLoadingStatus('Importing AtlasMap Catalog');
  await cfg.fileService.importADMCatalog(selectedFile);
}

/**
 * Import an ADM catalog file or a user JAR file.
 *
 * @param selectedFile
 * @param userFileSuffix
 * @param cfg
 */
function importAtlasGlobalFile(selectedFile: File, userFileSuffix: string, cfg: ConfigModel) {
  if (userFileSuffix === 'ADM') {
    cfg.errorService.resetAll();

    // Clear out current user documents from the backend service before processing the
    // imported ADM.
    cfg.fileService
      .resetMappings()
      .toPromise()
      .then(async () => {
        cfg.fileService
          .resetLibs()
          .toPromise()
          .then(async () => {
            await processMappingsCatalog(selectedFile, cfg);
          });
      })
      .catch((error: any) => {
        if (error.status === 0) {
          cfg.errorService.addError(
            new ErrorInfo({
              message:
                'Fatal network error: Could not connect to AtlasMap design runtime service.',
              level: ErrorLevel.ERROR,
              scope: ErrorScope.APPLICATION,
              type: ErrorType.INTERNAL,
              object: error,
            })
          );
        } else {
          cfg.errorService.addError(
            new ErrorInfo({
              message: 'Could not reset document definitions before import.',
              level: ErrorLevel.ERROR,
              scope: ErrorScope.APPLICATION,
              type: ErrorType.INTERNAL,
              object: error,
            })
          );
        }
      });
  } else if (userFileSuffix === 'JAR') {
    cfg.documentService.processDocument(
      selectedFile,
      InspectionType.JAVA_CLASS,
      false
    );
  }
}

/**
 * The user has imported a file (mapping catalog, Java archive or source/target
 * level specific instance or schema).
 *
 * @param selectedFile - File object representing the file the user selected.
 * @param isSource - true if selected file is associated with the Source panel,
 *                   false otherwise
 */
export function importAtlasFile(selectedFile: File, isSource: boolean) {
  const cfg = ConfigModel.getConfig();
  const userFileComps = selectedFile.name.split('.');
  const userFileSuffix: string = userFileComps[
    userFileComps.length - 1
  ].toUpperCase();

  if (userFileSuffix === 'ADM' || userFileSuffix === 'JAR') {
    importAtlasGlobalFile(selectedFile, userFileSuffix, cfg);
  } else {
    importInstanceSchema(selectedFile, cfg, isSource);
  }
}

/**
 * Remove all documents and imported JARs from the server.
 */
export function resetAtlasmap() {
  const cfg = ConfigModel.getConfig();
  cfg.errorService.resetAll();
  return cfg.fileService.resetMappings().toPromise().then( async() => {
    cfg.mappings = null;
    cfg.fileService.resetLibs().toPromise().then( async() => {
      await cfg.initializationService.initialize();
    });
    cfg.clearDocs();
    return cfg.initializationService.initialize();
  }).catch((error: any) => {
    if (error.status === 0) {
      cfg.errorService.addError(new ErrorInfo({
        message: 'Fatal network error: Could not connect to AtlasMap design runtime service.',
        level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
    } else {
      cfg.errorService.addError(new ErrorInfo({message: 'Could not reset mapping definitions.',
        level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
    }
  });
}

export function enableMappingPreview(enabled: boolean) {
  const cfg = ConfigModel.getConfig();

  if (enabled) {
    cfg.mappingService.enableMappingPreview();
  } else {
    cfg.mappingService.disableMappingPreview();
  }
}