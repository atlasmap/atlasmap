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
    {'Content-Type': 'application/json; application/xml; application/octet-stream',
     'Accept':       'application/json; application/xml; application/octet-stream'});
  private mappingPreviewInputSubscription: Subscription;
  private mappingUpdatedSubscription: Subscription;
  private xmlBuffer: string;

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
        observer.error(error);
        observer.complete();
      });
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
  }

  /**
   * Retrieve the current user data mappings catalog from the server as a compressed byte array buffer.
   */
  getCurrentMappingCatalog(): Observable<Uint8Array> {
    const atlasmapCatalogName = 'atlasmap-catalog.adm';
    return new Observable<Uint8Array>((observer: any) => {
      const baseURL: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/GZ/';
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
        // Error is okay - there is no compressed file available.
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
   * Retrieve the current user AtlasMap data mappings from the server as an XML buffer.
   */
  getCurrentMappingXML(): Observable<string> {
    const mappingFileNames: string[] = this.cfg.mappingFiles;
    const mappingDefinition: MappingDefinition = this.cfg.mappings;
    return new Observable<string>((observer: any) => {
      const baseURL: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/XML/';
      const operations: Observable<any>[] = [];
      for (const mappingName of mappingFileNames) {
        const url: string = baseURL + mappingName;
        DataMapperUtil.debugLogJSON(null, 'Mapping Service Request', this.cfg.initCfg.debugMappingServiceCalls, url);
        const xmlHeaders = new HttpHeaders(
          { 'Content-Type':  'application/xml',
            'Accept':        'application/xml',
            'Response-Type': 'application/xml'
          });
        const operation = this.http.get(url, { headers: xmlHeaders, responseType: 'text' }).pipe(map((res: any) => res ));
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
          observer.complete();
          return res;
        })
        .catch((error: any) => {
          this.handleError('Error occurred while resetting mappings.', error); },
      );
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
  }

 /**
  * The user has specified an XML file to be loaded into their runtime model.  Use the mapping
  * service to get the runtime server to translate the XML into JSON and trigger a new mapping.
  *
  * @param buffer - XML content
  */
  setMappingToService(XMLbuffer: string): void {
    const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/XML';
    DataMapperUtil.debugLogJSON(null, 'Mapping Service Request', this.cfg.initCfg.debugMappingServiceCalls, url);
    this.http.put(url, XMLbuffer, { headers: this.headers }).toPromise().then((res: any) => {
        DataMapperUtil.debugLogJSON(res, 'Mapping Service Response', this.cfg.initCfg.debugMappingServiceCalls, url);
      })
      .catch((error: any) => {
        this.handleError('Error occurred while establishing mappings from an imported XML.', error); },
    );
  }

  /**
   * The user has either exported their mappings or we're saving them for them on the server.
   *
   * @param compressedBuffer
   */
   setCompressedMappingToService(compressedBuffer: any): void {
     const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/GZ';
     DataMapperUtil.debugLogJSON(null, 'Set Compressed Mapping Service Request', this.cfg.initCfg.debugMappingServiceCalls, url);
     this.http.put(url, compressedBuffer, { headers: this.headers }).toPromise().then((res: any) => {
         DataMapperUtil.debugLogJSON(res, 'Set Compressed Mapping Service Response', this.cfg.initCfg.debugMappingServiceCalls, url);
       })
       .catch((error: any) => {
         this.handleError('Error occurred while establishing mappings from a compressed mappings file.', error); },
     );
   }

  saveMappingToService(): Observable<boolean> {
    return new Observable<boolean>((observer: any) => {
      const payload: any = this.serializeMappingsToJSON();
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/JSON';
      DataMapperUtil.debugLogJSON(payload, 'Mapping Service Request', this.cfg.initCfg.debugMappingServiceCalls, url);
      this.http.put(url, JSON.stringify(payload), { headers: this.headers }).toPromise()
        .then((res: any) => {
          DataMapperUtil.debugLogJSON(res, 'Mapping Service Response', this.cfg.initCfg.debugMappingServiceCalls, url);
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
    if (fields.length === 0 && compoundSelection) {
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
    let suggestedValue = '0';
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
      suggestedValue = mappingPair.sourceFields[mappingPair.sourceFields.length - 1].actions[0].argumentValues[0].value;

    } else if (mappingPair.transition.mode === TransitionMode.SEPARATE) {
      if (field.isSource()) {

        // Compound target mapping when not in Separate mode.
        this.cfg.errorService.info('The selected mapping details action ' + TransitionModel.getActionName(mappingPair.transition.mode) +
                ' is not applicable to compound target selections (' + field.name +
                ').  Recommend using field action \'Separate\'.', null);
        return;
      }
      suggestedValue = mappingPair.targetFields[mappingPair.targetFields.length - 1].actions[0].argumentValues[0].value;
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

  fieldSelected(field: Field, compoundSelection: boolean): void {
    // Start out with a clean slate.
    this.cfg.errorService.clearMappingErrors();

    if (!field.isTerminal()) {
      field.docDef.populateChildren(field);
      field.docDef.updateFromMappings(this.cfg.mappings);
      field.collapsed = !field.collapsed;
      return;
    }

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
          }
      } else {
        mapping = null;
      }
    }

    if (mapping == null) {
      const mappingsForField: MappingModel[] = this.cfg.mappings.findMappingsForField(field);
      if (mappingsForField && mappingsForField.length > 1) {
        this.mappingSelectionRequiredSource.next(field);
        return;
      } else if (mappingsForField && mappingsForField.length === 1) {
        mapping = mappingsForField[0];
      }

      if (mapping == null) {
        this.addNewMapping(field, compoundSelection);
        mapping = this.cfg.mappings.activeMapping;
      }
    }

    // Check to see if the field is a valid selection for this mapping
    const exclusionReason: string = mapping.getFieldSelectionExclusionReason(field);
    if (exclusionReason != null) {
      this.cfg.errorService.mappingError('The field \'' + field.displayName + '\' cannot be selected, ' + exclusionReason + '.', null);
      return;
    }

    mapping.brandNewMapping = false;

    const latestFieldPair: FieldMappingPair = mapping.getCurrentFieldMapping();
    if (latestFieldPair != null) {
      const lastMappedField: MappedField = latestFieldPair.getLastMappedField(field.isSource());
      if (lastMappedField != null && lastMappedField.field.name.length === 0) {
        lastMappedField.field = field;
      }
      if (!fieldRemoved) {

        // Auto-transition from MAP mode to either COMBINE or SEPARATE if the user has
        // compound-selected another field.
        if (latestFieldPair.transition.mode === TransitionMode.MAP) {
          const mappedFields: MappedField[] = latestFieldPair.getMappedFields(field.isSource());
          if (mappedFields.length > 1) {
            if (field.isSource()) {
              latestFieldPair.transition.mode = TransitionMode.COMBINE;
              mappedFields[0].updateSeparateOrCombineFieldAction(false, true, '1', true, compoundSelection, false);
              this.cfg.errorService.info(
                'Note: Auto-transitioning to field action \'Combine\'.  ' +
                'You may want to examine the separator character in the Action box of the Mapping Details section.', null);
            } else {
              latestFieldPair.transition.mode = TransitionMode.SEPARATE;
              mappedFields[0].updateSeparateOrCombineFieldAction(true, false, '1', false, compoundSelection, false);
              this.cfg.errorService.info(
                'Note: Auto-transitioning to field action \'Separate\'.  ' +
                'You may want to examine the separator character in the Action box of the Mapping Details section.', null);
            }
          }
        }

        latestFieldPair.updateTransition(field.isSource(), compoundSelection, fieldRemoved);
        this.selectMapping(mapping);
        this.saveCurrentMapping();
      }
    }
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
        if (this.cfg.mappings.activeMapping.getCurrentFieldMapping() === inputFieldMapping) {
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
    if (this.cfg.initCfg.baseMappingServiceUrl == null) {
      // validation service not configured.
      return;
    }
    const payload: any = MappingSerializer.serializeMappings(this.cfg);
    const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/validate';
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
    if (this.cfg.mappings.activeMapping != null && this.cfg.mappings.activeMapping.hasFullyMappedPair()) {
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
  }

  /**
   * Asynchronously retrieve the current user-defined AtlasMap mappings from the runtime server as an XML buffer.
   */
  async getXMLbuf(): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      this.cfg.mappingFiles[0] = this.cfg.mappings.name;
      this.cfg.mappingService.getCurrentMappingXML().toPromise().then((result: string) => {
        this.xmlBuffer = result;
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
   * Export the current mappings catalog.  Establish the file content in JSON format (mappings + schema +
   * instance-schema) and compress it.
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

      // Retrieve the XML mappings buffer from the server.
      if (!await this.getXMLbuf()) {
        this.cfg.errorService.mappingError('Unable to retrieve the current data mappings.', null);
      }

      // Start with the user mappings (XML).
      aggregateBuffer += DocumentManagementService.generateExportMappings(this.xmlBuffer[0]);
      let exportMeta = '   "exportMeta": [\n';
      let exportBlockData = '      "exportBlockData": [\n';
      let docCount = 0;

      // Establish two string arrays:
      //   exportMeta - meta-data describing the instance or schema documents.
      //   exportBlockData - the actual source of the instance or schema documents.
      for (const doc of this.cfg.getAllDocs()) {
        if (doc.inspectionSource && doc.inspectionSource !== null &&
             (doc.inspectionType === InspectionType.INSTANCE) || (doc.inspectionType === InspectionType.SCHEMA)) {
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
        const fileContent: Blob = new Blob([compress], {type: 'application/octet-stream'});

        // User export gets written to the local downloads area.
        if (userExport) {
          if (!await DataMapperUtil.writeFile(fileContent, mappingsFileName)) {
            this.cfg.errorService.mappingError('Unable to save the current data mappings.', null);
          }
        }
        // Reinitialize the model mappings to the runtime.
        this.cfg.mappingService.setCompressedMappingToService(fileContent);
      } catch (error1) {
        this.cfg.errorService.mappingError('Unable to compress the current data mappings.\n', error1);
        return;
      }
    } catch (error) {
      this.cfg.errorService.mappingError('Unable to export the current data mappings.', error);
      return;
    }
  }
}
