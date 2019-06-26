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
import { NGXLogger } from 'ngx-logger';

import { Observable, Subscription, Subject, forkJoin } from 'rxjs';
import { map, timeout } from 'rxjs/operators';

import { ConfigModel } from '../models/config.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { DataMapperUtil } from '../common/data-mapper-util';
import { InspectionType } from '../common/config.types';
import { Field } from '../models/field.model';
import { DocumentManagementService } from '../services/document-management.service';
import { MappingModel, MappedField } from '../models/mapping.model';
import { TransitionMode, TransitionModel } from '../models/transition.model';
import { MappingDefinition } from '../models/mapping-definition.model';
import { ErrorInfo, ErrorLevel } from '../models/error.model';

import { MappingSerializer } from './mapping-serializer.service';
import { NgxLoggerLevel } from 'ngx-logger';

@Injectable()
export class MappingManagementService {
  _cfg: ConfigModel;

  mappingUpdatedSource = new Subject<void>();
  mappingUpdated$ = this.mappingUpdatedSource.asObservable();

  debugMappingUpdatedSubscription: Subscription;

  mappingSelectionRequiredSource = new Subject<Field>();
  mappingSelectionRequired$ = this.mappingSelectionRequiredSource.asObservable();

  mappingPreviewInputSource = new Subject<MappingModel>();
  mappingPreviewInput$ = this.mappingPreviewInputSource.asObservable();
  mappingPreviewOutputSource = new Subject<MappingModel>();
  mappingPreviewOutput$ = this.mappingPreviewOutputSource.asObservable();
  mappingPreviewErrorSource = new Subject<ErrorInfo[]>();
  mappingPreviewError$ = this.mappingPreviewErrorSource.asObservable();

  private headers = new HttpHeaders(
    {'Content-Type': 'application/json; application/octet-stream',
     'Accept':       'application/json; application/octet-stream'});
  private mappingPreviewInputSubscription: Subscription;
  private mappingUpdatedSubscription: Subscription;
  private jsonBuffer: string;

  constructor(private logger: NGXLogger, private http: HttpClient) {}

  get cfg() {
    return this._cfg;
  }

  set cfg(cfg: ConfigModel) {
    this._cfg = cfg;
    if (!this._cfg.logger) {
      this._cfg.logger = this.logger;
    }
    if (this._cfg.isDebugEnabled()) {
      this.mappingUpdated$.subscribe(() => {
        if (!this.cfg.mappings) {
          return;
        }
        this.cfg.logger.debug('mapping updated: ' + JSON.stringify(this.serializeMappingsToJSON()));
      });
    }
  }

  findMappingFiles(filter: string): Observable<string[]> {
    return new Observable<string[]>((observer: any) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mappings' + (filter == null ? '' : '?filter=' + filter);
      this.cfg.logger.trace('Mapping List Request');
      this.http.get(url, { headers: this.headers }).toPromise().then((body: any) => {
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Mapping List Response: ${JSON.stringify(body)}`);
        }
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
      this.cfg.logger.trace('Mapping Catalog Request');
      const catHeaders = new HttpHeaders(
        { 'Content-Type':  'application/octet-stream',
          'Accept':        'application/octet-stream',
          'Response-Type': 'application/octet-stream'
        });
      this.http.get(url, { headers: catHeaders, responseType: 'arraybuffer' }).toPromise().then((body: any) => {
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Mapping Catalog Response: ${JSON.stringify(body)}`);
        }
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
      this.cfg.logger.trace('Mapping Catalog Request');
      const catHeaders = new HttpHeaders(
        { 'Content-Type':  'application/octet-stream',
          'Accept':        'application/octet-stream',
          'Response-Type': 'application/octet-stream'
        });
      this.http.get(url, { headers: catHeaders, responseType: 'arraybuffer' }).toPromise().then((body: any) => {
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Mapping Catalog Response: ${JSON.stringify(body)}`);
        }
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
        this.cfg.logger.trace('Mapping Service Request');
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
          if (this.cfg.isTraceEnabled()) {
            this.cfg.logger.trace(`Mapping Service Response: ${JSON.stringify(d)}`);
          }
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
    return new Promise<boolean>( async(resolve, reject) => {
      if (!this.cfg.mappings) {
        resolve(false);
      }
      const activeMapping: MappingModel = this.cfg.mappings.activeMapping;
      if ((activeMapping != null) && (this.cfg.mappings.mappings.indexOf(activeMapping) === -1)) {
        this.cfg.mappings.mappings.push(activeMapping);
      }
      const newMappings: MappingModel[] = [];
      for (const mapping of this.cfg.mappings.mappings) {
        if (mapping.isFullyMapped() || mapping.hasFieldActions()) {
          newMappings.push(mapping);
        }
      }
      this.cfg.mappings.mappings = newMappings;
      await this.cfg.mappingService.validateMappings();
      this.mappingUpdatedSource.next();
      resolve(true);
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
        this.cfg.logger.trace('Mapping Service Request');
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
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Mapping Service Response: ${JSON.stringify(data)}`);
        }
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
      this.cfg.logger.trace('Mapping Service Request - Reset');
      this.http.delete(url, { headers: this.headers }).toPromise().then((res: any) => {
          if (this.cfg.isTraceEnabled()) {
            this.cfg.logger.trace(`Mapping Service Response - Reset: ${JSON.stringify(res)}`);
          }
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
      this.cfg.logger.trace('Mapping Service Request');
      this.http.put(url, jsonBuffer, { headers: this.headers }).toPromise().then((res: any) => {
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Mapping Service Response: ${JSON.stringify(res)}`);
        }
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
       this.cfg.logger.trace('Set Compressed Mapping Service Request');
       this.http.put(url, compressedBuffer, { headers: this.headers }).toPromise().then((res: any) => {
          if (this.cfg.isTraceEnabled()) {
            this.cfg.logger.trace(`Set Compressed Mapping Service Response: ${JSON.stringify(res)}`);
          }
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

  /**
   * Remove the specified mapping model from the mappings array and update the runtime.
   *
   * @param mappingModel
   */
  async removeMapping(mappingModel: MappingModel): Promise<boolean> {
    return new Promise<boolean>( async(resolve) => {
      const mappingWasRemoved: boolean = this.cfg.mappings.removeMapping(mappingModel);
      if (mappingWasRemoved) {
        this.deselectMapping();
        await this.saveCurrentMapping();
      } else {
        this.deselectMapping();
      }
      resolve(true);
    });
  }

  async updateMappedField(mapping: MappingModel, isSource: boolean, fieldRemoved: boolean): Promise<boolean> {
    return new Promise<boolean>( async(resolve) => {
      mapping.updateTransition(isSource, false, fieldRemoved);
      await this.saveCurrentMapping();
      resolve(true);
    });
  }

  /**
   * Remove the specified field from a previously established and mapped field pair.
   *
   * @param mapping
   * @param removeField
   */
  async removeMappedFieldPairField(mapping: MappingModel, removeField: Field, compoundSelection: boolean): Promise<boolean> {
    return new Promise<boolean>( async(resolve) => {
      let fields: MappedField[] = null;

      if (removeField.isSource()) {
        fields = mapping.sourceFields;
      } else {
        fields = mapping.targetFields;
      }
      for (const mfield of fields) {
        if (mfield.field.name === removeField.name) {
          DataMapperUtil.removeItemFromArray(mfield, fields);

          // If all that is left is the 'None' field after removing the user field then remove
          // the mapping completely.
          if ((fields.length === 1) && (fields[0].isNoneField())) {
            await this.cfg.mappingService.removeMapping(mapping);
            this.cfg.mappings.activeMapping = null;
          }
          break;
        }
      }
      mapping.updateTransition(removeField.isSource(), compoundSelection, true);
      await this.saveCurrentMapping();
      resolve(true);
    });
  }

  resequenceMappedField(mapping: MappingModel, insertedMappedField: MappedField, targetIndex: number): void {
    if (mapping != null) {
      const mappedFields = mapping.getMappedFields(insertedMappedField.isSource());
      mapping.resequenceFieldActionIndices(mappedFields, insertedMappedField, targetIndex, false);
      this.saveCurrentMapping();
    }
  }

  /**
   * Add a compound-selected field to an existing mapping.  Needed for combine/ separate modes.
   * A compound source selection in separate mode or a compound target selection in combine mode is an error.
   * @param field
   */
  addActiveMappingField(field: Field): void {
    const mapping = this.cfg.mappings.activeMapping;
    let suggestedValue = 1;
    if (mapping.transition == null || field == null) {
      return;
    }
    if (mapping.transition.mode === TransitionMode.MANY_TO_ONE) {

      // Compound target mapping when not in ONE_TO_MANY mode
      if (!field.isSource()) {
        this.cfg.errorService.info(`Cannot add target field '${field.name}' into mapping:
        Multiple target fields cannot be added into
          ${TransitionModel.getMappingModeName(mapping.transition.mode)}
          mapping. Only one of Source field or Target field could be multiple.`, null);
        return;
      }
      if (mapping.sourceFields[mapping.sourceFields.length - 1].actions.length > 0) {
        suggestedValue = mapping.sourceFields[mapping.sourceFields.length - 1].index + 1;
      }
    } else if (mapping.transition.mode === TransitionMode.ONE_TO_MANY) {
      if (field.isSource()) {

        // Compound source mapping when not in MANY_TO_ONE mode.
        this.cfg.errorService.info(`Cannot add source field '${field.name}' into mapping:
          Multiple source fields cannot be added into
          ${TransitionModel.getMappingModeName(mapping.transition.mode)}
          mapping. Only one of Source field or Target field could be multiple.`, null);
        return;
      }
      if (mapping.targetFields[mapping.targetFields.length - 1].actions.length > 0) {
        suggestedValue = mapping.targetFields[mapping.targetFields.length - 1].index + 1;
      }
    }
    const newMField = new MappedField;
    newMField.field = field;
    newMField.index = suggestedValue;
    newMField.updateSeparateOrCombineFieldAction(mapping.transition.mode === TransitionMode.ONE_TO_MANY,
      mapping.transition.mode === TransitionMode.MANY_TO_ONE, field.isSource(), true, false);
    if (field.isSource()) {
      mapping.sourceFields.push(newMField);
    } else {
      mapping.targetFields.push(newMField);
    }
  }

  /**
   * Remove the specified field from the active mapping field pair.
   * @param field
   */
  removeActiveMappingField(field: Field, compoundSelection: boolean): void {
    this.removeMappedFieldPairField(this.cfg.mappings.activeMapping, field, compoundSelection);
  }

  /**
   * Auto-transition from MAP mode to either COMBINE or SEPARATE mode.
   *
   * @param mapping
   * @param field
   */
  transitionMode(mapping: MappingModel, field: Field): void {
    if (mapping.transition.mode === TransitionMode.ONE_TO_ONE) {
      const mappedFields: MappedField[] = mapping.getMappedFields(field.isSource());
      if (mappedFields.length > 2) {
        if (field.isSource()) {
          mapping.transition.mode = TransitionMode.MANY_TO_ONE;
          mappedFields[1].index = 1;
          mappedFields[1].updateSeparateOrCombineFieldAction(false, true, true, true, false);
          this.cfg.errorService.info(
            'Note: You\'ve selected multiple fields to combine.  ' +
            'You may want to examine the separator character in the \'Sources\' box of the Mapping Details section.', null);
        } else {
          mapping.transition.mode = TransitionMode.ONE_TO_MANY;
          mappedFields[1].index = 1;
          mappedFields[1].updateSeparateOrCombineFieldAction(true, false, false, true, false);
          this.cfg.errorService.info(
            'Note: You\'ve selected multiple fields to separate into.  ' +
            'You may want to examine the separator character in the \'Sources\' box of the Mapping Details section.', null);
        }
      }
    }
  }

  fieldSelected(field: Field, compoundSelection: boolean, position?: string, offset?: number): void {

    // Start out with a clean slate.
    this.cfg.errorService.clearMappingErrors();
    this.cfg.errorService.clearValidationErrors();
    this.cfg.errorService.clearWarnings();

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
          if (mapping.isFieldMapped(field)) {
            this.removeActiveMappingField(field, compoundSelection);
            fieldRemoved = true;
          } else {
            this.addActiveMappingField(field);
            fieldAdded = true;
          }
      } else {
        if (!mapping.isFieldMapped(field)) {
          mapping = null;
        }
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

    if (this.cfg.mappings.activeMapping && !this.cfg.mappings.activeMapping.isFullyMapped()) {
      this.addNewMapping(field, compoundSelection);
      fieldAdded = true;
    }

    if (!fieldAdded && !fieldRemoved) {
      this.selectMapping(mapping);
      this.validateMappings();
      return;
    }

    // Check to see if the field is a valid selection for this mapping
    const exclusionReason: string = mapping.getFieldSelectionExclusionReason(field);
    if (exclusionReason != null) {
      this.cfg.errorService.mappingError('The field \'' + field.name + '\' cannot be selected, ' + exclusionReason + '.', null);
      return;
    }

    mapping.brandNewMapping = false;

    const lastMappedField: MappedField = mapping.getLastMappedField(field.isSource());
    if (lastMappedField != null && lastMappedField.isNoneField()) {
      lastMappedField.field = field;
    }
    if (!fieldRemoved) {
      if (compoundSelection || this.hasMultipleMappings(mapping)) {
        this.transitionMode(mapping, field);
      }
      mapping.updateTransition(field.isSource(), compoundSelection, fieldRemoved, position, offset);
      this.selectMapping(mapping);
      this.validateMappings();
    }
  }

  hasMultipleMappings(mapping: MappingModel): boolean {
    return (mapping.sourceFields.length > 2 || mapping.targetFields.length > 2);
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
      mapping.getMappedFields(selectedField.isSource())[0].field = selectedField;
      mapping.updateTransition(selectedField.isSource(), false, false);
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
    this.mappingUpdatedSource.next();
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
      if (this.cfg.isTraceEnabled()) {
        this.cfg.logger.trace(`Process Mapping Preview Request: ${JSON.stringify(payload)}`);
      }
      this.http.put(url, payload, { headers: this.headers }).toPromise().then((body: any) => {
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Process Mapping Preview  Response: ${JSON.stringify(body)}`);
        }
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
        if (this.cfg.mappings.activeMapping === inputFieldMapping) {
          // TODO let error service subscribe mappingPreviewErrorSource instead of doing this
          this.cfg.mappings.activeMapping.previewErrors = audits;
        }
        this.mappingPreviewErrorSource.next(audits);
      }).catch((error: any) => {
        if (this.cfg.mappings && this.cfg.mappings.activeMapping &&
            this.cfg.mappings.activeMapping === inputFieldMapping) {
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
      if (this.cfg.mappings.activeMapping.isFullyMapped()) {
        this.mappingPreviewInputSource.next(this.cfg.mappings.activeMapping);
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

  async validateMappings(): Promise<boolean> {

    return new Promise<boolean>((resolve, reject) => {
      if (this.cfg.initCfg.baseMappingServiceUrl === null || this.cfg.mappings === null) {
        // validation service not configured or required
        resolve(false);
        return;
      }
      const payload: any = MappingSerializer.serializeMappings(this.cfg);
      const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/validate';
      if (this.cfg.isTraceEnabled()) {
        this.cfg.logger.trace(`Validation Service Request: ${JSON.stringify(payload)}\n`);
      }
      this.http.put(url, payload, { headers: this.headers }).toPromise().then((body: any) => {
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Validation Service Response: ${JSON.stringify(body)}\n`);
        }
        if (this.cfg.mappings === null) {
          resolve(false);
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
        resolve(true);
      }).catch((error: any) => {
        this.cfg.logger.warn('Unable to fetch validation data.');
      });
    });
  }

  async notifyMappingUpdated(): Promise<boolean> {
    return new Promise<boolean>( async(resolve, reject) => {
      if (this.cfg.mappings != null && this.cfg.mappings.activeMapping != null &&
        this.cfg.mappings.activeMapping.isFullyMapped()) {
        await this.validateMappings();
      }
      this.mappingUpdatedSource.next();
      resolve(true);
    });
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
    if (this.cfg.isTraceEnabled()) {
      this.cfg.logger.trace(`Validation Service Request: ${JSON.stringify(payload)}`);
    }
    this.http.put(url, payload, { headers: this.headers }).toPromise().then((body: any) => {
      if (this.cfg.isTraceEnabled()) {
        this.cfg.logger.trace(`Validation Service Response: ${JSON.stringify(body)}`);
      }
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

          await this.cfg.initializationService.processMappingsCatalogFiles(value);

          try {
            this.cfg.initCfg.initialized = false;
            this.cfg.initCfg.mappingInitialized = false;
            this.cfg.mappings = null;
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
