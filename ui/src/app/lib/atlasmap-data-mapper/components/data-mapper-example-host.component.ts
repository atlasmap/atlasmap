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

import { Component, ViewChild, OnInit, OnDestroy } from '@angular/core';
import { ConfigModel } from '../models/config.model';

import { ErrorHandlerService } from '../services/error-handler.service';
import { DocumentManagementService } from '../services/document-management.service';
import { MappingManagementService } from '../services/mapping-management.service';
import { InitializationService } from '../services/initialization.service';

import { DataMapperAppComponent } from './app/data-mapper-app.component';
import { environment } from '../../../../environments/environment';
import { Examples } from '../models/examples';
import {MappingIdentifierService} from '../services/mapping-identifier.service';

@Component({
  selector: 'data-mapper-example-host',
  template: '<data-mapper #dataMapperComponent></data-mapper>',
  providers: [MappingManagementService, ErrorHandlerService, DocumentManagementService, MappingIdentifierService],
})

export class DataMapperAppExampleHostComponent implements OnInit {

  @ViewChild('dataMapperComponent')
  dataMapperComponent: DataMapperAppComponent;

  constructor(private initializationService: InitializationService, private mappingIdentifierService: MappingIdentifierService) { }

  ngOnInit() {
    // initialize config information before initializing services
    const c: ConfigModel = this.initializationService.cfg;
    //
    c.mappingId = this.mappingIdentifierService.getCurrentMappingId();

    // store references to our services in our config model

    // initialize base urls for our service calls
    c.initCfg.baseJavaInspectionServiceUrl = environment.backendUrls.javaInspectionServiceUrl;
    c.initCfg.baseXMLInspectionServiceUrl = environment.backendUrls.xmlInspectionServiceUrl;
    c.initCfg.baseJSONInspectionServiceUrl = environment.backendUrls.jsonInspectionServiceUrl;
    c.initCfg.baseMappingServiceUrl = environment.backendUrls.atlasServiceUrl;

    if (environment.xsrf) {
      c.initCfg.xsrfHeaderName = environment.xsrf.headerName;
      c.initCfg.xsrfCookieName = environment.xsrf.cookieName;
      c.initCfg.xsrfDefaultTokenValue = environment.xsrf.defaultTokenValue;
    }

    // initialize data for our class path service call
    // note that quotes, newlines, and tabs are escaped
    c.initCfg.pomPayload = Examples.pom;
    c.initCfg.classPathFetchTimeoutInMilliseconds = 30000;
    // if classPath is specified, maven call to resolve pom will be skipped
    c.initCfg.classPath = null;

    // enable mapping preview mode for standalone
    c.initCfg.disableMappingPreviewMode = false;

    // enable the navigation bar and import/export for stand-alone
    c.initCfg.disableNavbar = false;

    // initialize system
    this.initializationService.initialize();
  }

}
