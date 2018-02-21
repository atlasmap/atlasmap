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

import { Headers, Http, Response } from '@angular/http';

import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/operator/map';

import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { MappingModel, FieldMappingPair, MappedField } from '../models/mapping.model';
import { FieldActionConfig, FieldActionArgument } from '../models/transition.model';
import { MappingDefinition } from '../models/mapping-definition.model';
import { ErrorInfo, ErrorLevel } from '../models/error.model';

import { MappingSerializer } from './mapping-serializer.service';

import { DataMapperUtil } from '../common/data-mapper-util';

@Injectable()
export class MappingManagementService {
  cfg: ConfigModel;

  mappingUpdatedSource = new Subject<void>();
  mappingUpdated$ = this.mappingUpdatedSource.asObservable();

  saveMappingSource = new Subject<Function>();
  saveMappingOutput$ = this.saveMappingSource.asObservable();

  mappingSelectionRequiredSource = new Subject<Field>();
  mappingSelectionRequired$ = this.mappingSelectionRequiredSource.asObservable();

  private headers: Headers = new Headers();

  constructor(private http: Http) {
    this.headers.append('Content-Type', 'application/json');
  }

  initialize(): void {
    return;
  }

  findMappingFiles(filter: string): Observable<string[]> {
    return new Observable<string[]>((observer: any) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mappings' + (filter == null ? '' : '?filter=' + filter);
      DataMapperUtil.debugLogJSON(null, 'Mapping List Response', this.cfg.initCfg.debugMappingServiceCalls, url);
      this.http.get(url, { headers: this.headers }).toPromise()
        .then((res: Response) => {
          const body = res.json();
          DataMapperUtil.debugLogJSON(body, 'Mapping List Response', this.cfg.initCfg.debugMappingServiceCalls, url);
          const entries: any[] = body.StringMap.stringMapEntry;
          const mappingFileNames: string[] = [];
          for (const entry of entries) {
            mappingFileNames.push(entry.name);
          }
          observer.next(mappingFileNames);
          observer.complete();
        })
        .catch((error: any) => {
          observer.error(error);
          observer.complete();
        },
      );
    });
  }

  fetchMappings(mappingFileNames: string[], mappingDefinition: MappingDefinition): Observable<boolean> {
    return new Observable<boolean>((observer: any) => {
      if (mappingFileNames.length == 0) {
        observer.complete();
        return;
      }

      const baseURL: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/';
      const operations: any[] = [];
      for (const mappingName of mappingFileNames) {
        const url: string = baseURL + mappingName;
        DataMapperUtil.debugLogJSON(null, 'Mapping Service Request', this.cfg.initCfg.debugMappingServiceCalls, url);
        const operation = this.http.get(url).map((res: Response) => res.json());
        operations.push(operation);
      }
      Observable.forkJoin(operations).subscribe((data: any[]) => {
        if (!data) {
          observer.next(false);
          observer.complete();
          return;
        }
        for (const d of data) {
          DataMapperUtil.debugLogJSON(d, 'Mapping Service Response', this.cfg.initCfg.debugMappingServiceCalls, null);
          MappingSerializer.deserializeMappingServiceJSON(d, mappingDefinition, this.cfg);
        }

        this.notifyMappingUpdated();
        observer.next(true);
        observer.complete();
      },
        (error: any) => {
          observer.error(error);
          observer.complete();
        });
    });
  }

  saveCurrentMapping(): void {
    const activeMapping: MappingModel = this.cfg.mappings.activeMapping;
    if ((activeMapping != null) && (this.cfg.mappings.mappings.indexOf(activeMapping) == -1)) {
      this.cfg.mappings.mappings.push(activeMapping);
    }

    const newMappings: MappingModel[] = [];
    for (const mapping of this.cfg.mappings.mappings) {
      if (mapping.hasFullyMappedPair()) {
        newMappings.push(mapping);
      }
    }

    this.cfg.mappings.mappings = newMappings;

    this.saveMappingSource.next(null);
  }

  serializeMappingsToJSON(): any {
    return MappingSerializer.serializeMappings(this.cfg);
  }

  saveMappingToService(): void {
    const payload: any = this.serializeMappingsToJSON();
    const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping';
    DataMapperUtil.debugLogJSON(payload, 'Mapping Service Request', this.cfg.initCfg.debugMappingServiceCalls, url);
    this.http.put(url, JSON.stringify(payload), { headers: this.headers }).toPromise()
      .then((res: Response) => {
        DataMapperUtil.debugLogJSON(res, 'Mapping Service Response', this.cfg.initCfg.debugMappingServiceCalls, url);
      })
      .catch((error: any) => { this.handleError('Error occurred while saving mapping.', error); },
    );
  }

  handleMappingSaveSuccess(saveHandler: Function): void {
    if (saveHandler != null) {
      saveHandler();
    }
    this.notifyMappingUpdated();
  }

  removeMapping(mappingModel: MappingModel): void {
    const mappingWasSaved: boolean = this.cfg.mappings.removeMapping(mappingModel);
    if (mappingWasSaved) {
      const saveHandler: Function = (() => {
        this.deselectMapping();
      });
      this.saveMappingSource.next(saveHandler);
    } else {
      this.deselectMapping();
    }
  }

  removeMappedPair(fieldPair: FieldMappingPair): void {
    this.cfg.mappings.activeMapping.removeMappedPair(fieldPair);
    if (this.cfg.mappings.activeMapping.fieldMappings.length == 0) {
      this.deselectMapping();
    } else {
      this.notifyMappingUpdated();
    }
    this.saveCurrentMapping();
  }

  addMappedPair(): FieldMappingPair {
    const fieldPair: FieldMappingPair = new FieldMappingPair();
    this.cfg.mappings.activeMapping.fieldMappings.push(fieldPair);
    this.notifyMappingUpdated();
    this.saveCurrentMapping();
    return fieldPair;
  }

  updateMappedField(fieldPair: FieldMappingPair): void {
    fieldPair.updateTransition();
    this.notifyMappingUpdated();
    this.saveCurrentMapping();
  }

  fieldSelected(field: Field): void {
    if (!field.isTerminal()) {
      field.docDef.populateChildren(field);
      field.docDef.updateFromMappings(this.cfg.mappings);
      field.collapsed = !field.collapsed;
      return;
    }

    let mapping: MappingModel = this.cfg.mappings.activeMapping;

    if (mapping != null
      && mapping.hasMappedFields(field.isSource())
      && !mapping.isFieldMapped(field, field.isSource())) {
      mapping = null;
    }

    if (mapping == null) {
      const mappingsForField: MappingModel[] = this.cfg.mappings.findMappingsForField(field);
      if (mappingsForField && mappingsForField.length > 1) {
        this.mappingSelectionRequiredSource.next(field);
        return;
      } else if (mappingsForField && mappingsForField.length == 1) {
        mapping = mappingsForField[0];
      }
    }

    if (mapping == null) {
      this.addNewMapping(field);
      return;
    }

    //check to see if field is a valid selection for this mapping
    const exclusionReason: string = mapping.getFieldSelectionExclusionReason(field);
    if (exclusionReason != null) {
      this.cfg.errorService.warn("The field '" + field.displayName + "' cannot be selected, " + exclusionReason + '.', null);
      return;
    }

    mapping.brandNewMapping = false;

    const latestFieldPair: FieldMappingPair = mapping.getCurrentFieldMapping();
    const lastMappedField: MappedField = latestFieldPair.getLastMappedField(field.isSource());
    if ((lastMappedField != null)) {
      lastMappedField.field = field;
    }
    latestFieldPair.updateTransition();
    this.selectMapping(mapping);
  }

  addNewMapping(selectedField: Field): void {
    this.deselectMapping();
    const mapping: MappingModel = new MappingModel();
    mapping.brandNewMapping = false;
    if (selectedField != null) {
      const fieldPair: FieldMappingPair = mapping.getFirstFieldMapping();
      fieldPair.getMappedFields(selectedField.isSource())[0].field = selectedField;
      fieldPair.updateTransition();
    }
    this.selectMapping(mapping);
  }

  selectMapping(mappingModel: MappingModel) {
    if (mappingModel == null) {
      this.deselectMapping();
      return;
    }
    this.cfg.mappings.activeMapping = mappingModel;
    this.cfg.showMappingDetailTray = true;
    for (const fieldPair of mappingModel.fieldMappings) {
      DocumentDefinition.selectFields(fieldPair.getAllFields());
    }
    this.cfg.mappings.initializeMappingLookupTable(mappingModel);
    this.saveCurrentMapping();
    this.notifyMappingUpdated();
  }

  deselectMapping(): void {
    this.cfg.showMappingDetailTray = false;
    this.cfg.mappings.activeMapping = null;
    for (const doc of this.cfg.getAllDocs()) {
      doc.clearSelectedFields();
    }
    this.notifyMappingUpdated();
  }

  validateMappings(): void {
    if (this.cfg.initCfg.baseMappingServiceUrl == null) {
      //validation service not configured.
      return;
    }
    const payload: any = MappingSerializer.serializeMappings(this.cfg);
    const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/validate';
    DataMapperUtil.debugLogJSON(payload, 'Validation Service Request', this.cfg.initCfg.debugValidationServiceCalls, url);
    this.http.put(url, payload, { headers: this.headers }).toPromise()
      .then((res: Response) => {
        DataMapperUtil.debugLogJSON(res, 'Validation Service Response', this.cfg.initCfg.debugValidationServiceCalls, url);
        const mapping: MappingModel = this.cfg.mappings.activeMapping;
        const body: any = res.json();
        const activeMappingErrors: ErrorInfo[] = [];
        const globalErrors: ErrorInfo[] = [];
        // Only update active mapping and global ones, since validateMappings() is always invoked when mapping is updated.
        // This should be eventually turned into mapping entry level validation.
        // https://github.com/atlasmap/atlasmap-ui/issues/116
        if (body && body.Validations && body.Validations.validation) {
          for (const validation of body.Validations.validation) {
            let level: ErrorLevel = ErrorLevel.VALIDATION_ERROR;
            if (validation.status === 'WARN') {
              level = ErrorLevel.WARN;
            } else if (validation.status === 'INFO') {
              level = ErrorLevel.INFO;
            }
            const errorInfo = new ErrorInfo(validation.message, level);
            if (!validation.scope || validation.scope != 'MAPPING' || !validation.id) {
              globalErrors.push(errorInfo);
            } else if (mapping && mapping.uuid && validation.id === mapping.uuid) {
              activeMappingErrors.push(errorInfo);
            }
          }
        }
        this.cfg.validationErrors = globalErrors;
        if (mapping) {
          mapping.validationErrors = activeMappingErrors;
        }
      })
      .catch((error: any) => {
        this.cfg.errorService.error('Error fetching validation data.', { 'error': error, 'url': url, 'request': payload });
      },
    );
  }

  fetchFieldActions(): Observable<FieldActionConfig[]> {
    return new Observable<FieldActionConfig[]>((observer: any) => {
      let actionConfigs: FieldActionConfig[] = [];
      const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'fieldActions';
      DataMapperUtil.debugLogJSON(null, 'Field Action Config Request', this.cfg.initCfg.debugFieldActionServiceCalls, url);
      this.http.get(url, { headers: this.headers }).toPromise()
        .then((res: Response) => {
          const body: any = res.json();
          DataMapperUtil.debugLogJSON(body, 'Field Action Config Response', this.cfg.initCfg.debugFieldActionServiceCalls, url);
          if (body && body.ActionDetails
            && body.ActionDetails.actionDetail
            && body.ActionDetails.actionDetail.length) {
            for (const svcConfig of body.ActionDetails.actionDetail) {
              const fieldActionConfig: FieldActionConfig = new FieldActionConfig();
              fieldActionConfig.name = svcConfig.name;
              fieldActionConfig.sourceType = svcConfig.sourceType;
              fieldActionConfig.targetType = svcConfig.targetType;
              fieldActionConfig.method = svcConfig.method;
              fieldActionConfig.serviceObject = svcConfig;

              if (svcConfig.parameters && svcConfig.parameters.property
                && svcConfig.parameters.property.length) {
                for (const svcProperty of svcConfig.parameters.property) {
                  const argumentConfig: FieldActionArgument = new FieldActionArgument();
                  argumentConfig.name = svcProperty.name;
                  argumentConfig.type = svcProperty.fieldType;
                  argumentConfig.serviceObject = svcProperty;
                  fieldActionConfig.arguments.push(argumentConfig);
                }
              }
              actionConfigs.push(fieldActionConfig);
            }
          }
          actionConfigs = this.sortFieldActionConfigs(actionConfigs);
          observer.next(actionConfigs);
          observer.complete();
        })
        .catch((error: any) => {
          observer.error(error);
          observer.next(actionConfigs);
          observer.complete();
        },
      );
    });
  }

  sortFieldActionConfigs(configs: FieldActionConfig[]): FieldActionConfig[] {
    const sortedActionConfigs: FieldActionConfig[] = [];
    if (configs == null || configs.length == 0) {
      return sortedActionConfigs;
    }

    const configsByName: { [key: string]: FieldActionConfig; } = {};
    const configNames: string[] = [];
    for (const fieldActionConfig of configs) {
      const name: string = fieldActionConfig.name;
      //if field is a dupe, discard it
      if (configsByName[name] != null) {
        continue;
      }
      configsByName[name] = fieldActionConfig;
      configNames.push(name);
    }

    configNames.sort();

    for (const name of configNames) {
      sortedActionConfigs.push(configsByName[name]);
    }
    return sortedActionConfigs;
  }

  notifyMappingUpdated(): void {
    if (this.cfg.mappings.mappings.length > 0) {
      this.validateMappings();
    }
    this.mappingUpdatedSource.next();
  }

  private handleError(message: string, error: any): void {
    this.cfg.errorService.error(message, error);
  }
}
