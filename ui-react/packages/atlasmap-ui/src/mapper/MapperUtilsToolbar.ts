// import { InspectionType } from '../../../../../ui/src/app/lib/atlasmap-data-mapper/common/config.types';
// import { ConfigModel } from '../../../../../ui/src/app/lib/atlasmap-data-mapper/models/config.model';
// import { ModalWindowComponent } from '../../../../../ui/src/app/lib/atlasmap-data-mapper/components/modal/modal-window.component';
// import { TemplateEditComponent } from '../../../../../ui/src/app/lib/atlasmap-data-mapper/components/app/template-edit.component';
// import { ExpressionComponent } from '../../../../../ui/src/app/lib/atlasmap-data-mapper/components/toolbar/expression.component';
// import { TransitionMode } from '../../../../../ui/src/app/lib/atlasmap-data-mapper/models/transition.model';
// import { ErrorScope, ErrorType, ErrorInfo, ErrorLevel } from '../../../../../ui/src/app/lib/atlasmap-data-mapper/models/error.model';
// import { ErrorHandlerService } from '../../../../../ui/dist/lib/src/app/lib/atlasmap-data-mapper';

export interface IProcessImportedFileArgs {
  selectedFile: File;
}

/**
 * User file import callback (ADM or Java archive).
 *
 * @param selectedFile - user selected File object
 */
export function processImportedFile({ selectedFile }: IProcessImportedFileArgs) {
  console.log('processImportedFile: ' + selectedFile.name);

  const userFileComps = selectedFile.name.split('.');
  const userFileSuffix: string = userFileComps[userFileComps.length - 1].toUpperCase();

  if (userFileSuffix === 'ADM') {
        /*
    const error: any = null;
    new ErrorInfo({
      message: 'This is a test of the AtlasMap error service.',
      level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, 
      object: error});

      this.cfg.errorService.resetAll();

      // Clear out current user documents from the runtime service before processing the imported ADM.
      this.cfg.fileService.resetMappings().toPromise().then( async() => {
        this.cfg.fileService.resetLibs().toPromise().then( async() => {
          await this.processMappingsCatalog(selectedFile);
        });
      }).catch((error: any) => {
        if (error.status === 0) {
          this.cfg.errorService.addError(new ErrorInfo({
            message: 'Fatal network error: Could not connect to AtlasMap design runtime service.',
            level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
        } else {
          this.cfg.errorService.addError(new ErrorInfo({
            message: 'Could not reset document definitions before import.',
            level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
        }
      });
      */
  } else if (userFileSuffix === 'JAR') {
    // this.cfg.documentService.processDocument(selectedFile, InspectionType.JAVA_CLASS, false);
  }
}
