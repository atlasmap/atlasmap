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

// import { DocumentType } from '../common/config.types';
import { ConfigModel } from '../models/config.model';
// import { DocumentDefinition } from '../models/document-definition.model';

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

  getCSSClass(action: string) {
    if ('showDetails' == action) {
      return 'fa fa-exchange link' + (this.cfg.mappings.activeMapping ? ' selected' : '');
    } else if ('showLines' == action) {
      return 'fa fa-share-alt link' + (this.cfg.showLinesAlways ? ' selected' : '');
    } else if ('advancedMode' == action) {
      let clz = 'fa fa-cog link ';
      if (this.cfg.showLinesAlways || this.cfg.showTypes
        || !this.cfg.showMappedFields || !this.cfg.showUnmappedFields) {
        clz += 'selected';
      }
      return clz;
    } else if ('showMappingTable' == action) {
      return 'fa fa-table link' + (this.cfg.showMappingTable ? ' selected' : '');
    } else if ('showNamespaceTable' == action) {
      return 'fa fa-code link' + (this.cfg.showNamespaceTable ? ' selected' : '');
    } else if ('editTemplate' == action) {
      return 'fa fa-file-text-o link' + (this.cfg.mappings.templateExists() ? ' selected' : '');
    }
  }

  toolbarButtonClicked(action: string, event: any): void {
    event.preventDefault();
    if ('showDetails' == action) {
      if (this.cfg.mappings.activeMapping == null) {
        this.cfg.mappingService.addNewMapping(null);
        this.cfg.mappings.activeMapping.brandNewMapping = true;
      } else {
        this.cfg.mappingService.deselectMapping();
      }
    } else if ('editTemplate' == action) {
      this.editTemplate();
    } else if ('showLines' == action) {
      this.cfg.showLinesAlways = !this.cfg.showLinesAlways;
    } else if ('showTypes' == action) {
      this.cfg.showTypes = !this.cfg.showTypes;
    } else if ('showMappedFields' == action) {
      this.cfg.showMappedFields = !this.cfg.showMappedFields;
    } else if ('showUnmappedFields' == action) {
      this.cfg.showUnmappedFields = !this.cfg.showUnmappedFields;
    } else if ('addMapping' == action) {
      this.cfg.mappingService.addNewMapping(null);
    } else if ('showMappingTable' == action) {
      this.cfg.showMappingTable = !this.cfg.showMappingTable;
      if (this.cfg.showMappingTable) {
        this.cfg.showNamespaceTable = false;
      }
    } else if ('showNamespaceTable' == action) {
      this.cfg.showNamespaceTable = !this.cfg.showNamespaceTable;
      if (this.cfg.showNamespaceTable) {
        this.cfg.showMappingTable = false;
      }
    }

    // Use the initialization service to trigger the observable updateFromConfig method
    // in the parent data-mapper-app class.  This avoids materializing the lineMachine object
    // post-check.
    this.cfg.initializationService.systemInitializedSource.next();
  }

  ngOnInit() {
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
}
