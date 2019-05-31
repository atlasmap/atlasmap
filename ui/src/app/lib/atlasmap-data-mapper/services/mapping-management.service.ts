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

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { deflate } from 'pako';

import { Observable, Subscription, Subject, forkJoin } from 'rxjs';
import { map, timeout } from 'rxjs/operators';

import { ConfigModel } from '../models/config.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { DataMapperUtil } from '../common/data-mapper-util';
import { InspectionType } from '../common/config.types';
import { Field } from '../models/field.model';
import { DocumentManagementService } from '../services/document-management.service';
import { MappingModel, FieldMappingPair, MappedField } from '../models/mapping.model';
import { FieldActionConfig, FieldActionArgument, TransitionMode, TransitionModel } from '../models/transition.model';
import { MappingDefinition } from '../models/mapping-definition.model';
import { ErrorInfo, ErrorLevel } from '../models/error.model';

import { MappingSerializer } from './mapping-serializer.service';

@Injectable()
export class MappingManagementService {
  cfg: ConfigModel;

  mappingUpdatedSource = new Subject<void>();
  mappingUpdated$ = this.mappingUpdatedSource.asObservable();

  saveMappingSource = new Subject<Function>();
  saveMappingOutput$ = this.saveMappingSource.asObservable();

  mappingSelectionRequiredSource = new Subject<Field>();
  mappingSelectionRequired$ = this.mappingSelectionRequiredSource.asObservable();

  mappingPreviewInputSource = new Subject<FieldMappingPair>();
  mappingPreviewInput$ = this.mappingPreviewInputSource.asObservable();
  mappingPreviewOutputSource = new Subject<FieldMappingPair>();
  mappingPreviewOutput$ = this.mappingPreviewOutputSource.asObservable();
  mappingPreviewErrorSource = new Subject<ErrorInfo[]>();
  mappingPreviewError$ = this.mappingPreviewErrorSource.asObservable();

  private headers = new HttpHeaders(
    {'Content-Type': 'application/json; application/octet-stream',
     'Accept':       'application/json; application/octet-stream'});
  private mappingPreviewInputSubscription: Subscription;
  private mappingUpdatedSubscription: Subscription;
  private jsonBuffer: string;

  constructor(private http: HttpClient) {}

  // TODO consider extracting these utilities into separated class...
  // put these model parser kind of guys together
  static extractFieldActionConfig(actionDetail: any): FieldActionConfig {
    const fieldActionConfig: FieldActionConfig = new FieldActionConfig();
    fieldActionConfig.name = actionDetail.name;
    fieldActionConfig.isCustom = actionDetail.custom;
    fieldActionConfig.sourceType = actionDetail.sourceType;
    fieldActionConfig.targetType = actionDetail.targetType;
    fieldActionConfig.method = actionDetail.method;
    fieldActionConfig.serviceObject = actionDetail;

    if (actionDetail.parameters && actionDetail.parameters.parameter
      && actionDetail.parameters.parameter.length) {
      for (const actionParameter of actionDetail.parameters.parameter) {
        const argumentConfig: FieldActionArgument = new FieldActionArgument();
        argumentConfig.name = actionParameter.name;
        argumentConfig.type = actionParameter.fieldType;
        argumentConfig.values = actionParameter.values;
        argumentConfig.serviceObject = actionParameter;
        fieldActionConfig.arguments.push(argumentConfig);
      }
    }
    return fieldActionConfig;
  }

  static sortFieldActionConfigs(configs: FieldActionConfig[]): FieldActionConfig[] {
    const sortedActionConfigs: FieldActionConfig[] = [];
    if (configs == null || configs.length === 0) {
      return sortedActionConfigs;
    }

    const configsByName: { [key: string]: FieldActionConfig[]; } = {};
    const configNames: string[] = [];
    for (const fieldActionConfig of configs) {
      const name: string = fieldActionConfig.name;
      let sameNamedConfigs: FieldActionConfig[] = configsByName[name];
      if (!sameNamedConfigs) {
        sameNamedConfigs = [];
        configNames.push(name);
      }
      sameNamedConfigs.push(fieldActionConfig);
      configsByName[name] = sameNamedConfigs;
    }

    configNames.sort();

    for (const name of configNames) {
      const sameNamedConfigs: FieldActionConfig[] = configsByName[name];
      for (const fieldActionConfig of sameNamedConfigs) {
        sortedActionConfigs.push(fieldActionConfig);
      }
    }
    return sortedActionConfigs;
  }

  findMappingFiles(filter: string): Observable<string[]> {
    return new Observable<string[]>((observer: any) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mappings' + (filter == null ? '' : '?filter=' + filter);
      DataMapperUtil.debugLogJSON(null, 'Mapping List Response', this.cfg.initCfg.debugMappingServiceCalls, url);
      this.http.get(url, { headers: this.headers }).toPromise().then((body: any) => {
        DataMapperUtil.debugLogJSON(body, 'Mapping List Response', this.cfg.initCfg.debugMappingServiceCalls, url);
        const entries: any[] = body.StringMap.stringMapEntry;
        const mappingFileNames: string[] = [];
        for (const entry of entries) {
          mappingFileNames.push(entry.name);
        }
        observer.next(mappingFileNames);
        observer.complete();
      }).catch((error: any) => {
        if (error.status !== DataMapperUtil.HTTP_STATUS_NO_CONTENT) {
          this.handleError('Error occurred while accessing the current mapping files from the runtime service.', error);
          observer.error(error);
        }
        observer.complete();
      });
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
  }

  getMappingId(): string {
    return (this.cfg.mappingFiles.length > 0) ? this.cfg.mappingFiles[0] : '0';
  }

  /**
   * Retrieve the current user data mappings catalog from the server as a GZIP compressed byte array buffer.
   */
  getCurrentMappingCatalog(): Observable<Uint8Array> {
    const catalogName = 'adm-catalog-files.gz';
    return new Observable<Uint8Array>((observer: any) => {
      const baseURL: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/GZ/';
      const url: string = baseURL + catalogName;
      DataMapperUtil.debugLogJSON(null, 'Mapping Catalog Response', this.cfg.initCfg.debugMappingServiceCalls, url);
      const catHeaders = new HttpHeaders(
        { 'Content-Type':  'application/octet-stream',
          'Accept':        'application/octet-stream',
          'Response-Type': 'application/octet-stream'
        });
      this.http.get(url, { headers: catHeaders, responseType: 'arraybuffer' }).toPromise().then((body: any) => {
        DataMapperUtil.debugLogJSON(body, 'Mapping Catalog Response', this.cfg.initCfg.debugMappingServiceCalls, url);
        observer.next(body);
        observer.complete();
      }).catch((error: any) => {
        if (error.status !== DataMapperUtil.HTTP_STATUS_NO_CONTENT) {
          this.handleError('Error occurred while accessing the current mappings catalog from the runtime service.', error);
          observer.error(error);
        }
        observer.complete();
      });
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
  }

  getCurrentADMCatalog(): Observable<Uint8Array> {
    const atlasmapCatalogName = 'atlasmap-catalog.adm';
    return new Observable<Uint8Array>((observer: any) => {
      const baseURL: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/ZIP/';
      const url: string = baseURL + atlasmapCatalogName;
      DataMapperUtil.debugLogJSON(null, 'Mapping Catalog Response', this.cfg.initCfg.debugMappingServiceCalls, url);
      const catHeaders = new HttpHeaders(
        { 'Content-Type':  'application/octet-stream',
          'Accept':        'application/octet-stream',
          'Response-Type': 'application/octet-stream'
        });
      this.http.get(url, { headers: catHeaders, responseType: 'arraybuffer' }).toPromise().then((body: any) => {
        DataMapperUtil.debugLogJSON(body, 'Mapping Catalog Response', this.cfg.initCfg.debugMappingServiceCalls, url);
        observer.next(body);
        observer.complete();
      }).catch((error: any) => {
        if (error.status !== DataMapperUtil.HTTP_STATUS_NO_CONTENT) {
          this.handleError('Error occurred while accessing the ADM catalog from the runtime service.', error);
          observer.error(error);
        }
        observer.complete();
      });
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
  }

  fetchMappings(mappingFileNames: string[], mappingDefinition: MappingDefinition): Observable<boolean> {
    return new Observable<boolean>((observer: any) => {
      if (mappingFileNames.length === 0) {
        observer.complete();
        return;
      }
      const baseURL: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/JSON/';
      const operations: Observable<any>[] = [];
      for (const mappingName of mappingFileNames) {
        const url: string = baseURL + mappingName;
        DataMapperUtil.debugLogJSON(null, 'Mapping Service Request', this.cfg.initCfg.debugMappingServiceCalls, url);
        const operation = this.http.get(url).pipe(map((res: any) => res));
        operations.push(operation);
      }

      forkJoin(operations).toPromise().then((data: any[]) => {
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
      }).catch((error: any) => {
          observer.error(error);
          observer.complete();
        });
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
  }

  /**
   * Save the current active mappings to the UI configuration mappings.  Restrict the saved mappings
   * to fully mapped pairs (source/target) and source-side mappings with a transformation/ field action.
   */
  async saveCurrentMapping(): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      if (!this.cfg.mappings) {
        resolve(false);
      }
      const activeMapping: MappingModel = this.cfg.mappings.activeMapping;
      if ((activeMapping != null) && (this.cfg.mappings.mappings.indexOf(activeMapping) === -1)) {
        this.cfg.mappings.mappings.push(activeMapping);
      }
      const newMappings: MappingModel[] = [];
      for (const mapping of this.cfg.mappings.mappings) {
        if (mapping.hasFullyMappedPair() || mapping.hasFieldAction()) {
          newMappings.push(mapping);
        }
      }
      this.cfg.mappings.mappings = newMappings;
      this.saveMappingToService().toPromise().then(() => {
        this.saveMappingSource.next(null);
        this.notifyMappingUpdated();
        resolve(true);
      }).catch((error: any) => {
        if (error.status === 0) {
          this.cfg.errorService.mappingError(
            'Fatal network error: Unable to connect to the AtlasMap design runtime service.', error);
        } else {
          this.cfg.errorService.mappingError(
            'Unable to save current mapping definitions: ' + error.status + ' ' + error.statusText, error);
        }
        resolve(false);
      });
    });
  }

  serializeMappingsToJSON(): any {
    return MappingSerializer.serializeMappings(this.cfg);
  }

  /**
   * Retrieve the current user AtlasMap data mappings from the server as an JSON buffer.
   */
  getCurrentMappingJson(): Observable<string> {
    const mappingFileNames: string[] = this.cfg.mappingFiles;
    const mappingDefinition: MappingDefinition = this.cfg.mappings;
    return new Observable<string>((observer: any) => {
      const baseURL: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/JSON/';
      const operations: Observable<any>[] = [];
      for (const mappingName of mappingFileNames) {
        const url: string = baseURL + mappingName;
        DataMapperUtil.debugLogJSON(null, 'Mapping Service Request', this.cfg.initCfg.debugMappingServiceCalls, url);
        const jsonHeaders = new HttpHeaders(
          { 'Content-Type':  'application/json',
            'Accept':        'application/json',
            'Response-Type': 'application/json'
          });
        const operation = this.http.get(url, { headers: jsonHeaders, responseType: 'text' }).pipe(map((res: any) => res ));
        operations.push(operation);
      }

      forkJoin(operations).toPromise().then((data: string[]) => {
        if (!data) {
          observer.next('no data');
          observer.complete();
          return;
        }
        DataMapperUtil.debugLogJSON(data, 'Mapping Service Response', this.cfg.initCfg.debugMappingServiceCalls, null);
        this.notifyMappingUpdated();
        observer.next(data);
        observer.complete();
      }).catch((error: any) => {
        observer.error(error);
        observer.complete();
      });
    });
  }

  /**
   * Establish an observable function to delete mapping files on the runtime.
   */
  resetAll(): Observable<boolean> {
    return new Observable<boolean>((observer: any) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/RESET';
      DataMapperUtil.debugLogJSON(null, 'Mapping Service Request - Reset', this.cfg.initCfg.debugMappingServiceCalls, url);
      this.http.delete(url, { headers: this.headers }).toPromise().then((res: any) => {
          DataMapperUtil.debugLogJSON(res, 'Mapping Service Response - Reset', this.cfg.initCfg.debugMappingServiceCalls, url);
          observer.next(true);
          observer.complete();
          return res;
        })
        .catch((error: any) => {
          this.handleError('Error occurred while resetting mappings.', error); },
      );
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
  }

 /**
  * Commit the specified AtlasMapping JSON user mapping string to the runtime service.  The mappings
  * are kept separate so they can be updated with minimal overhead.
  *
  * @param buffer - JSON content
  */
  setMappingToService(jsonBuffer: string): Observable<boolean> {
    return new Observable<boolean>((observer: any) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/JSON/' + this.getMappingId();
      DataMapperUtil.debugLogJSON(null, 'Mapping Service Request', this.cfg.initCfg.debugMappingServiceCalls, url);
      this.http.put(url, jsonBuffer, { headers: this.headers }).toPromise().then((res: any) => {
        DataMapperUtil.debugLogJSON(res, 'Mapping Service Response', this.cfg.initCfg.debugMappingServiceCalls, url);
        observer.next(true);
        observer.complete();
      })
      .catch((error: any) => {
        this.handleError('Error occurred while establishing mappings from an imported JSON.', error);
        observer.error(error);
        observer.complete();
      });
    });
  }

  /**
   * The user has either exported their mappings or imported new mappings.  Either way we're saving them on the server.
   *
   * @param compressedBuffer
   */
   setBinaryFileToService(compressedBuffer: any, url: string): Observable<boolean> {
     return new Observable<boolean>((observer: any) => {
       DataMapperUtil.debugLogJSON(null, 'Set Compressed Mapping Service Request', this.cfg.initCfg.debugMappingServiceCalls, url);
       this.http.put(url, compressedBuffer, { headers: this.headers }).toPromise().then((res: any) => {
          DataMapperUtil.debugLogJSON(res, 'Set Compressed Mapping Service Response', this.cfg.initCfg.debugMappingServiceCalls, url);
          observer.next(true);
          observer.complete();
       })
      .catch((error: any) => {
        this.handleError('Error occurred while saving mapping.', error);
        observer.error(error);
        observer.complete();
      });
    });
   }

  saveMappingToService(): Observable<boolean> {
    return new Observable<boolean>((observer: any) => {
      const payload: any = this.serializeMappingsToJSON();
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/JSON/' + this.getMappingId();
      DataMapperUtil.debugLogJSON(payload, 'Mapping Service Request', this.cfg.initCfg.debugMappingServiceCalls, url);
      this.http.put(url, JSON.stringify(payload), { headers: this.headers }).toPromise()
        .then((res: any) => {
          DataMapperUtil.debugLogJSON(res, 'Mapping Service Response', this.cfg.initCfg.debugMappingServiceCalls, url);
          observer.next(true);
          observer.complete();
        })
      .catch((error: any) => {
        this.handleError('Error occurred while saving mapping.', error);
        observer.error(error);
        observer.complete();
      });
    });
  }

  handleMappingSaveSuccess(saveHandler: Function): void {
    if (saveHandler != null) {
      saveHandler();
    }
    this.notifyMappingUpdated();
  }

  /**
   * Remove the specified mapping model from the mappings array and update the runtime.
   *
   * @param mappingModel
   */
  removeMapping(mappingModel: MappingModel): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      const mappingWasRemoved: boolean = this.cfg.mappings.removeMapping(mappingModel);
      if (mappingWasRemoved) {
        const saveHandler: Function = (async() => {
          this.deselectMapping();
          await this.saveCurrentMapping();
        });
        this.saveMappingSource.next(saveHandler);
      } else {
        this.deselectMapping();
      }
      resolve(true);
    });
  }

  removeMappedPair(fieldPair: FieldMappingPair): void {
    this.cfg.mappings.activeMapping.removeMappedPair(fieldPair);
    if (this.cfg.mappings.activeMapping.fieldMappings.length === 0) {
      this.deselectMapping();
    }
    this.saveCurrentMapping();
  }

  addMappedPair(): FieldMappingPair {
    const fieldPair: FieldMappingPair = new FieldMappingPair();
    this.cfg.mappings.activeMapping.fieldMappings.push(fieldPair);
    this.saveCurrentMapping();
    return fieldPair;
  }

  updateMappedField(fieldPair: FieldMappingPair, isSource: boolean, fieldRemoved: boolean): void {
    fieldPair.updateTransition(isSource, false, fieldRemoved);
    this.saveCurrentMapping();
  }

  /**
   * Remove the specified field from a previously established and mapped field pair.
   * @param fieldPair
   * @param removeField
   */
  removeMappedFieldPairField(fieldPair: FieldMappingPair, removeField: Field, compoundSelection: boolean): void {
    let fields: MappedField[] = null;
    if (removeField.isSource()) {
      fields = fieldPair.sourceFields;
    } else {
      fields = fieldPair.targetFields;
    }
    for (const mfield of fields) {
      if (mfield.field.name === removeField.name) {
        DataMapperUtil.removeItemFromArray(mfield, fields);
        break;
      }
    }
    fieldPair.updateTransition(removeField.isSource(), compoundSelection, true);

    // If the removed field was the last field of this pairing then remove the field pair as well.
    if (fields.length === 1 && compoundSelection) {
      this.removeMappedPair(fieldPair);
    }
    this.saveCurrentMapping();
  }

  resequenceMappedField(fieldPair: FieldMappingPair, insertedMappedField: MappedField, targetIndex: string): void {
    if (fieldPair != null) {
      const mappedFields = fieldPair.getMappedFields(insertedMappedField.isSource());
      fieldPair.resequenceFieldActionIndices(mappedFields, insertedMappedField, targetIndex, false);
      this.saveCurrentMapping();
    }
  }

  /**
   * Add a compound-selected field to an existing mapping.  Needed for combine/ separate modes.
   * A compound source selection in separate mode or a compound target selection in combine mode is an error.
   * @param field
   */
  addActiveMappingField(field: Field): void {
    const mappingPair: FieldMappingPair = this.cfg.mappings.activeMapping.getFirstFieldMapping();
    let suggestedValue = '1';
    if (mappingPair.transition == null || field == null) {
      return;
    }
    if (mappingPair.transition.mode === TransitionMode.COMBINE) {

      // Compound source mapping when not in Combine mode
      if (!field.isSource()) {
        this.cfg.errorService.info('The selected mapping details action ' + TransitionModel.getActionName(mappingPair.transition.mode) +
                ' is not applicable from compound source selections (' + field.name +
                ').  Recommend using field action \'Combine\'.', null);
        return;
      }
      if (mappingPair.sourceFields[mappingPair.sourceFields.length - 1].actions.length > 0) {
        suggestedValue = ((+mappingPair.sourceFields[mappingPair.sourceFields.length - 1].getFieldIndex()) + 1).toString();
      }
    } else if (mappingPair.transition.mode === TransitionMode.SEPARATE) {
      if (field.isSource()) {

        // Compound target mapping when not in Separate mode.
        this.cfg.errorService.info('The selected mapping details action ' + TransitionModel.getActionName(mappingPair.transition.mode) +
                ' is not applicable to compound target selections (' + field.name +
                ').  Recommend using field action \'Separate\'.', null);
        return;
      }
      if (mappingPair.targetFields[mappingPair.targetFields.length - 1].actions.length > 0) {
        suggestedValue = ((+mappingPair.targetFields[mappingPair.targetFields.length - 1].getFieldIndex()) + 1).toString();
      }
    }
    const newMField = new MappedField;
    newMField.field = field;
    newMField.updateSeparateOrCombineFieldAction(mappingPair.transition.mode === TransitionMode.SEPARATE,
      mappingPair.transition.mode === TransitionMode.COMBINE, suggestedValue, field.isSource(), true, false);
    if (field.isSource()) {
      mappingPair.sourceFields.push(newMField);
    } else {
      mappingPair.targetFields.push(newMField);
    }
  }

  /**
   * Remove the specified field from the active mapping field pair.
   * @param field
   */
  removeActiveMappingField(field: Field, compoundSelection: boolean): void {
    const activeMapping: MappingModel = this.cfg.mappings.activeMapping;
    const mappingPair: FieldMappingPair = activeMapping.getCurrentFieldMapping();
    this.removeMappedFieldPairField(mappingPair, field, compoundSelection);
  }

  /**
   * Auto-transition from MAP mode to either COMBINE or SEPARATE mode.
   *
   * @param fieldPair
   * @param field
   */
  transitionMode(fieldPair: FieldMappingPair, field: Field): void {
    if (fieldPair.transition.mode === TransitionMode.MAP) {
      const mappedFields: MappedField[] = fieldPair.getMappedFields(field.isSource());
      if (mappedFields.length > 1) {
        if (field.isSource()) {
          fieldPair.transition.mode = TransitionMode.COMBINE;
          mappedFields[1].updateSeparateOrCombineFieldAction(false, true, '1', true, true, false);
          this.cfg.errorService.info(
            'Note: You\'ve selected multiple fields to combine.  ' +
            'You may want to examine the separator character in the \'Sources\' box of the Mapping Details section.', null);
        } else {
          fieldPair.transition.mode = TransitionMode.SEPARATE;
          mappedFields[1].updateSeparateOrCombineFieldAction(true, false, '1', false, true, false);
          this.cfg.errorService.info(
            'Note: You\'ve selected multiple fields to separate into.  ' +
            'You may want to examine the separator character in the \'Sources\' box of the Mapping Details section.', null);
        }
      }
    }
  }

  fieldSelected(field: Field, compoundSelection: boolean): void {

    // Start out with a clean slate.
    this.cfg.errorService.clearMappingErrors();
    this.cfg.errorService.clearValidationErrors();

    if (!field.isTerminal()) {
      field.docDef.populateChildren(field);
      field.docDef.updateFromMappings(this.cfg.mappings);
      field.collapsed = !field.collapsed;
      return;
    }
    let fieldAdded = false;
    let mapping: MappingModel = this.cfg.mappings.activeMapping;
    let fieldRemoved = false;

    if (mapping != null && mapping.hasMappedFields(field.isSource())) {

      // If the user has performed a compound selection (ctrl/cmd-m1) of a previously unselected field
      // then add it to the active mapping; otherwise remove it.
      if (compoundSelection) {
          if (mapping.isFieldMapped(field, field.isSource())) {
            this.removeActiveMappingField(field, compoundSelection);
            fieldRemoved = true;
          } else {
            this.addActiveMappingField(field);
            fieldAdded = true;
          }
      } else {
        mapping = null;
      }
    }

    if ((mapping == null) || (this.cfg.mappings.activeMapping && this.cfg.mappings.activeMapping.brandNewMapping)) {
      const mappingsForField: MappingModel[] = this.cfg.mappings.findMappingsForField(field);

      if (mappingsForField && mappingsForField.length === 1) {
        mapping = mappingsForField[0];
        this.cfg.mappings.activeMapping = mapping;

      // Source fields may be part of multiple mappings - trigger mapping required source observable thread.
      } else if (mappingsForField && mappingsForField.length > 1) {
        this.mappingSelectionRequiredSource.next(field);
        return;
      }

      if (mapping == null) {
        this.addNewMapping(field, compoundSelection);
        mapping = this.cfg.mappings.activeMapping;
        fieldAdded = true;
      }
    }

    if (this.cfg.mappings.activeMapping && !this.cfg.mappings.activeMapping.hasFullyMappedPair()) {
      this.addNewMapping(field, compoundSelection);
      fieldAdded = true;
    }

    if (!fieldAdded && !fieldRemoved) {
      this.selectMapping(mapping);
      return;
    }

    // Check to see if the field is a valid selection for this mapping
    const exclusionReason: string = mapping.getFieldSelectionExclusionReason(field);
    if (exclusionReason != null) {
      this.cfg.errorService.mappingError('The field \'' + field.name + '\' cannot be selected, ' + exclusionReason + '.', null);
      return;
    }

    mapping.brandNewMapping = false;

    const latestFieldPair: FieldMappingPair = mapping.getCurrentFieldMapping();
    if (latestFieldPair != null) {
      const lastMappedField: MappedField = latestFieldPair.getLastMappedField(field.isSource());
      if (lastMappedField != null && lastMappedField.isNoneField()) {
        lastMappedField.field = field;
      }
      if (!fieldRemoved) {
        if (compoundSelection || this.hasMultipleMappings(latestFieldPair)) {
          this.transitionMode(latestFieldPair, field);
        }
        latestFieldPair.updateTransition(field.isSource(), compoundSelection, fieldRemoved);
        this.selectMapping(mapping);
      }
    }
  }

  hasMultipleMappings(fieldPair: FieldMappingPair): boolean {
    return (fieldPair.sourceFields.length > 2 || fieldPair.targetFields.length > 2);
  }

  /**
   * Instantiate a new mapping model and associate the selected field with it.
   * @param selectedField
   * @param compoundSelection - indicates a compound-selection (ctrl/cmd-M1) if true, standard mouse click if false.
   */
  addNewMapping(selectedField: Field, compoundSelection: boolean): void {
    if (!compoundSelection) {
      this.deselectMapping();
    }
    const mapping: MappingModel = new MappingModel();
    mapping.brandNewMapping = false;
    if (selectedField != null) {
      const fieldPair: FieldMappingPair = mapping.getFirstFieldMapping();
      fieldPair.getMappedFields(selectedField.isSource())[0].field = selectedField;
      fieldPair.updateTransition(selectedField.isSource(), false, false);
      this.saveCurrentMapping();
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
    this.cfg.mappings.initializeMappingLookupTable(mappingModel);
    this.saveCurrentMapping();
  }

  deselectMapping(): void {
    this.cfg.showMappingDetailTray = false;
    this.cfg.mappings.activeMapping = null;
    this.notifyMappingUpdated();
  }

  enableMappingPreview(): void {
    if (this.cfg.initCfg.baseMappingServiceUrl == null) {
      // process mapping service not configured.
      return;
    }

    this.mappingPreviewInputSubscription = this.mappingPreviewInput$.subscribe(inputFieldMapping => {
      if (!inputFieldMapping || !inputFieldMapping.isFullyMapped()) {
        return;
      }
      let hasValue = false;
      for (const sourceField of inputFieldMapping.getFields(true)) {
        if (sourceField.value) {
          hasValue = true;
          break;
        }
      }
      if (!hasValue) {
        return;
      }

      const payload: any = {
        'ProcessMappingRequest': {
          'jsonType': ConfigModel.mappingServicesPackagePrefix + '.ProcessMappingRequest',
          'mapping': MappingSerializer.serializeFieldMapping(this.cfg, inputFieldMapping, 'preview', false)
        }
      };
      const docRefs: any = {};
      for (const docRef of this.cfg.getAllDocs()) {
        docRefs[docRef.id] = docRef.uri;
      }

      const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/process';
      DataMapperUtil.debugLogJSON(payload, 'Process Mapping Preview Request', this.cfg.initCfg.debugProcessMappingPreviewCalls, url);
      this.http.put(url, payload, { headers: this.headers }).toPromise().then((body: any) => {
        DataMapperUtil.debugLogJSON(body, 'Process Mapping Preview  Response', this.cfg.initCfg.debugProcessMappingPreviewCalls, url);
        const answer = MappingSerializer.deserializeFieldMapping(body.ProcessMappingResponse.mapping, docRefs, this.cfg, false);
        for (const toWrite of inputFieldMapping.targetFields) {
          if (DocumentDefinition.getNoneField().path === toWrite.field.path) {
            // excluging none field garbage
            continue;
          }
          for (const toRead of answer.targetFields) {
            if (toWrite.field.docDef.id === toRead.parsedData.parsedDocID
                && toWrite.field.path === toRead.parsedData.parsedPath) {
              // TODO let field component subscribe mappingPreviewOutputSource instead of doing this
              toWrite.field.value = toRead.parsedData.parsedValue;
              const index = answer.targetFields.indexOf(toRead);
              if (index !== -1) {
                answer.targetFields.splice(index, 1);
                break;
              }
            }
          }
        }
        this.mappingPreviewOutputSource.next(answer);
        const audits = MappingSerializer.deserializeAudits(body.ProcessMappingResponse.audits);
        if (this.cfg.mappings.activeMapping.getCurrentFieldMapping() === inputFieldMapping) {
          // TODO let error service subscribe mappingPreviewErrorSource instead of doing this
          this.cfg.mappings.activeMapping.previewErrors = audits;
        }
        this.mappingPreviewErrorSource.next(audits);
      }).catch((error: any) => {
        if (this.cfg.mappings && this.cfg.mappings.activeMapping &&
            this.cfg.mappings.activeMapping.getCurrentFieldMapping() === inputFieldMapping) {
          // TODO let error service subscribe mappingPreviewErrorSource instead of doing this
          this.cfg.mappings.activeMapping.previewErrors = [new ErrorInfo(error, ErrorLevel.ERROR, null)];
        }
        this.mappingPreviewErrorSource.next([new ErrorInfo(error, ErrorLevel.ERROR, null)]);
      });
    });

    this.mappingUpdatedSubscription = this.mappingUpdated$.subscribe(() => {
      if (!this.cfg || !this.cfg.mappings || !this.cfg.mappings.activeMapping) {
        return;
      }
      const activePair = this.cfg.mappings.activeMapping.getCurrentFieldMapping();
      if (activePair && activePair.isFullyMapped()) {
        this.mappingPreviewInputSource.next(activePair);
      }
    });
  }

  disableMappingPreview(): void {
    if (this.mappingUpdatedSubscription) {
      this.mappingUpdatedSubscription.unsubscribe();
      this.mappingUpdatedSubscription = undefined;
    }
    if (this.mappingPreviewInputSubscription) {
      this.mappingPreviewInputSubscription.unsubscribe();
      this.mappingPreviewInputSubscription = undefined;
    }
  }

  validateMappings(): void {
    if (this.cfg.initCfg.baseMappingServiceUrl === null || this.cfg.mappings === null) {
      // validation service not configured or required
      return;
    }
    const payload: any = MappingSerializer.serializeMappings(this.cfg);
    const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/validate';
    DataMapperUtil.debugLogJSON(payload, 'Validation Service Request', this.cfg.initCfg.debugValidationServiceCalls, url);
    this.http.put(url, payload, { headers: this.headers }).toPromise().then((body: any) => {
      DataMapperUtil.debugLogJSON(body, 'Validation Service Response', this.cfg.initCfg.debugValidationServiceCalls, url);
      if (this.cfg.mappings === null) {
        return;
      }
      const activeMappingErrors: ErrorInfo[] = [];
      const globalErrors: ErrorInfo[] = [];
      const mapping = this.cfg.mappings.activeMapping;

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
          if (!validation.scope || validation.scope !== 'MAPPING' || !validation.id) {
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
    }).catch((error: any) => {
      this.cfg.errorService.error('Error fetching validation data.', { 'error': error, 'url': url, 'request': payload });
    });
  }

  fetchFieldActions(): Observable<FieldActionConfig[]> {
    return new Observable<FieldActionConfig[]>((observer: any) => {
      let actionConfigs: FieldActionConfig[] = [];
      const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'fieldActions';
      DataMapperUtil.debugLogJSON(null, 'Field Action Config Request', this.cfg.initCfg.debugFieldActionServiceCalls, url);
      this.http.get(url, { headers: this.headers }).toPromise().then((body: any) => {
        DataMapperUtil.debugLogJSON(body, 'Field Action Config Response', this.cfg.initCfg.debugFieldActionServiceCalls, url);
        if (body && body.ActionDetails
          && body.ActionDetails.actionDetail
          && body.ActionDetails.actionDetail.length) {
          for (const actionDetail of body.ActionDetails.actionDetail) {
            const fieldActionConfig = MappingManagementService.extractFieldActionConfig(actionDetail);
            actionConfigs.push(fieldActionConfig);
          }
        }
        actionConfigs = MappingManagementService.sortFieldActionConfigs(actionConfigs);
        observer.next(actionConfigs);
        observer.complete();
      }).catch((error: any) => {
        observer.error(error);
        observer.next(actionConfigs);
        observer.complete();
      });
    });
  }

  notifyMappingUpdated(): void {
    if (this.cfg.mappings != null && this.cfg.mappings.activeMapping != null &&
      this.cfg.mappings.activeMapping.hasFullyMappedPair()) {
      this.validateMappings();
    }
    this.mappingUpdatedSource.next();
  }

  /**
   * Validate and push the active mapping to the server.
   */
  setMappingInstance(): void {
    if (this.cfg.initCfg.baseMappingServiceUrl == null) {
      // validation service not configured.
      return;
    }
    const payload: any = MappingSerializer.serializeMappings(this.cfg);
    const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/setinstance';
    DataMapperUtil.debugLogJSON(payload, 'Validation Service Request', this.cfg.initCfg.debugValidationServiceCalls, url);
    this.http.put(url, payload, { headers: this.headers }).toPromise().then((body: any) => {
      DataMapperUtil.debugLogJSON(body, 'Validation Service Response', this.cfg.initCfg.debugValidationServiceCalls, url);
      const mapping: MappingModel = this.cfg.mappings.activeMapping;
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
          if (!validation.scope || validation.scope !== 'MAPPING' || !validation.id) {
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
    }).catch((error: any) => {
      this.cfg.errorService.error('Error fetching validation data.', { 'error': error, 'url': url, 'request': payload });
    });
  }

  private handleError(message: string, error: any): void {
    this.cfg.errorService.mappingError(message, error);
    this.cfg.initCfg.initialized = true;
  }

  /**
   * Asynchronously retrieve the current user-defined AtlasMap mappings from the runtime server as an JSON buffer.
   */
  async getJsonBuf(): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      if (this.cfg.mappings === null) {
        resolve(false);
      }
      this.cfg.mappingFiles[0] = this.cfg.mappings.name;
      this.getCurrentMappingJson().toPromise().then((result: string) => {
        this.jsonBuffer = result;
        resolve(true);
      }).catch((error: any) => {
        if (error.status === 0) {
          this.cfg.errorService.mappingError(
            'Fatal network error: Unable to connect to the AtlasMap design runtime service.', error);
        } else {
          this.cfg.errorService.mappingError(
            'Unable to access current mapping definitions: ' + error.status + ' ' + error.statusText, error);
        }
        resolve(false);
      });
    });
  }

  /**
   * Update the current mapping files and export the current mappings catalog (ADM).
   *
   * Establish the file content in JSON format (mappings + schema + instance-schema), compress
   * it (GZIP), update the runtime, then fetch the full ADM catalog ZIP file from the runtime
   * and export it.
   *
   * @param event
   */
  async exportMappingsCatalog(mappingsFileName: string) {
    let aggregateBuffer = '   {\n';
    let userExport = true;

    try {
      if (mappingsFileName === null || mappingsFileName.length === 0) {
        mappingsFileName = 'atlasmap-mapping.adm';
        userExport = false;
      }

      // Retrieve the JSON mappings buffer from the server.
      if (await this.getJsonBuf()) {
        aggregateBuffer += DocumentManagementService.generateExportMappings(this.jsonBuffer[0]);
      }

      let exportMeta = '   "exportMeta": [\n';
      let exportBlockData = '      "exportBlockData": [\n';
      let docCount = 0;

      // Establish two string arrays:
      //   exportMeta - meta-data describing the instance or schema documents.
      //   exportBlockData - the actual source of the instance/schema/mappings documents or the Java class name.
      for (const doc of this.cfg.getAllDocs()) {
        if (doc.inspectionSource !== null &&
             (doc.inspectionType === InspectionType.INSTANCE) || (doc.inspectionType === InspectionType.SCHEMA) ||
               (doc.inspectionType === InspectionType.JAVA_CLASS) ) {
          if (docCount > 0) {
            exportMeta += ',\n';
            exportBlockData += ',\n';
          }
          exportMeta += DocumentManagementService.generateExportMetaStr(doc);
          exportBlockData += DocumentManagementService.generateExportBlockData(doc.inspectionSource);
          docCount++;
        }
      }
      exportMeta += '   ],\n';
      exportBlockData += '   ]\n';
      aggregateBuffer += exportMeta;
      aggregateBuffer += exportBlockData;
      aggregateBuffer += '   }\n';

      // Compress the JSON buffer - write out as binary.
      const binBuffer = DataMapperUtil.str2bytes(aggregateBuffer);
      try {
        const compress = deflate(binBuffer, {gzip: true});
        let fileContent: Blob = new Blob([compress], {type: 'application/octet-stream'});

        // Save the model mappings to the runtime.
        this.setBinaryFileToService(fileContent,
          this.cfg.initCfg.baseMappingServiceUrl + 'mapping/GZ/' + this.getMappingId()).toPromise()
          .then(async(result: boolean) => {


          // Fetch the full ADM catalog file from the runtime (ZIP) and export it to to the local
          // downloads area.
          if (userExport) {

            this.getCurrentADMCatalog().subscribe( async(value: Uint8Array) => {

              // If value is null then no compressed mappings catalog is available on the server.
              if (value === null) {
                return;
              }

              fileContent = new Blob([value], {type: 'application/octet-stream'});
              if (!await DataMapperUtil.writeFile(fileContent, mappingsFileName)) {
                this.cfg.errorService.mappingError('Unable to save the current data mappings.', null);
              }
            });
          }
        }).catch((error: any) => {
          if (error.status === 0) {
            this.cfg.errorService.mappingError(
              'Fatal network error: Unable to connect to the AtlasMap design runtime service.', error);
          } else {
            this.cfg.errorService.mappingError(
              'Unable to update the catalog mappings file to the AtlasMap design runtime service.  ' +
                error.status + ' ' + error.statusText, error);
          }
        });
      } catch (error1) {
        this.cfg.errorService.mappingError('Unable to compress the current data mappings.\n', error1);
        return;
      }
    } catch (error) {
      this.cfg.errorService.mappingError('Unable to export the current data mappings.', error);
      return;
    }
  }

  /**
   * Perform a binary read of the specified catalog (.ADM) file and push it to the runtime.  The ADM file is
   * in (ZIP) file format.  Once pushed, we can retrieve from runtime the extracted compressed (GZIP) mappings
   * file catalog as well as the mappings JSON file.  These files exist separately for performance reasons.
   *
   * Once the runtime has its ADM catalog, catalog files and mappings file set then restart the DM.
   *
   * @param mappingsFileName - ADM master ZIP catalog
   */
  async importADMCatalog(mappingsFileName: string) {
    let fileBin = null;
    const reader = new FileReader();

    // Turn the imported ADM file into a binary octet stream.
    try {
      fileBin = await DataMapperUtil.readBinaryFile(mappingsFileName, reader);
    } catch (error) {
      this.cfg.errorService.mappingError('Unable to import the specified catalog file \'' + mappingsFileName + '\'', error);
      return;
    }
    const fileContent: Blob = new Blob([fileBin], {type: 'application/octet-stream'});

    // Push the binary stream to the runtime.
    this.setBinaryFileToService(fileContent, this.cfg.initCfg.baseMappingServiceUrl +
      'mapping/ZIP/' + this.getMappingId()).toPromise().then((result: boolean) => {

        // Retrieve the extracted mappings file catalog (GZIP).
        this.getCurrentMappingCatalog().subscribe(async(value: Uint8Array) => {

          // If value is null then the imported ADM didn't contain a mappings catalog.
          if (value === null) {
            return;
          }

          await this.cfg.initializationService.processMappingsCatalogFiles(value, true);

          try {
            this.cfg.initCfg.initialized = false;
            this.cfg.initCfg.mappingInitialized = false;
            this.cfg.mappings = null;
            this.cfg.initCfg.discardNonMockSources = true;
            await this.cfg.initializationService.initialize();
          } catch (error) {
            this.cfg.errorService.mappingError('Unable to import the catalog file: \n' + mappingsFileName +
              '\n' + error.message, error);
            return;
          }
        });
      }).catch((error: any) => {
        if (error.status === 0) {
          this.cfg.errorService.mappingError(
            'Fatal network error: Unable to connect to the AtlasMap design runtime service.', error);
        } else {
          this.cfg.errorService.mappingError(
            'Unable to send the ADM file to the runtime service.  ' + error.status + ' ' + error.statusText, error);
        }
      });
  }
}
