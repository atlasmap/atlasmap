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
import { NGXLogger } from 'ngx-logger';

import { Observable, Subscription, Subject, forkJoin } from 'rxjs';
import { map, timeout } from 'rxjs/operators';

import { ConfigModel } from '../models/config.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { DataMapperUtil } from '../common/data-mapper-util';
import { Field } from '../models/field.model';
import { MappingModel, MappedField } from '../models/mapping.model';
import { TransitionMode, TransitionModel } from '../models/transition.model';
import { MappingDefinition } from '../models/mapping-definition.model';
import { ErrorInfo, ErrorLevel } from '../models/error.model';

import { MappingSerializer } from './mapping-serializer.service';

/**
 * Handles mapping updates. It restores mapping status from backend and reflect in UI,
 * and/or reflect mapping changes caused by the user action made in UI to the backend.
 */
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
        this.cfg.logger.debug('mapping updated: ' + JSON.stringify(MappingSerializer.serializeMappings(this.cfg)));
      });
    }
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

  resequenceMappedField(mapping: MappingModel, insertedMappedField: MappedField, targetIndex: number): void {
    if (mapping != null) {
      const mappedFields = mapping.getMappedFields(insertedMappedField.isSource());
      mapping.resequenceFieldIndices(mappedFields, insertedMappedField, targetIndex, false);
      this.saveCurrentMapping();
    }
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
            this.removeMappedField(this.cfg.mappings.activeMapping, field, compoundSelection);
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
   * Add a compound-selected field to an existing mapping.  Needed for combine/ separate modes.
   * A compound source selection in separate mode or a compound target selection in combine mode is an error.
   * @param field
   */
  private addActiveMappingField(field: Field): void {
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

  private hasMultipleMappings(mapping: MappingModel): boolean {
    return (mapping.sourceFields.length > 2 || mapping.targetFields.length > 2);
  }

  /**
   * Remove the specified field from mapping.
   *
   * @param mapping
   * @param removeField
   */
  private async removeMappedField(mapping: MappingModel, removeField: Field, compoundSelection: boolean): Promise<boolean> {
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

}
