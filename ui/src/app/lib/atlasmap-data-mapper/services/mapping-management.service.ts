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
import { Field } from '../models/field.model';
import { MappingModel, MappedField } from '../models/mapping.model';
import { TransitionMode } from '../models/transition.model';
import { MappingDefinition } from '../models/mapping-definition.model';
import { ErrorInfo, ErrorLevel } from '../models/error.model';

import { MappingSerializer } from './mapping-serializer.service';
import { DataMapperUtil } from '../common/data-mapper-util';
import { DocumentDefinition, PaddingField } from '../models/document-definition.model';
import { runInThisContext } from 'vm';

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
        this.cfg.mappings.getAllMappings(true).forEach(m => this.updateTransition(m));
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

  async updateMappedField(mapping: MappingModel): Promise<boolean> {
    return new Promise<boolean>( async(resolve) => {
      if (mapping.isEmpty()) {
        this.cfg.mappings.removeMapping(mapping);
        this.deselectMapping();
      } else {
        this.updateTransition(mapping);
      }
      await this.saveCurrentMapping();
      resolve(true);
    });
  }

  /**
   * Move existing MappedField in the mapping to specified index, involves field index updates.
   * @param mapping
   * @param insertedMappedField
   * @param targetIndex
   */
  moveMappedFieldTo(mapping: MappingModel, insertedMappedField: MappedField, targetIndex: number): void {
    if (mapping == null) {
      return;
    }
    const mappedFields = mapping.getMappedFields(insertedMappedField.isSource());
    mappedFields.splice(mapping.getIndexForMappedField(insertedMappedField) - 1, 1);
    mappedFields.splice(targetIndex - 1, 0, insertedMappedField);
    this.clearTrailingPaddingFields(mappedFields);
    this.saveCurrentMapping();
  }

  /**
   * Given an index range, fill in the field-pair mappings gap with place-holder fields.
   *
   * @param lowIndex
   * @param highIndex
   * @param fieldPair
   */
  addPlaceholders(count: number, mapping: MappingModel, isSource: boolean) {
    let padField = null;
    for (let i = 0; i < count; i++) {
      padField = new MappedField;
      padField.field = new PaddingField(isSource);
      if (isSource) {
          mapping.sourceFields.push(padField);
      } else {
          mapping.targetFields.push(padField);
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
    let addField = false;
    let removeField = false;
    let mapping: MappingModel = this.cfg.mappings.activeMapping;

    // Check compound selection and active mapping status to determine the action
    if (mapping != null) {
      if (mapping.hasMappedField(field.isSource())) {

        // If the user has performed a compound selection (ctrl/cmd-m1) of a previously unselected field
        // then add it to the active mapping; otherwise remove it.
        if (compoundSelection) {
            if (mapping.isFieldMapped(field)) {
              removeField = true;
            } else {
              addField = true;
            }
        } else {
          if (!mapping.isFieldMapped(field)) {
            if (!mapping.isFullyMapped()) {
              this.cfg.mappings.removeMapping(mapping);
              this.deselectMapping();
            }
            mapping = null;
          }
        }
      } else {
        addField = true;
      }
    }

    // Select other existing mapping if selected field participates, or create a new one
    if (mapping == null) {
      const mappingsForField: MappingModel[] = this.cfg.mappings.findMappingsForField(field);

      if (mappingsForField && mappingsForField.length === 1) {
        mapping = mappingsForField[0];
        this.selectMapping(mapping);

      // Source fields may be part of multiple mappings - trigger mapping required source observable thread.
      } else if (mappingsForField && mappingsForField.length > 1) {
        this.mappingSelectionRequiredSource.next(field);
        return;
      } else {
        mapping = new MappingModel();
        this.selectMapping(mapping);
        addField = true;
      }
    }

    if (!addField && !removeField) {
      this.selectMapping(mapping);
      this.validateMappings();
      return;
    }

    if (addField) {
      const exclusionReason: string = this.getFieldSelectionExclusionReason(mapping, field);
      if (exclusionReason != null) {
        this.cfg.errorService.mappingError('The field \'' + field.name + '\' cannot be selected, ' + exclusionReason + '.', null);
        return;
      }
      mapping.addField(field, false);
      this.updateTransition(mapping, position, offset);
      this.saveCurrentMapping();
      return;
    }

    if (removeField) {
      mapping.getMappedFieldForField(field);
      mapping.removeField(field);
      if (mapping.isEmpty()) {
        this.cfg.mappings.removeMapping(mapping);
        this.deselectMapping();
      } else {
        this.updateTransition(mapping, position, offset);
      }
      this.saveCurrentMapping();
    }
  }

  getFieldSelectionExclusionReason(mapping: MappingModel, field: Field): string {
    if (mapping.getAllMappedFields().length === 0) { // if mapping hasn't had a field selected yet, allow it
      return null;
    }

    if (!field.isTerminal()) {
      return 'field is a parent field';
    }

    // Target fields may only be mapped once.
    const existingMappedField = mapping.getMappedTarget(field);
    if (existingMappedField != null) {
      const macPlatform: boolean = /(MacPPC|MacIntel|Mac_PowerPC|Macintosh|Mac OS X)/.test(navigator.userAgent);
      return 'it is already the target of another mapping (' + existingMappedField + '). ' +
        'Use ' + (macPlatform ? 'CMD' : 'CTRL') + '-M1 to select multiple elements for \'Combine\' or \'Separate\' actions.';
    }

    const lookupMode: boolean = mapping.isLookupMode();

    if (lookupMode) {
      if (!field.enumeration) {
        return 'only Enumeration fields are valid for this mapping';
      }
    } else {
      // enums are not selectable in these modes
      if (field.enumeration) {
        return 'Enumeration fields are not valid for this mapping';
      }
      if (mapping.transition.enableExpression && !field.isSource() && mapping.getMappedFields(false).length > 0) {
        return 'Cannot add multiple target fields when conditional mapping is enabled.';
      }
      if (mapping.getMappedFields(field.isSource()).length > 0
      && mapping.getMappedFields(!field.isSource()).length > 1) {
        const direction = field.isSource() ? 'source' : 'target';
        return `Multiple ${direction} fields cannot be added into
          mapping. Only one of Source field or Target field could be multiple.`;
      }
    }
    return null;
  }

  isFieldSelectable(mapping: MappingModel, field: Field): boolean {
    return this.getFieldSelectionExclusionReason(mapping, field) == null;
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
    if (selectedField != null) {
      mapping.addField(selectedField, false);
      this.updateTransition(mapping);
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

  private updateTransition(mapping: MappingModel, position?: string, offset?: number): void {
    for (const field of mapping.getAllFields()) {
      if (field.enumeration) {
        mapping.transition.mode = TransitionMode.ENUM;
        return;
      }
    }

    if (mapping.getMappedFields(true).length > 1) {
      mapping.transition.mode = TransitionMode.MANY_TO_ONE;
    } else if (mapping.getMappedFields(false).length > 1) {
      mapping.transition.mode = TransitionMode.ONE_TO_MANY;
    } else {
      mapping.transition.mode = TransitionMode.ONE_TO_ONE;
    }

    // Update conditional expression field references if enabled.
    if (mapping.transition.enableExpression && mapping.transition.expression) {
      mapping.transition.expression.updateFieldReference(mapping, position, offset);
    }
  }

  /**
   * Remove any trailing padding fields for the mapped field array.  This occurs when a user moves
   * a mapped element above the last padding field.
   *
   * @param mappedFields
   */
  private clearTrailingPaddingFields(mappedFields: MappedField[]): void {
    let index = 0;
    let mField = null;

    for (index = mappedFields.length - 1; index >= 0; index--) {
      mField = mappedFields[index];
      if (mField.isPadField()) {
        DataMapperUtil.removeItemFromArray(mField, mappedFields);
        continue;
      }
      break;
    }
  }

}
