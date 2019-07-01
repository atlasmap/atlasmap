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
import { Injectable } from '@angular/core';
import { ConfigModel } from '../models/config.model';
import { NGXLogger } from 'ngx-logger';
import { ErrorHandlerService } from './error-handler.service';
import { FieldActionDefinition, FieldActionArgument } from '../models/field-action.model';
import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MappingModel } from '../models/mapping.model';

@Injectable()
export class FieldActionService {
  cfg: ConfigModel = ConfigModel.getConfig();
  actionDefinitions: FieldActionDefinition[];
  isInitialized = false;
  private headers = new HttpHeaders(
    {'Content-Type': 'application/json; application/octet-stream',
     'Accept':       'application/json; application/octet-stream'});

  constructor(
    private errorService: ErrorHandlerService,
    private logger: NGXLogger,
    private http: HttpClient) {}

  async fetchFieldActions(): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      if (this.cfg.preloadedFieldActionMetadata) {
        this.actionDefinitions = [];
        for (const actionDetail of this.cfg.preloadedFieldActionMetadata.ActionDetails.actionDetail) {
          const fieldActionDefinition = this.extractFieldActionDefinition(actionDetail);
          this.actionDefinitions.push(fieldActionDefinition);
        }
        this.sortFieldActionDefinitions(this.actionDefinitions);
        this.isInitialized = true;
        resolve(true);
        return;
      }

      if (this.cfg.mappingService == null) {
        this.cfg.errorService.warn('Mapping service is not provided. Field Actions will not be used.', null);
        this.isInitialized = true;
        resolve(true);
        return;
      } else if (this.cfg.initCfg.baseMappingServiceUrl == null) {
        this.cfg.errorService.warn('Mapping service URL is not provided. Field Actions will not be used.', null);
        this.isInitialized = true;
        resolve(true);
        return;
      }

      // Fetch the field actions from the runtime service.
      this.doFetchFieldActions().toPromise()
        .then((fetchedActionConfigs: FieldActionDefinition[]) => {
          this.actionDefinitions = fetchedActionConfigs;
          this.isInitialized = true;
          resolve(true);
        }).catch((error: any) => {
          if (error.status === 0) {
            reject(`Fatal network error: Could not connect to AtlasMap design runtime service. (${error})`);
          } else {
            reject(`Could not load field action configs: ${error.status} ${error.statusText}`);
          }
          resolve(false);
        });
    });
  }

  getActionDefinitionForName(actionName: string): FieldActionDefinition {
    if (!this.actionDefinitions || !actionName) {
      return null;
    }
    for (const actionDef of this.actionDefinitions) {
      if (actionName === actionDef.name) {
        return actionDef;
      }
    }
    return null;
  }

  /**
   * Return the field action Definitions applicable to the specified field mapping pair.
   * @param mapping
   */
  getActionsAppliesToField(mapping: MappingModel, isSource: boolean = true): FieldActionDefinition[] {
    if (!mapping || !this.actionDefinitions) {
      return [];
    }
    return this.actionDefinitions.filter(d => d.appliesToField(mapping, isSource));
  }

  private doFetchFieldActions(): Observable<FieldActionDefinition[]> {
    return new Observable<FieldActionDefinition[]>((observer: any) => {
      let actionConfigs: FieldActionDefinition[] = [];
      const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'fieldActions';
      this.cfg.logger.trace('Field Action Config Request');
      this.http.get(url, { headers: this.headers }).toPromise().then((body: any) => {
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Field Action Config Response: ${JSON.stringify(body)}`);
        }
        if (body && body.ActionDetails
          && body.ActionDetails.actionDetail
          && body.ActionDetails.actionDetail.length) {
          for (const actionDetail of body.ActionDetails.actionDetail) {
            const fieldActionConfig = this.extractFieldActionDefinition(actionDetail);
            actionConfigs.push(fieldActionConfig);
          }
        }
        actionConfigs = this.sortFieldActionDefinitions(actionConfigs);
        observer.next(actionConfigs);
        observer.complete();
      }).catch((error: any) => {
        observer.error(error);
        observer.next(actionConfigs);
        observer.complete();
      });
    });
  }

  private extractFieldActionDefinition(actionDetail: any): FieldActionDefinition {
    if (this.cfg.isDebugEnabled()) {
      this.cfg.logger.debug(`Deserializing field action definition: ${JSON.stringify(actionDetail)}`);
    }

    const fieldActionDefinition = new FieldActionDefinition();
    fieldActionDefinition.name = actionDetail.name;
    fieldActionDefinition.isCustom = actionDetail.custom;
    fieldActionDefinition.sourceType = actionDetail.sourceType;
    fieldActionDefinition.targetType = actionDetail.targetType;
    fieldActionDefinition.method = actionDetail.method;
    fieldActionDefinition.multiplicity = actionDetail.multiplicity;
    fieldActionDefinition.serviceObject = actionDetail;

    for (const key of Object.keys(actionDetail.actionSchema.properties)) {
      const propertyObject = actionDetail.actionSchema.properties[key];
      if (key === '@type') {
        fieldActionDefinition.name = propertyObject.const;
        continue;
      }
      const argumentDefinition = new FieldActionArgument();
      argumentDefinition.name = key;
      argumentDefinition.type = propertyObject.type;
      argumentDefinition.values = propertyObject.enum;
      argumentDefinition.serviceObject = propertyObject;
      fieldActionDefinition.arguments.push(argumentDefinition);
    }

    return fieldActionDefinition;
  }

  private sortFieldActionDefinitions(definitions: FieldActionDefinition[]): FieldActionDefinition[] {
    const sortedActionDefinitions: FieldActionDefinition[] = [];
    if (definitions == null || definitions.length === 0) {
      return sortedActionDefinitions;
    }

    const defsByName: { [key: string]: FieldActionDefinition[]; } = {};
    const defNames: string[] = [];
    for (const fieldActionConfig of definitions) {
      const name: string = fieldActionConfig.name;
      let sameNamedDefs: FieldActionDefinition[] = defsByName[name];
      if (!sameNamedDefs) {
        sameNamedDefs = [];
        defNames.push(name);
      }
      sameNamedDefs.push(fieldActionConfig);
      defsByName[name] = sameNamedDefs;
    }

    defNames.sort();

    for (const name of defNames) {
      const sameNamedDefs: FieldActionDefinition[] = defsByName[name];
      for (const fieldActionDefinition of sameNamedDefs) {
        sortedActionDefinitions.push(fieldActionDefinition);
      }
    }
    return sortedActionDefinitions;
  }

}
