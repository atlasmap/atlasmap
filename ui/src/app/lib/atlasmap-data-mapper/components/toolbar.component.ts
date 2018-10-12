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
import { Component, Input, OnInit } from '@angular/core';

import { ConfigModel } from '../models/config.model';
import { InitializationService } from '../services/initialization.service';

import { ModalWindowComponent } from './modal-window.component';
import { TemplateEditComponent } from './template-edit.component';

@Component({
  selector: 'toolbar',
  templateUrl: './toolbar.component.html',
})

export class ToolbarComponent implements OnInit {
  @Input() cfg: ConfigModel;
  @Input() modalWindow: ModalWindowComponent;
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
        const arrayBuffer = this.reader.result;
        const bytes = new Uint8Array(arrayBuffer);
        resolve(bytes);
      };
      this.reader.readAsArrayBuffer(fileContent);
    });
  }

  /**
   * Schema import button click.  Disable initialization to trigger the loading icon.
   */
  processClick(loadingStatus: string) {
    this.cfg.initCfg.initialized = false;
    this.cfg.initializationService.updateLoadingStatus(loadingStatus);
  }

  /* A user has selected a compressed mappings catalog file to be imported into the canvas.
  *
  * @param event
  */
  async processMappingsCatalog(event) {

    // Wait for the async read of the selected mappings doc to be completed.
    try {
      this.fileData = await this.readFile(new Blob([event.target.files[0]]));
    } catch (error) {
      this.cfg.errorService.mappingError('Unable to import the specified data mappings file: ' +
        event.target.files[0].name + '\n' + error.message, error);
      return;
    }

    // Inflate the buffer and push it to the server.
    try {
      this.cfg.initializationService.processMappingsCatalog(this.fileData);
      window.location.reload(true);
    } catch (error) {
      this.cfg.errorService.mappingError('Unable to decompress the aggregate mappings file: \n' + event.target.files[0].name +
       '\n' + error.message, error);
      return;
    }
  }

  getCSSClass(action: string) {
    if ('showDetails' === action) {
      return 'fa fa-exchange link' + (this.cfg.mappings.activeMapping ? ' selected' : '');
    } else if ('showLines' === action) {
      return 'fa fa-share-alt link' + (this.cfg.showLinesAlways ? ' selected' : '');
    } else if ('showMappingTable' === action) {
      return 'fa fa-table link' + (this.cfg.showMappingTable ? ' selected' : '');
    } else if ('showNamespaceTable' === action) {
      return 'fa fa-code link' + (this.cfg.showNamespaceTable ? ' selected' : '');
    } else if ('editTemplate' === action) {
      return 'fa fa-file-text-o link' + (this.cfg.mappings.templateExists() ? ' selected' : '');
    } else if ('importMappings' === action) {
      return 'pficon pficon-import link';
    } else if ('exportMappings' === action) {
      return 'pficon pficon-export link';
    }
  }

  getEditorSettingsCSSClass(open: boolean) {
    return 'fa fa-cog link ' + (open ? ' selected' : '');
  }

  toolbarButtonClicked(action: string, event: any): void {
    event.preventDefault();
    if ('showDetails' === action) {
      if (this.cfg.mappings.activeMapping == null) {
        this.cfg.mappingService.addNewMapping(null, false);
        this.cfg.mappings.activeMapping.brandNewMapping = true;
      } else {
        this.cfg.mappingService.deselectMapping();
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
      self.cfg.mappingService.saveCurrentMapping();
    };
    this.modalWindow.show();
  }

  /**
   * The user has specified an AtlasMap mappings catalog file name into which the current live mappings and
   * support documents will be exported.
   *
   * @param event
   */
  handleMappingsInstanceName(event) {
      let filename = event.target.value;
      if (filename !== null || filename.length > 0) {

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
    this.cfg.mappingService.exportMappingsCatalog(this.mappingsFileName);
  }

  /**
   * Cancel button - Export > Current Mapping
   */
  handleExportMappingCancel(): void {
    this.mappingsFileName = '';
  }
}
