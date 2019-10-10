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
import { FieldAction, Multiplicity } from '../models/field-action.model';

import { MappingSerializer } from '../utils/mapping-serializer';
import { DataMapperUtil } from '../common/data-mapper-util';
import { PaddingField } from '../models/document-definition.model';
import { LookupTableUtil } from '../utils/lookup-table-util';

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
        if (this.cfg.mappings && this.cfg.mappings.activeMapping) {
          this.cfg.logger.debug('mapping updated: ' + JSON.stringify(MappingSerializer.serializeMappings(this.cfg)));
        }
      });
    }
  }

  /**
   * Return true if the runtime service is available, false otherwise.
   */
  async runtimeServiceActive(): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'ping';
      this.cfg.logger.trace('Runtime Service Ping Request');
      this.http.get(url, { headers: this.headers }).toPromise().then((body: string) => {
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Runtime Service Ping Response: ${JSON.stringify(body)}`);
        }
        if (body) {
          if (JSON.stringify(body).match('pong')) {
            resolve(true);
            return;
          }
        }
        resolve(false);
      }).catch((error: any) => {
        reject(error);
      });
    });
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

      forkJoin(operations).toPromise().then( async(data: any[]) => {
        if (!data) {
          observer.next(false);
          observer.complete();
          return;
        }
        for (const d of data) {
          if (this.cfg.isTraceEnabled()) {
            this.cfg.logger.trace(`Mapping Service Response: ${JSON.stringify(d)}`);
          }
          this.cfg.mappings = mappingDefinition;
          MappingSerializer.deserializeMappingServiceJSON(d, this.cfg);
        }
        this.cfg.mappings.getAllMappings(true).forEach(m => this.updateTransition(m));
        observer.next(true);
        observer.complete();
      }).catch((error: any) => {
          observer.error(error);
          observer.complete();
        });
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
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
        await this.notifyMappingUpdated();
      } else {
        this.deselectMapping();
      }
      resolve(true);
    });
  }

  /**
   * Remove all mappings from the current session.
   */
  async removeAllMappings(): Promise<boolean> {
    return new Promise<boolean>( async(resolve) => {
      for (const mapping of this.cfg.mappings.getAllMappings(true)) {
        const mappingWasRemoved: boolean = this.cfg.mappings.removeMapping(mapping);
        if (mappingWasRemoved) {
          this.deselectMapping();
        } else {
          this.deselectMapping();
        }
      }
      await this.notifyMappingUpdated();
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
      await this.notifyMappingUpdated();
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
    insertedMappedField.parsedData.parsedIndex = targetIndex.toString();
    const mappedFields = mapping.getMappedFields(insertedMappedField.isSource());
    mappedFields.splice(mapping.getIndexForMappedField(insertedMappedField) - 1, 1);
    mappedFields.splice(targetIndex - 1, 0, insertedMappedField);
    this.clearExtraPaddingFields(mappedFields, true);
    this.notifyMappingUpdated();
  }

  /**
   * Given an index range, fill in the mappings gap with place-holder fields.
   *
   * @param count - number of padding fields to add
   * @param mapping - mapping to modify
   * @param basePadIndex - 0 based
   * @param isSource
   */
  addPlaceholders(count: number, mapping: MappingModel, basePadIndex: number, isSource: boolean) {
    let padField = null;
    for (let i = 0; i < count; i++) {
      padField = new MappedField;
      padField.field = new PaddingField(isSource);
      padField.parsedData.parsedIndex = String(basePadIndex + i);
      if (isSource) {
        mapping.sourceFields.splice(basePadIndex + i, 0, padField);
      } else {
        mapping.targetFields.splice(basePadIndex + i, 0, padField);
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

      const mappedFields = this.cfg.mappings.activeMapping.getAllMappedFields();
      for (const mappedField of mappedFields) {
        mappedField.field.expandToRoot();
      }

      this.notifyMappingUpdated();
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
      if (mapping.sourceFields.length > 0 && mapping.targetFields.length > 0) {
        this.notifyMappingUpdated();
      }
      return;
    }

    if (removeField) {
      mapping.getMappedFieldForField(field);
      mapping.removeField(field);
      if (mapping.getUserFieldCount(field) === 1) {
        this.clearExtraPaddingFields(mapping.getMappedFields(field.isSource()), false);
      }
      if (mapping.isEmpty()) {
        this.cfg.mappings.removeMapping(mapping);
        this.deselectMapping();
      } else {
        this.updateTransition(mapping, position, offset);
      }
      this.notifyMappingUpdated();
    }
  }

  getFieldSelectionExclusionReason(mapping: MappingModel, field: Field): string {
    if (!field.isTerminal()) {
      return 'field is a parent field';
    }
    if (field.isInNestedCollection()) {
      return 'Nested collection is not supported';
    }

    if (mapping.getAllMappedFields().length === 0) { // if mapping hasn't had a field selected yet, allow it
      return null;
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
      return null;
    }
    // enums are not selectable in these modes
    if (field.enumeration) {
      return 'Enumeration fields are not valid for this mapping';
    }
    // Expression mapping
    if (mapping.transition.enableExpression && !field.isSource() && mapping.getMappedFields(false).length > 0) {
      return 'cannot add multiple target fields when conditional mapping is enabled.';
    }

    // Check multiplicity restrictions
    const mappedFields = mapping.getMappedFields(field.isSource());
    const otherSideMappedFields = mapping.getMappedFields(!field.isSource());
    const direction = field.isSource() ? 'source' : 'target';
    const otherDirection = !field.isSource() ? 'source' : 'target';
    if (mappedFields.length > 0) {
      if (field.isInCollection() || mappedFields[0].field.isInCollection()) {
        return 'a collection field cannot be a part of compound selection.';
      } else if (otherSideMappedFields.length > 1) {
        return `multiple ${direction} fields cannot be added into this
          mapping. Only one Source field or Target field could be made multiple.`;
      }
    } else {
      if (otherSideMappedFields.length > 1 && field.isInCollection()) {
        return `a collection field cannot be selected as a ${direction} field
         when multiple ${otherDirection} fields are already selected.`;
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
      this.notifyMappingUpdated();
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

  /**
   * Remove any mappings referencing the specified document ID.
   *
   * @param docId - Specified document ID
   * @param cfg
   */
  removeDocumentReferenceFromAllMappings(docId: string) {
    for (const mapping of this.cfg.mappings.getAllMappings(true)) {
      for (const mappedField of mapping.getAllFields()) {
        if (!(mappedField instanceof PaddingField) && (mappedField.docDef.id === docId)) {
          this.removeFieldFromAllMappings(mappedField);
          this.cfg.mappings.removeMapping(mapping);
          if (mapping === this.cfg.mappings.activeMapping) {
            this.cfg.mappingService.deselectMapping();
          }
        }
      }
    }
  }

  removeFieldFromAllMappings(field: Field): void {
    for (const mapping of this.cfg.mappings.getAllMappings(true)) {
      const mappedField: MappedField = mapping.getMappedFieldForField(field);
      if (mappedField != null) {
        mapping.removeMappedField(mappedField);
        if (mapping.isEmpty()) {
          this.cfg.mappings.removeMapping(mapping);
        }
      }
    }
  }

  toggleExpressionMode() {
    if (!this.cfg.mappings || !this.cfg.mappings.activeMapping || !this.cfg.mappings.activeMapping.transition) {
      this.cfg.errorService.info('Please select a mapping first.', null);
      return;
    }
    if (this.cfg.mappings.activeMapping.getFirstCollectionField(false)) {
      this.cfg.errorService.warn(
        `Cannot establish a conditional mapping expression when referencing a target collection field.`, null);
      return;
    } else if (this.cfg.mappings.activeMapping.getFirstCollectionField(true)) {
      this.cfg.errorService.warn(
        `Cannot establish a conditional mapping expression when referencing a source collection field.`, null);
      return;
    } else if (this.cfg.mappings.activeMapping.transition.mode === TransitionMode.ONE_TO_MANY) {
      this.cfg.errorService.warn(
        `Cannot establish a conditional mapping expression when multiple target fields are selected.
        Please select only one target field and try again.`, null);
      return;
    }

    this.cfg.mappings.activeMapping.transition.enableExpression
      = !this.cfg.mappings.activeMapping.transition.enableExpression;
    this.updateTransition(this.cfg.mappings.activeMapping);
  }

  /**
   * Invoke the runtime service to both validate and save the current active mapping.
   */
  private async validateMappings(): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      this.cfg.errorService.clearValidationErrors();
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

  /**
   * Validate and save complete mappings.  Triggered either as an observable or directly.
   */
  async notifyMappingUpdated(): Promise<boolean> {
    return new Promise<boolean>( async(resolve, reject) => {

      if (this.cfg.mappings) {

        const activeMapping: MappingModel = this.cfg.mappings.activeMapping;
        if (activeMapping && (this.cfg.mappings.mappings.indexOf(activeMapping) === -1)) {
          this.cfg.mappings.mappings.push(activeMapping);
        }

        // Validate even if there is no active mapping.  It may be due to a mapping removal.
        await this.validateMappings();
      }
      this.mappingUpdatedSource.next();
      resolve(true);
    });
  }

  /**
   * Update mode transition from a single mapping to multiple-mappings and back.
   *
   * @param mapping
   * @param position
   * @param offset
   */
  private updateTransition(mapping: MappingModel, position?: string, offset?: number): void {
    for (const field of mapping.getAllFields()) {
      if (field.enumeration) {
        mapping.transition.mode = TransitionMode.ENUM;
        LookupTableUtil.populateMappingLookupTable(this.cfg.mappings, mapping);
        return;
      }
    }

    const sourceMappedFields = mapping.getMappedFields(true);
    const sourceMappedCollection = (mapping.isFullyMapped() && sourceMappedFields[0].field
        && sourceMappedFields[0].field.isInCollection());
    const targetMappedFields = mapping.getMappedFields(false);
    const targetMappedCollection = (mapping.isFullyMapped() && targetMappedFields[0].field
        && targetMappedFields[0].field.isInCollection());

    if (sourceMappedCollection && targetMappedCollection) {
      mapping.transition.mode = TransitionMode.FOR_EACH;
    } else if (sourceMappedFields.length > 1 || sourceMappedCollection || mapping.transition.enableExpression) {
      mapping.transition.mode = TransitionMode.MANY_TO_ONE;
      if (!mapping.transition.enableExpression
       && ( !mapping.transition.transitionFieldAction
         || mapping.transition.transitionFieldAction.definition.multiplicity !== Multiplicity.MANY_TO_ONE)) {
        mapping.transition.transitionFieldAction
         = FieldAction.create(this.cfg.fieldActionService.getActionDefinitionForName('Concatenate', Multiplicity.MANY_TO_ONE));
        mapping.transition.transitionFieldAction.setArgumentValue('delimiter', ' ');
      }
    } else if (targetMappedFields.length > 1 || targetMappedCollection) {
      mapping.transition.mode = TransitionMode.ONE_TO_MANY;
      if (!mapping.transition.transitionFieldAction
       || mapping.transition.transitionFieldAction.definition.multiplicity !== Multiplicity.ONE_TO_MANY) {
        mapping.transition.transitionFieldAction
         = FieldAction.create(this.cfg.fieldActionService.getActionDefinitionForName('Split', Multiplicity.ONE_TO_MANY));
        mapping.transition.transitionFieldAction.setArgumentValue('delimiter', ' ');
      }
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
   * @param trailing - Remove trailing padding fields only
   */
  private clearExtraPaddingFields(mappedFields: MappedField[], trailing: boolean): void {
    let index = 0;
    let mField = null;

    for (index = mappedFields.length - 1; index >= 0; index--) {
      mField = mappedFields[index];
      if (mField.isPadField()) {
        DataMapperUtil.removeItemFromArray(mField, mappedFields);
        continue;
      }
      if (trailing) {
        break;
      }
    }
  }

  /**
   * Return an array of strings representing display names of mapping fields based on the
   * specified filter.
   *
   * @param filter
   */
  executeFieldSearch(configModel: ConfigModel, filter: string, isSource: boolean): any[] {
    const activeMapping = configModel.mappings.activeMapping;
    const formattedFields: any[] = [];
    let fields: Field[] = [];
    for (const docDef of configModel.getDocs(isSource)) {
      fields = fields.concat(docDef.getTerminalFields());
    }
    let documentName = '';
    let fieldCount = -1;
    let formattedField = null;

    for (const field of fields) {
      let displayName = (field == null) ? '' : field.getFieldLabel(configModel.showTypes, true);

      if (filter == null || filter === '' || displayName.toLowerCase().indexOf(filter.toLowerCase()) !== -1) {
        if (!configModel.mappingService.isFieldSelectable(activeMapping, field)) {
          continue;
        }
        if (documentName !== field.docDef.name) {
          if (fieldCount === 0) {
            formattedFields.pop();
            continue;
          } else {
            documentName = field.docDef.name;
            formattedField = { 'field': null, 'displayName': documentName };
            fieldCount = 0;
            formattedFields.push(formattedField);
          }
        }
        displayName = DataMapperUtil.extractDisplayPath(field.path, 100);
        formattedField = { 'field': field, 'displayName': displayName };
        fieldCount++;
        formattedFields.push(formattedField);
      }
      if (formattedFields.length > 19) {
        break;
      }
    }
    return formattedFields;
  }
}
