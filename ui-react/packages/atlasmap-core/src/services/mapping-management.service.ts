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

import ky from 'ky';
import log from 'loglevel';
import { Observable, Subscription, Subject, forkJoin, from } from 'rxjs';
import { timeout } from 'rxjs/operators';

import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { MappingModel, MappedField } from '../models/mapping.model';
import { TransitionMode } from '../models/transition.model';
import { MappingDefinition } from '../models/mapping-definition.model';
import { ErrorInfo, ErrorLevel, ErrorType, ErrorScope } from '../models/error.model';
import { FieldAction, Multiplicity } from '../models/field-action.model';

import { MappingSerializer } from '../utils/mapping-serializer';
import { DataMapperUtil } from '../common/data-mapper-util';
import { PaddingField } from '../models/document-definition.model';
import { LookupTableUtil } from '../utils/lookup-table-util';

/**
 * Handles mapping updates. It restores mapping status from backend and reflect in UI,
 * and/or reflect mapping changes caused by the user action made in UI to the backend.
 */
export class MappingManagementService {
  _cfg!: ConfigModel;

  lineRefreshSource = new Subject<void>();
  lineRefresh$ = this.lineRefreshSource.asObservable();
  mappingUpdatedSource = new Subject<void>();
  mappingUpdated$ = this.mappingUpdatedSource.asObservable();

  debugMappingUpdatedSubscription?: Subscription;

  mappingSelectionRequiredSource = new Subject<Field>();
  mappingSelectionRequired$ = this.mappingSelectionRequiredSource.asObservable();

  mappingPreviewInputSource = new Subject<MappingModel>();
  mappingPreviewInput$ = this.mappingPreviewInputSource.asObservable();
  mappingPreviewOutputSource = new Subject<MappingModel>();
  mappingPreviewOutput$ = this.mappingPreviewOutputSource.asObservable();
  mappingPreviewErrorSource = new Subject<ErrorInfo[]>();
  mappingPreviewError$ = this.mappingPreviewErrorSource.asObservable();

  private mappingPreviewInputSubscription?: Subscription;
  private mappingUpdatedSubscription?: Subscription;

  constructor(private api: typeof ky) {}

  get cfg() {
    return this._cfg;
  }

  set cfg(cfg: ConfigModel) {
    this._cfg = cfg;
    if (!this._cfg.logger) {
      this._cfg.logger = log.getLogger('mapping-management');
    }
    if (this._cfg.logger.getLevel() <= this._cfg.logger.levels.DEBUG) {
      this.mappingUpdated$.subscribe(() => {
        if (!this.cfg.mappings) {
          return;
        }
        if (this.cfg.mappings && this.cfg.mappings.activeMapping) {
          this.cfg.logger!.info('mapping updated: ' + JSON.stringify(MappingSerializer.serializeMappings(this.cfg)));
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
      this.cfg.logger!.debug('Runtime Service Ping Request');
      this.api.get(url).json().then((body: any) => {
        this.cfg.logger!.debug(`Runtime Service Ping Response: ${JSON.stringify(body)}`);
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
      // Ref https://github.com/atlasmap/atlasmap/issues/1577
      // for (const mappingName of mappingFileNames) {
        const url: string = baseURL;
        this.cfg.logger!.debug('Mapping Service Request: ' + url);
        const operation = from(this.api.get(url).json());
        operations.push(operation);
      // }

      forkJoin(operations).toPromise().then( async(data: any[]) => {
        if (!data) {
          observer.next(false);
          observer.complete();
          return;
        }
        for (const d of data) {
          this.cfg.logger!.debug(`Mapping Service Response: ${JSON.stringify(d)}`);
          this.cfg.mappings = mappingDefinition;
          MappingSerializer.deserializeMappingServiceJSON(d, this.cfg);
        }
        this.cfg.mappings!.getAllMappings(true).forEach(m => this.updateTransition(m)); // TODO: check this non null operator
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
      const mappingWasRemoved: boolean = this.cfg.mappings!.removeMapping(mappingModel); // TODO: check this non null operator
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
      // TODO: check these non null operator on the mappings
      for (const mapping of this.cfg.mappings!.getAllMappings(true)) {
        const mappingWasRemoved: boolean = this.cfg.mappings!.removeMapping(mapping);
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
        this.cfg.mappings!.removeMapping(mapping); // TODO: check this non null operator
        this.deselectMapping();
      } else {
        this.updateTransition(mapping);
      }
      await this.notifyMappingUpdated();
      resolve(true);
    });
  }

  /**
   * Move the specified mapped field in the specified mapping to the specified index.
   *
   * @param mapping
   * @param insertedMappedField
   * @param targetIndex
   */
  moveMappedFieldTo(mapping: MappingModel, insertedMappedField: MappedField,
    targetIndex: number): void {
    if (!mapping || !insertedMappedField) {
      return;
    }
    insertedMappedField.parsedData.parsedIndex = targetIndex.toString();
    const mappedFields = mapping.getMappedFields(insertedMappedField.isSource());
    mappedFields.splice(mapping.getIndexForMappedField(insertedMappedField)! - 1, 1);
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

  fieldSelected(field: Field, compoundSelection?: boolean, position?: string, offset?: number): void {

    // Keep around for now
    compoundSelection = true;

    if (!field.isTerminal()) {
      field.docDef.populateChildren(field);
      field.docDef.updateFromMappings(this.cfg.mappings!); // TODO: check this non null operator
      field.collapsed = !field.collapsed;
      return;
    }
    let addField = false;
    let removeField = false;
    let mapping: MappingModel | null = this.cfg.mappings!.activeMapping; // TODO: check this non null operator

    // Check compound selection and active mapping status to determine the action
    if (mapping !== null) {
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
              this.cfg.mappings!.removeMapping(mapping); // TODO: check this non null operator
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
      const mappingsForField: MappingModel[] = this.cfg.mappings!.findMappingsForField(field); // TODO: check this non null operator

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

      const mappedFields = this.cfg.mappings!.activeMapping!.getAllMappedFields(); // TODO: check this non null operator
      for (const mappedField of mappedFields) {
        // TODO: check this non null operator
        mappedField.field!.expandToRoot();
      }

      this.notifyMappingUpdated();
      return;
    }

    if (addField) {
      const exclusionReason = this.getFieldSelectionExclusionReason(mapping, field);
      if (exclusionReason != null) {
        this.cfg.errorService.addError(new ErrorInfo({
          message: `The field \'${field.name}\' cannot be selected, ${exclusionReason}.`,
          level: ErrorLevel.ERROR, mapping: mapping, scope: ErrorScope.MAPPING, type: ErrorType.USER}));
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
        this.cfg.mappings!.removeMapping(mapping); // TODO: check this non null operator
        this.deselectMapping();
      } else {
        this.updateTransition(mapping, position, offset);
      }
      this.notifyMappingUpdated();
    }
  }

  getFieldSelectionExclusionReason(mapping: MappingModel, field: Field): string | null {
    if (!field.isTerminal()) {
      return 'field is a parent field';
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
      // TODO: check this non null operator
      if (field.isInCollection() || mappedFields[0].field!.isInCollection()) {
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

    if (field.isInCollection()) {
      if (otherSideMappedFields.length > 0) {
        // TODO: check this non null operator
        if (field.getCollectionCount() !== otherSideMappedFields[0].field!.getCollectionCount()) {
          const target = field.isSource() ? otherSideMappedFields[0].field : field;
          if (target!.getCollectionCount() !== 1) {
            return `source and target must have the same nested collection count or target must have a single nested collection on the path.`;
          }
        }
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
    this.cfg.mappings!.activeMapping = mappingModel; // TODO: check this non null operator
    this.cfg.showMappingDetailTray = true;
    this.mappingUpdatedSource.next();
  }

  deselectMapping(): void {
    this.cfg.showMappingDetailTray = false;
    this.cfg.mappings!.activeMapping = null; // TODO: check this non null operator
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
      this.cfg.logger!.debug(`Process Mapping Preview Request: ${JSON.stringify(payload)}`);
      this.api.put(url, { json: payload }).json().then((body: any) => {
        this.cfg.logger!.debug(`Process Mapping Preview  Response: ${JSON.stringify(body)}`);
        const answer = MappingSerializer.deserializeFieldMapping(body.ProcessMappingResponse.mapping, docRefs, this.cfg, false);
        for (const toWrite of inputFieldMapping.targetFields) {
          for (const toRead of answer.targetFields) {
            // TODO: check these non null operator
            if (toWrite.field!.docDef.id === toRead.parsedData.parsedDocID
                && toWrite.field!.path === toRead.parsedData.parsedPath) {
              // TODO let field component subscribe mappingPreviewOutputSource instead of doing this
              // TODO: check this non null operator
              toWrite.field!.value = toRead.parsedData.parsedValue!;
              const index = answer.targetFields.indexOf(toRead);
              if (index !== -1) {
                answer.targetFields.splice(index, 1);
                break;
              }
            }
          }
        }
        this.mappingPreviewOutputSource.next(answer);
        const audits = MappingSerializer.deserializeAudits(body.ProcessMappingResponse.audits, ErrorType.PREVIEW);
        // TODO: check this non null operator
        if (this.cfg.mappings!.activeMapping === inputFieldMapping) {
          audits.forEach(a => a.mapping = inputFieldMapping);
          this.cfg.errorService.addError(...audits);
        }
        this.mappingPreviewErrorSource.next(audits);
      }).catch((error: any) => {
        if (this.cfg.mappings && this.cfg.mappings.activeMapping &&
            this.cfg.mappings.activeMapping === inputFieldMapping) {
          this.cfg.errorService.addError(new ErrorInfo({message: error, level: ErrorLevel.ERROR,
            mapping: inputFieldMapping, scope: ErrorScope.MAPPING, type: ErrorType.PREVIEW}));
        }
        this.mappingPreviewErrorSource.next([new ErrorInfo({message: error, level: ErrorLevel.ERROR})]);
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
    for (const mapping of this.cfg.mappings!.getAllMappings(true)) {
      for (const mappedField of mapping.getAllFields()) {
        if (!(mappedField instanceof PaddingField) && (mappedField.docDef.id === docId)) {
          this.removeFieldFromAllMappings(mappedField);
          this.cfg.mappings!.removeMapping(mapping); // TODO: check this non null operator
          if (mapping === this.cfg.mappings!.activeMapping) { // TODO: check this non null operator
            this.cfg.mappingService.deselectMapping();
          }
        }
      }
    }
  }

  removeFieldFromAllMappings(field: Field): void {
    // TODO: check this non null operator
    for (const mapping of this.cfg.mappings!.getAllMappings(true)) {
      const mappedField = mapping.getMappedFieldForField(field);
      if (mappedField != null) {
        mapping.removeMappedField(mappedField);
        if (mapping.isEmpty()) {
          this.cfg.mappings!.removeMapping(mapping); // TODO: check this non null operator
        }
      }
    }
  }

  willClearOutSourceFieldsOnTogglingExpression() {
    if (this.cfg.mappings!.activeMapping!.transition.enableExpression) {
      return this.cfg.mappings!.activeMapping!.getFirstCollectionField(true) != null;
    } else {
      return false;
    }
  }

  conditionalMappingExpressionEnabled(): boolean {
    return !!(this.cfg.mappings?.activeMapping?.transition?.enableExpression);
  }

  toggleExpressionMode() {
    if (!this.cfg.mappings || !this.cfg.mappings.activeMapping || !this.cfg.mappings.activeMapping.transition) {
      this.cfg.errorService.addError(new ErrorInfo({
        message: 'Please select a mapping first.', level: ErrorLevel.INFO, scope: ErrorScope.MAPPING, type: ErrorType.USER}));
      return;
    }
    if (this.cfg.mappings.activeMapping.transition.mode === TransitionMode.ONE_TO_MANY) {
      this.cfg.errorService.addError(new ErrorInfo({
        message: `Cannot establish a conditional mapping expression when multiple target fields are selected.
        Please select only one target field and try again.`,
        level: ErrorLevel.WARN, scope: ErrorScope.MAPPING, type: ErrorType.USER, mapping: this.cfg.mappings.activeMapping}));
      return;
    }

    if (this.willClearOutSourceFieldsOnTogglingExpression()) {
      // Clear out source fields, if the mapping contains a source collection
      const activeMapping = this.cfg.mappings.activeMapping;
      activeMapping.sourceFields.splice(0, activeMapping.sourceFields.length);
    }

    this.cfg.mappings.activeMapping.transition.enableExpression
      = !this.cfg.mappings.activeMapping.transition.enableExpression;
    this.updateTransition(this.cfg.mappings.activeMapping);
  }

  /**
   * Invoke the runtime service to both validate and save the current active mapping.
   */
  private async validateMappings(): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      if (this.cfg.initCfg.baseMappingServiceUrl === null || this.cfg.mappings === null) {
        // validation service not configured or required
        resolve(false);
        return;
      }
      this.cfg.errorService.clearValidationErrors();
      const payload: any = MappingSerializer.serializeMappings(this.cfg);
      const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/validate';
      this.cfg.logger!.debug(`Validation Service Request: ${JSON.stringify(payload)}\n`);
      this.api.put(url, { json: payload }).json().then((body: any) => {
        this.cfg.logger!.debug(`Validation Service Response: ${JSON.stringify(body)}\n`);
        if (this.cfg.mappings === null) {
          resolve(false);
          return;
        }
        const errors: ErrorInfo[] = [];

        // This should be eventually turned into mapping entry level validation.
        // https://github.com/atlasmap/atlasmap-ui/issues/116
        if (body && body.Validations && body.Validations.validation) {
          for (const validation of body.Validations.validation) {
            const level: ErrorLevel = validation.status;
            let scope: ErrorScope = validation.scope;
            let validatedMapping: MappingModel | undefined = undefined;
            if (!scope || scope !== ErrorScope.MAPPING || !validation.id) {
              scope = ErrorScope.APPLICATION;
            } else {
              scope = ErrorScope.MAPPING;
              if (this.cfg.mappings && this.cfg.mappings.mappings) {
                validatedMapping = this.cfg.mappings.mappings.find(m => m.uuid === validation.id)!; // TODO: check this non null operator
              }
            }
            errors.push(new ErrorInfo({message: validation.message, level: level, scope: scope,
              mapping: validatedMapping, type: ErrorType.VALIDATION}));
          }
        }
        this.cfg.errorService.addError(...errors);
        resolve(true);
      }).catch(() => {
        this.cfg.logger!.warn('Unable to fetch validation data.');
        resolve(false);
      });
    });
  }

  /**
   * Notify the line machine to update the lines between panels.  Most widgets require a
   * small delay to allow the panel to complete forming so add it here.
   */
  notifyLineRefresh(): void {
    setTimeout(() => {
      this.lineRefreshSource.next();
    }, 1);
  }

  /**
   * Validate and save complete mappings.  Triggered either as an observable or directly.
   */
  async notifyMappingUpdated(): Promise<boolean> {
    return new Promise<boolean>( async(resolve) => {

      if (this.cfg.mappings) {

        const activeMapping: MappingModel = this.cfg.mappings.activeMapping!; // TODO: check this non null operator
        if (activeMapping && (this.cfg.mappings.mappings.indexOf(activeMapping) === -1)) {
          this.cfg.mappings.mappings.push(activeMapping);
        }

        // Validate even if there is no active mapping.  It may be due to a mapping removal.
        await this.validateMappings();
      }
      this.mappingUpdatedSource.next();
      this.notifyLineRefresh();
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
        LookupTableUtil.populateMappingLookupTable(this.cfg.mappings!, mapping); // TODO: check this non null operator
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
         = FieldAction.create(this.cfg.fieldActionService.getActionDefinitionForName('Concatenate', Multiplicity.MANY_TO_ONE)!); // TODO: check this non null operator
        mapping.transition.transitionFieldAction.setArgumentValue('delimiter', ' ');
      }
    } else if (targetMappedFields.length > 1 || targetMappedCollection) {
      mapping.transition.mode = TransitionMode.ONE_TO_MANY;
      if (!mapping.transition.transitionFieldAction
       || mapping.transition.transitionFieldAction.definition.multiplicity !== Multiplicity.ONE_TO_MANY) {
        mapping.transition.transitionFieldAction
         = FieldAction.create(this.cfg.fieldActionService.getActionDefinitionForName('Split', Multiplicity.ONE_TO_MANY)!); // TODO: check this non null operator
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
    const activeMapping = configModel.mappings!.activeMapping!; // TODO: check this non null operator
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
