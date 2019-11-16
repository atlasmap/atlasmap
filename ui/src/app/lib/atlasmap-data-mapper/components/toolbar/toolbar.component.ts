/*
Copyright (C) 2017 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
import { Component, Input, OnInit, ViewChild } from '@angular/core';

import { InspectionType } from '../../common/config.types';
import { ConfigModel } from '../../models/config.model';
import { ModalWindowComponent } from '../modal//modal-window.component';
import { TemplateEditComponent } from '../app/template-edit.component';
import { ExpressionComponent } from './expression.component';
import { TransitionMode } from '../../models/transition.model';
import { ErrorScope, ErrorType, ErrorInfo, ErrorLevel } from '../../models/error.model';

@Component({
  selector: 'toolbar',
  templateUrl: './toolbar.component.html',
})

export class ToolbarComponent implements OnInit {
  @Input() cfg: ConfigModel;
  @Input() modalWindow: ModalWindowComponent;

  @ViewChild('expressionComponent') expressionComponent: ExpressionComponent;

  targetSupportsTemplate = false;

  private fileData: any;
  private mappingsFileName: string;
  private reader: FileReader;

  /**
   * Perform an asynchronous read of a local file, returning a byte array for the file content.
   *
   * @param fileContent
   */
  async readFile(fileContent: Blob): Promise<Uint8Array> {
    return new Promise<Uint8Array>((resolve, reject) => {
      this.reader.onload = (event: any) => {
        const arrayBuffer: any = this.reader.result;
        const bytes = new Uint8Array(arrayBuffer);
        resolve(bytes);
      };
      this.reader.readAsArrayBuffer(fileContent);
    });
  }

  /**
   * The user has imported a file (mapping catalog or Java archive).  Process accordingly.
   *
   * @param event
   */
  processImportedFile(event) {
    const userFile = event.target.files[0];
    const userFileComps = userFile.name.split('.');
    const userFileSuffix: string = userFileComps[userFileComps.length - 1].toUpperCase();

    if (userFileSuffix === 'ADM') {
        this.cfg.errorService.resetAll();

        // Clear out current user documents from the runtime service before processing the imported ADM.
        this.cfg.fileService.resetMappings().toPromise().then( async() => {
          this.cfg.fileService.resetLibs().toPromise().then( async() => {
            await this.processMappingsCatalog(userFile);
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
    } else if (userFileSuffix === 'JAR') {
      this.cfg.documentService.processDocument(event.target.files[0], InspectionType.JAVA_CLASS, false);
    }

    event.srcElement.value = null;
  }

  /**
   * A user has selected a compressed mappings catalog (ZIP) file to be imported into the canvas.
   *
   * @param selectedFile
   */
  async processMappingsCatalog(selectedFile: any) {
    this.cfg.initializationService.updateLoadingStatus('Importing AtlasMap Catalog');
    await this.cfg.fileService.importADMCatalog(selectedFile);
  }

  getFileSuffix() {
    return '.adm,.jar';
  }

  getCSSClass(action: string) {
    if (this.cfg.mappings !== null) {
      if ('showDetails' === action) {
        return 'fa fa-exchange link' + (this.cfg.mappings.activeMapping ? ' selected' : '');
      } else if ('editTemplate' === action) {
          return 'fa fa-file-text-o link' + (this.cfg.mappings.templateExists() ? ' selected' : '');
      }
    }
    if ('showLines' === action) {
      return 'fa fa-share-alt link' + (this.cfg.showLinesAlways ? ' selected' : '');
    } else if ('showMappingTable' === action) {
      return 'fa fa-table link' + (this.cfg.showMappingTable ? ' selected' : '');
    } else if ('showNamespaceTable' === action) {
      return 'fa fa-code link' + (this.cfg.showNamespaceTable ? ' selected' : '');
    } else if ('importMappings' === action) {
      return 'pficon pficon-import link';
    } else if ('exportMappings' === action) {
      return 'pficon pficon-export link';
    } else if ('enableExpression' === action) {
      return (this.cfg.mappings && this.cfg.mappings.activeMapping
          && this.cfg.mappings.activeMapping.transition
          && this.cfg.mappings.activeMapping.transition.enableExpression ? ' selected' : '');
    }
  }

  getEditorSettingsCSSClass(open: boolean) {
    return 'fa fa-cog link ' + (open ? ' selected' : '');
  }

  toolbarButtonClicked(action: string, event: any): void {
    event.preventDefault();
    if ('showDetails' === action) {
      if (this.cfg.showMappingDetailTray) {
        this.cfg.showMappingDetailTray = false;
      } else {
        this.cfg.showMappingDetailTray = true;
      }
    } else if ('editTemplate' === action) {
      this.editTemplate();
    } else if ('showLines' === action) {
      this.cfg.showLinesAlways = !this.cfg.showLinesAlways;
    } else if ('showTypes' === action) {
      this.cfg.showTypes = !this.cfg.showTypes;
    } else if ('showMappedFields' === action) {
      this.cfg.showMappedFields = !this.cfg.showMappedFields;
    } else if ('showUnmappedFields' === action) {
      this.cfg.showUnmappedFields = !this.cfg.showUnmappedFields;
    } else if ('addMapping' === action) {
      this.cfg.mappingService.addNewMapping(null, false);
    } else if ('showMappingTable' === action) {
      this.cfg.showMappingTable = !this.cfg.showMappingTable;
      if (this.cfg.showMappingTable) {
        this.cfg.showNamespaceTable = false;
      }
    } else if ('showNamespaceTable' === action) {
      this.cfg.showNamespaceTable = !this.cfg.showNamespaceTable;
      if (this.cfg.showNamespaceTable) {
        this.cfg.showMappingTable = false;
      }
    } else if ('showMappingPreview' === action) {
      this.cfg.showMappingPreview = !this.cfg.showMappingPreview;
    } else if ('clearMappings' === action) {
        this.clearMappings();
    } else if ('resetAll' === action) {
      this.resetAll();
    } else if ('enableExpression') {
      this.cfg.mappingService.toggleExpressionMode();
    }
    // Use the initialization service to trigger the observable updateFromConfig method
    // in the parent data-mapper-app class.  This avoids materializing the lineMachine object
    // post-check.
    this.cfg.initializationService.systemInitializedSource.next();
  }

  ngOnInit() {
    this.mappingsFileName = '';
    this.fileData = null;
    this.reader = new FileReader();
    // Disable template until runtime supports it - https://github.com/atlasmap/atlasmap/issues/329
    // const targetDoc: DocumentDefinition = this.cfg.targetDocs[0];
    // this.targetSupportsTemplate = targetDoc && (targetDoc.type == DocumentType.XML || targetDoc.type == DocumentType.JSON);
  }

  /**
   * Establish a modal window popup and if confirmed clear all mappings.
   */
  private clearMappings(): void {
    this.modalWindow.reset();
    this.modalWindow.confirmButtonText = 'Clear All Mappings';
    this.modalWindow.headerText = 'Clear All Mappings?';
    this.modalWindow.message = 'Are you sure you want to clear all mappings?';
    this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
      this.cfg.errorService.resetAll();
      this.cfg.mappingService.removeAllMappings();
    };
    this.modalWindow.show();
  }

  /**
   * Establish a modal window popup and if confirmed remove all documents and imported JARs from
   * the server and reinitialize the DM.
   */
  private resetAll(): void {
    this.modalWindow.reset();
    this.modalWindow.confirmButtonText = 'Reset';
    this.modalWindow.headerText = 'Reset All Mappings and Imports?';
    this.modalWindow.message = 'Are you sure you want to reset all mappings and clear all imported documents?';
    this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
      this.cfg.errorService.resetAll();
      this.cfg.fileService.resetMappings().toPromise().then( async() => {
        this.cfg.mappings = null;
        this.cfg.fileService.resetLibs().toPromise().then( async() => {
          await this.cfg.initializationService.initialize();
        });
        this.cfg.clearDocs();
        await this.cfg.initializationService.initialize();
      }).catch((error: any) => {
        if (error.status === 0) {
          this.cfg.errorService.addError(new ErrorInfo({
            message: 'Fatal network error: Could not connect to AtlasMap design runtime service.',
            level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
        } else {
          this.cfg.errorService.addError(new ErrorInfo({message: 'Could not reset mapping definitions.',
            level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
        }
      });
    };
    this.modalWindow.show();
  }

  private editTemplate(): void {
    const self: ToolbarComponent = this;
    this.modalWindow.reset();
    this.modalWindow.confirmButtonText = 'Save';
    this.modalWindow.headerText = this.cfg.mappings.templateExists() ? 'Edit Template' : 'Add Template';
    this.modalWindow.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
      const templateComponent: TemplateEditComponent = mw.nestedComponent as TemplateEditComponent;
      templateComponent.templateText = this.cfg.mappings.templateText;
    };
    this.modalWindow.nestedComponentType = TemplateEditComponent;
    this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
      const templateComponent: TemplateEditComponent = mw.nestedComponent as TemplateEditComponent;
      this.cfg.mappings.templateText = templateComponent.templateText;
      self.cfg.mappingService.notifyMappingUpdated();
    };
    this.modalWindow.show();
  }

  /**
   * The user has clicked in the export file text wudget.
   *
   * @param event
   */
  handleExportClick(event) {
    if (event.preventDefault) {
        event.preventDefault();
    }
    if (event.stopPropagation) {
      event.stopPropagation();
    }
  }

  /**
   * The user has specified an AtlasMap mappings catalog file name into which the current live mappings and
   * support documents will be exported.
   *
   * @param event
   */
  handleMappingsInstanceName(event) {
      let filename = event.target.value;
      if (filename !== null && filename.length > 0) {

        // Tack on a .adm suffix if one wasn't already specified.
        if (filename.split('.').pop() !== 'adm') {
          filename = filename.concat('.adm');
        }
        this.mappingsFileName = filename;
      }
    }

  /**
   * The user has requested their current mappings be exported.  Use the mapping management
   * service to establish the file content and to push it down to the server.
   *
   * @param event
   */
  handleExportMapping(event) {
    let fileName = this.mappingsFileName;
    if (this.mappingsFileName.length === 0) {
        fileName = 'atlasmap-mapping-' + this.cfg.mappingId +'.adm';
    }
    this.cfg.fileService.exportMappingsCatalog(fileName);
  }

  /**
   * Cancel button - Export > Current Mapping
   */
  handleExportMappingCancel(): void {
    this.mappingsFileName = '';
  }

  conditionalMappingExpressionEnabled(): boolean {
    return (this.cfg.mappings && this.cfg.mappings.activeMapping &&
      this.cfg.mappings.activeMapping.transition &&
      this.cfg.mappings.activeMapping.transition.enableExpression);
  }

}
