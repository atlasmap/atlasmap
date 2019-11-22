import { InspectionType } from '../../common/config.types';
import { ConfigModel } from '../../models/config.model';
import { DocumentDefinition } from '../../models/document-definition.model';
import {
  ErrorScope,
  ErrorType,
  ErrorInfo,
  ErrorLevel,
} from '../../models/error.model';
import { MappingDefinition } from '../../models/mapping-definition.model';
import { DocumentManagementService } from '../../services/document-management.service';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { FileManagementService } from '../../services/file-management.service';

/**
 * A user has selected a compressed mappings catalog file to be imported into the canvas.
 *
 * @param selectedFile
 */
async function processMappingsCatalog(selectedFile: any, cfg: ConfigModel) {
  cfg.initializationService.updateLoadingStatus('Importing AtlasMap Catalog');
  await cfg.fileService.importADMCatalog(selectedFile);
}

/**
 * The user has imported a file (mapping catalog or Java archive).
 *
 * @param event
 */
export function importAtlasFile(selectedFile: File) {
  const cfg = ConfigModel.getConfig();
  const userFileComps = selectedFile.name.split('.');
  const userFileSuffix: string = userFileComps[
    userFileComps.length - 1
  ].toUpperCase();

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

