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

import { NgModule, ModuleWithProviders, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, HttpClientXsrfModule, HttpXsrfTokenExtractor, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AlertModule, BsDropdownModule, TooltipModule, TypeaheadModule } from 'ngx-bootstrap';
import { LoggerModule, NGXLogger } from 'ngx-logger';
import { environment } from '../../../environments/environment';
import { DocumentManagementService } from './services/document-management.service';
import { MappingManagementService } from './services/mapping-management.service';
import { ErrorHandlerService } from './services/error-handler.service';
import { InitializationService } from './services/initialization.service';

import { DataMapperAppExampleHostComponent } from './components/data-mapper-example-host.component';
import { DataMapperAppComponent } from './components/app/data-mapper-app.component';
import { DataMapperErrorComponent } from './components/app/data-mapper-error.component';
import { ModalWindowComponent, EmptyModalBodyComponent } from './components/modal/modal-window.component';
import { ModalErrorWindowComponent } from './components/modal/modal-error-window.component';
import { ModalErrorDetailComponent } from './components/modal/modal-error-detail.component';
import { ToolbarComponent } from './components/toolbar/toolbar.component';
import { TemplateEditComponent } from './components/app/template-edit.component';
import { LineMachineComponent } from './components/app/line-machine.component';
import { ClassNameComponent } from './components/document/class-name.component';
import { CollapsableHeaderComponent } from './components/mapping-detail/collapsable-header.component';
import { DocumentDefinitionComponent } from './components/document/document-definition.component';
import { DocumentFieldDetailComponent } from './components/document/document-field-detail.component';
import { PropertyFieldEditComponent } from './components/app/property-field-edit.component';
import { ConstantFieldEditComponent } from './components/app/constant-field-edit.component';
import { FieldEditComponent } from './components/app/field-edit.component';
import { NamespaceEditComponent } from './components/app/namespace-edit.component';
import { MappingListComponent } from './components/mapping-list/mapping-list.component';
import { MappingListFieldComponent } from './components/mapping-list/mapping-list-field.component';
import { NamespaceListComponent } from './components/mapping-list/namespace-list.component';
import { MappingDetailComponent } from './components/mapping-detail/mapping-detail.component';
import { MappingFieldContainerComponent } from './components/mapping-detail/mapping-field-container.component';
import { MappingFieldDetailComponent } from './components/mapping-detail/mapping-field-detail.component';
import { MappingFieldActionComponent } from './components/mapping-detail/mapping-field-action.component';
import { MappingFieldActionArgumentComponent } from './components/mapping-detail/mapping-field-action-argument.component';
import { MappingSelectionComponent } from './components/mapping-detail/mapping-selection.component';
import { MappingSelectionSectionComponent } from './components/mapping-detail/mapping-selection-section.component';
import { LookupTableComponent } from './components/mapping-detail/lookup-table.component';
import { TransitionSelectionComponent } from './components/mapping-detail/transition-selection.component';
import { FocusDirective } from './common/focus.directive';
import { ExpressionComponent } from './components/toolbar/expression.component';

// export services/types for consumers of this module
export { ApiXsrfInterceptor, ApiHttpXsrfTokenExtractor } from './services/api-xsrf.service';
export { ErrorHandlerService } from './services/error-handler.service';
export { DocumentManagementService } from './services/document-management.service';
export { MappingManagementService } from './services/mapping-management.service';
export { InitializationService } from './services/initialization.service';
export { DocumentDefinition } from './models/document-definition.model';
export { MappingDefinition } from './models/mapping-definition.model';
export { DocumentType, InspectionType } from './common/config.types';
export { ConfigModel, DocumentInitializationModel } from './models/config.model';
export { MappingModel } from './models/mapping.model';
export { MappingSerializer } from './utils/mapping-serializer';

import { ToErrorIconClassPipe } from './common/to-error-icon-class.pipe';
import { ApiXsrfInterceptor, ApiHttpXsrfTokenExtractor } from './services/api-xsrf.service';
import {AppRoutingModule} from "../../app-routing.module";
import { FieldActionService } from './services/field-action.service';
import { FileManagementService } from './services/file-management.service';

export { DataMapperAppComponent } from './components/app/data-mapper-app.component';

export const typeaheadModuleForRoot: ModuleWithProviders = TypeaheadModule.forRoot();
export const tooltipModuleForRoot: ModuleWithProviders = TooltipModule.forRoot();
export const bsDropdownModuleForRoot: ModuleWithProviders = BsDropdownModule.forRoot();
export const httpClientXsrfModuleForRoot: ModuleWithProviders = HttpClientXsrfModule.withOptions(environment.xsrf);
export const alertModuleForRoot: ModuleWithProviders = AlertModule.forRoot();
export const loggerModuleForRoot: ModuleWithProviders = LoggerModule.forRoot(environment.ngxLoggerConfig);

// @dynamic
@NgModule({
  imports: [
    CommonModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    typeaheadModuleForRoot,
    tooltipModuleForRoot,
    bsDropdownModuleForRoot,
    httpClientXsrfModuleForRoot,
    alertModuleForRoot,
    AppRoutingModule
    alertModuleForRoot,
    loggerModuleForRoot,
  ],
  declarations: [
    DataMapperAppComponent,
    ClassNameComponent,
    DocumentDefinitionComponent,
    MappingDetailComponent,
    MappingFieldContainerComponent,
    ModalWindowComponent,
    ModalErrorWindowComponent,
    ModalErrorDetailComponent,
    DataMapperAppExampleHostComponent,
    MappingFieldActionComponent,
    MappingFieldActionArgumentComponent,
    MappingFieldDetailComponent,
    DocumentFieldDetailComponent,
    DataMapperErrorComponent,
    TransitionSelectionComponent,
    LineMachineComponent,
    MappingSelectionComponent,
    MappingSelectionSectionComponent,
    ToolbarComponent,
    LookupTableComponent,
    EmptyModalBodyComponent,
    FieldEditComponent,
    NamespaceEditComponent,
    PropertyFieldEditComponent,
    ConstantFieldEditComponent,
    CollapsableHeaderComponent,
    MappingListComponent,
    MappingListFieldComponent,
    NamespaceListComponent,
    TemplateEditComponent,
    ExpressionComponent,
    FocusDirective,
    ToErrorIconClassPipe
  ],
  exports: [
    DataMapperAppExampleHostComponent,
    ModalWindowComponent,
    ModalErrorWindowComponent,
    DataMapperAppComponent,
    AlertModule
  ],
  providers: [
    DocumentManagementService,
    MappingManagementService,
    ErrorHandlerService,
    InitializationService,
    FieldActionService,
    FileManagementService,
    NGXLogger,
  ],
  entryComponents: [
    MappingSelectionComponent,
    LookupTableComponent,
    EmptyModalBodyComponent,
    FieldEditComponent,
    NamespaceEditComponent,
    PropertyFieldEditComponent,
    ClassNameComponent,
    ConstantFieldEditComponent,
    TemplateEditComponent,
  ],
  bootstrap: [DataMapperAppExampleHostComponent],
  schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
})

export class DataMapperModule {
  static withInterceptor(): Array<ModuleWithProviders> {
    return [
      { ngModule: DataMapperModule,
        providers: [
          DocumentManagementService,
          MappingManagementService,
          ErrorHandlerService,
          FieldActionService,
          FileManagementService,
          InitializationService,
          {
            provide: HTTP_INTERCEPTORS,
            useClass: ApiXsrfInterceptor,
            multi: true
          },
        ],
      },
      { ngModule: DataMapperModule,
        providers: [
          DocumentManagementService,
          MappingManagementService,
          ErrorHandlerService,
          FieldActionService,
          FileManagementService,
          InitializationService,
          {
            provide: HttpXsrfTokenExtractor,
            useClass: ApiHttpXsrfTokenExtractor,
            multi: false
          },
        ],
      }
    ];
  }
}
