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

import {
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
} from '../models/error.model';
import { FieldAction, Multiplicity } from '../models/field-action.model';
import { LookupTableData, LookupTableUtil } from '../utils/lookup-table-util';
import { MappedField, MappingModel } from '../models/mapping.model';
import { Subject, Subscription } from 'rxjs';

import { CommonUtil } from '../utils/common-util';
import { ConfigModel } from '../models/config.model';
import { DocumentType } from '../common/config.types';
import { Field } from '../models/field.model';
import { MappingDefinition } from '../models/mapping-definition.model';
import { MappingSerializer } from '../utils/mapping-serializer';
import { MappingUtil } from '../utils/mapping-util';
import { PaddingField } from '../models/document-definition.model';
import { TransitionMode } from '../models/transition.model';
import ky from 'ky';
import log from 'loglevel';

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
  mappingSelectionRequired$ =
    this.mappingSelectionRequiredSource.asObservable();

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
          this.cfg.logger!.info(
            'mapping updated: ' +
              JSON.stringify(MappingSerializer.serializeMappings(this.cfg))
          );
        }
      });
    }
  }

  /**
   * Retrieve current mapping definition JSON file from backend, deserialize it and
   * load it into AtlasMap UI.
   *
   * @param mappingDefinition {@link MappingDefinition}
   * @returns
   */
  fetchMappings(
    _mappingFiles: string[],
    mappingDefinition: MappingDefinition
  ): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      this.cfg.fileService
        .getCurrentMappingJson()
        .then(async (mappingJson: any) => {
          if (!mappingJson) {
            resolve(false);
            return;
          }
          this.cfg.mappings = mappingDefinition;
          MappingSerializer.deserializeMappingServiceJSON(
            mappingJson,
            this.cfg
          );
          this.updateMappingsTransition();
          resolve(true);
        })
        .catch(() => {
          reject(false);
        });
    });
  }

  updateMappingsTransition() {
    this.cfg
      .mappings!.getAllMappings(true)
      .forEach((m) => this.updateTransition(m)); // TODO: check this non null operator
  }

  /**
   * Remove the specified mapping model from the mappings array and update the runtime.
   *
   * @param mappingModel
   */
  async removeMapping(mappingModel: MappingModel): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
      const mappingWasRemoved: boolean =
        this.cfg.mappings!.removeMapping(mappingModel); // TODO: check this non null operator
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
    return new Promise<boolean>(async (resolve) => {
      // TODO: check these non null operator on the mappings
      for (const mapping of this.cfg.mappings!.getAllMappings(true)) {
        this.cfg.mappings!.removeMapping(mapping);
        this.deselectMapping();
      }
      await this.notifyMappingUpdated();
      resolve(true);
    });
  }

  async updateMappedField(mapping: MappingModel): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
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
  moveMappedFieldTo(
    mapping: MappingModel,
    insertedMappedField: MappedField,
    targetIndex: number
  ): void {
    if (!mapping || !insertedMappedField) {
      return;
    }
    insertedMappedField.parsedData.parsedIndex = targetIndex.toString();
    const mappedFields = mapping.getMappedFields(
      insertedMappedField.isSource()
    );
    mappedFields.splice(
      mapping.getIndexForMappedField(insertedMappedField)! - 1,
      1
    );
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
  addPlaceholders(
    count: number,
    mapping: MappingModel,
    basePadIndex: number,
    isSource: boolean
  ) {
    let padField = null;
    for (let i = 0; i < count; i++) {
      padField = new MappedField();
      padField.field = new PaddingField(isSource);
      padField.parsedData.parsedIndex = String(basePadIndex + i);
      if (isSource) {
        mapping.sourceFields.splice(basePadIndex + i, 0, padField);
      } else {
        mapping.targetFields.splice(basePadIndex + i, 0, padField);
      }
    }
  }

  fieldSelected(
    field: Field,
    compoundSelection?: boolean,
    position?: string,
    offset?: number
  ): void {
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
      const mappingsForField: MappingModel[] =
        this.cfg.mappings!.findMappingsForField(field); // TODO: check this non null operator

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

      const mappedFields =
        this.cfg.mappings!.activeMapping!.getAllMappedFields(); // TODO: check this non null operator
      for (const mappedField of mappedFields) {
        // TODO: check this non null operator
        mappedField.field!.expandToRoot();
      }

      this.notifyMappingUpdated();
      return;
    }

    if (addField) {
      const exclusionReason = this.getFieldSelectionExclusionReason(
        mapping,
        field
      );
      if (exclusionReason != null) {
        this.cfg.errorService.addError(
          new ErrorInfo({
            message: `The field '${field.name}' cannot be selected, ${exclusionReason}.`,
            level: ErrorLevel.ERROR,
            mapping: mapping,
            scope: ErrorScope.MAPPING,
            type: ErrorType.USER,
          })
        );
        return;
      }
      mapping.addField(field, false);

      this.updateTransition(mapping, position, offset);
      if (mapping.sourceFields.length > 0 || mapping.targetFields.length > 0) {
        this.notifyMappingUpdated();
      }
      return;
    }

    if (removeField) {
      mapping.getMappedFieldForField(field);
      mapping.removeField(field);
      if (mapping.getUserFieldCount(field) === 1) {
        this.clearExtraPaddingFields(
          mapping.getMappedFields(field.isSource()),
          false
        );
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

  getFieldSelectionExclusionReason(
    mapping: MappingModel,
    field: Field
  ): string | null {
    if (!field.isTerminal()) {
      return 'field is a parent field';
    }

    if (mapping.getAllMappedFields().length === 0) {
      // if mapping hasn't had a field selected yet, allow it
      return null;
    }

    // Non-collection target fields may only be mapped once.
    const existingMappedField = mapping.getMappedTarget(field);
    if (existingMappedField != null && !field.isInCollection) {
      return (
        'it is already the target of another mapping (' +
        existingMappedField +
        '). '
      );
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
    if (
      mapping.transition.enableExpression &&
      !field.isSource() &&
      mapping.getMappedFields(false).length > 0
    ) {
      return 'cannot add multiple target fields when conditional mapping is enabled.';
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
      this.selectMapping(mapping);
      this.notifyMappingUpdated();
    } else {
      this.selectMapping(mapping);
    }
  }

  /**
   * Instantiate a new mapping model.
   */
  newMapping(): void {
    this.deselectMapping();
    const mapping: MappingModel = new MappingModel();
    // Determine type of mapping (i.e., transition mode)
    this.updateTransition(mapping);
    // SelectMapping marks new mapping as active mapping, which is necessary so
    // that it gets added to the existing mappings in notifyMappingUpdated().
    // TODO: this seems very unintuitive, seems like some step to explicitly
    // add the new mapping would make more sense
    this.selectMapping(mapping);
    this.notifyMappingUpdated();
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
    this.cfg.showMappingPreview = true;
    this.mappingPreviewInputSubscription = this.mappingPreviewInput$.subscribe(
      (inputFieldMapping) => {
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
          for (const targetField of inputFieldMapping.getFields(false)) {
            if (targetField.value) {
              hasValue = true;
              break;
            }
          }
        }
        if (!hasValue) {
          return;
        }

        const payload: any = {
          ProcessMappingRequest: {
            jsonType:
              ConfigModel.mappingServicesPackagePrefix +
              '.ProcessMappingRequest',
            mapping: MappingSerializer.serializeFieldMapping(
              this.cfg,
              inputFieldMapping,
              'preview',
              false
            ),
          },
        };
        const docRefs: any = {};
        for (const docRef of this.cfg.getAllDocs()) {
          docRefs[docRef.id] = docRef.uri;
        }

        const url: string =
          this.cfg.initCfg.baseMappingServiceUrl + 'mapping/process';
        this.cfg.logger!.debug(
          `Process Mapping Preview Request: ${JSON.stringify(payload)}`
        );
        this.api
          .put(url, { json: payload })
          .json()
          .then((body: any) => {
            this.cfg.logger!.debug(
              `Process Mapping Preview  Response: ${JSON.stringify(body)}`
            );
            const answer = MappingSerializer.deserializeFieldMapping(
              body.ProcessMappingResponse.mapping,
              docRefs,
              this.cfg,
              false
            );
            for (const toWrite of inputFieldMapping.targetFields) {
              for (const toRead of answer.targetFields) {
                // TODO: check these non null operator
                if (
                  toWrite.field?.docDef?.id === toRead.parsedData.parsedDocID &&
                  toWrite.field?.path === toRead.parsedData.parsedPath
                ) {
                  // TODO let field component subscribe mappingPreviewOutputSource instead of doing this
                  // TODO: check this non null operator
                  toWrite.field.value = toRead.parsedData.parsedValue!;
                  const index = answer.targetFields.indexOf(toRead);
                  if (index !== -1) {
                    answer.targetFields.splice(index, 1);
                    break;
                  }
                }
              }
            }
            this.mappingPreviewOutputSource.next(answer);
            const audits = MappingSerializer.deserializeAudits(
              body.ProcessMappingResponse.audits,
              ErrorType.PREVIEW
            );
            // TODO: check this non null operator
            if (this.cfg.mappings!.activeMapping === inputFieldMapping) {
              audits.forEach((a) => (a.mapping = inputFieldMapping));
              this.cfg.errorService.addError(...audits);
            }
            this.mappingPreviewErrorSource.next(audits);
          })
          .catch((error: any) => {
            if (
              this.cfg.mappings &&
              this.cfg.mappings.activeMapping &&
              this.cfg.mappings.activeMapping === inputFieldMapping
            ) {
              this.cfg.errorService.addError(
                new ErrorInfo({
                  message: error,
                  level: ErrorLevel.ERROR,
                  mapping: inputFieldMapping,
                  scope: ErrorScope.MAPPING,
                  type: ErrorType.PREVIEW,
                })
              );
            }
            this.mappingPreviewErrorSource.next([
              new ErrorInfo({ message: error, level: ErrorLevel.ERROR }),
            ]);
          });
      }
    );

    this.mappingUpdatedSubscription = this.mappingUpdated$.subscribe(() => {
      if (!this.cfg || !this.cfg.mappings || !this.cfg.mappings.activeMapping) {
        return;
      }
      if (this.cfg.mappings.activeMapping.isFullyMapped()) {
        this.mappingPreviewInputSource.next(this.cfg.mappings.activeMapping);
      }
    });
  }

  /**
   * On mapping preview disable, clear any preview values and unsubscribe from
   * both the mapping-updated and mapping-preview subscriptions.
   */
  disableMappingPreview(): void {
    let mappedValueCleared = false;
    this.cfg.showMappingPreview = false;

    // Clear any preview values on mapping preview disable.
    if (this.cfg.mappings?.activeMapping?.isFullyMapped()) {
      for (const mapping of this.cfg.mappings!.getAllMappings(true)) {
        for (const mappedField of mapping.getAllFields()) {
          if (mappedField.value?.length > 0 && !mappedField.isConstant()) {
            mappedField.value = '';
            mappedValueCleared = true;
          }
        }
      }
    }
    if (mappedValueCleared) {
      this.notifyMappingUpdated();
    }
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
        if (
          !(mappedField instanceof PaddingField) &&
          mappedField.docDef.id === docId
        ) {
          this.removeFieldFromAllMappings(mappedField);
          if (
            mapping.sourceFields.length === 0 ||
            mapping.targetFields.length === 0
          ) {
            this.cfg.mappings!.removeMapping(mapping); // TODO: check this non null operator
            if (mapping === this.cfg.mappings!.activeMapping) {
              // TODO: check this non null operator
              this.cfg.mappingService.deselectMapping();
            }
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
    if (this.cfg.mappings?.activeMapping?.transition.enableExpression) {
      return (
        this.cfg.mappings.activeMapping.getFirstCollectionField(true) != null
      );
    } else {
      return false;
    }
  }

  conditionalMappingExpressionEnabled(): boolean {
    return !!this.cfg.mappings?.activeMapping?.transition?.enableExpression;
  }

  toggleExpressionMode() {
    if (
      !this.cfg.mappings ||
      !this.cfg.mappings.activeMapping ||
      !this.cfg.mappings.activeMapping.transition
    ) {
      this.cfg.errorService.addError(
        new ErrorInfo({
          message: 'Please select a mapping first.',
          level: ErrorLevel.INFO,
          scope: ErrorScope.MAPPING,
          type: ErrorType.USER,
        })
      );
      return;
    }
    if (
      this.cfg.mappings.activeMapping.transition.mode ===
      TransitionMode.ONE_TO_MANY
    ) {
      this.cfg.errorService.addError(
        new ErrorInfo({
          message: `Cannot establish a conditional mapping expression when multiple target fields are selected.
        Please select only one target field and try again.`,
          level: ErrorLevel.WARN,
          scope: ErrorScope.MAPPING,
          type: ErrorType.USER,
          mapping: this.cfg.mappings.activeMapping,
        })
      );
      return;
    }

    if (this.willClearOutSourceFieldsOnTogglingExpression()) {
      // Clear out source fields, if the mapping contains a source collection
      const activeMapping = this.cfg.mappings.activeMapping;
      activeMapping.sourceFields.splice(0, activeMapping.sourceFields.length);
    }

    this.cfg.mappings.activeMapping.transition.enableExpression =
      !this.cfg.mappings.activeMapping.transition.enableExpression;
    this.updateTransition(this.cfg.mappings.activeMapping);
    if (this.cfg.mappings.activeMapping.transition.expression) {
      this.cfg.mappings.activeMapping.transition.expression.expressionUpdatedSource.next();
    }
  }

  /**
   * Invoke the runtime service to save the current active mapping.
   * No validation will occur.
   */
  async updateMappings(payload: any): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      if (
        this.cfg.initCfg.baseMappingServiceUrl === null ||
        this.cfg.mappings === null
      ) {
        resolve(false);
        return;
      }

      const url: string =
        this.cfg.initCfg.baseMappingServiceUrl +
        'mapping/' +
        DocumentType.JSON +
        '/' +
        this.cfg.mappingDefinitionId;
      this.cfg.logger!.debug(
        `Mapping Update Service Request: ${JSON.stringify(payload)}\n`
      );
      this.api
        .put(url, { json: payload })
        .then((body: any) => {
          this.cfg.logger!.debug(
            `Mapping Update Service Response: ${JSON.stringify(body)}\n`
          );
          resolve(true);
        })
        .catch((error: any) => {
          this.cfg.errorService.addError(
            new ErrorInfo({
              message: 'Unable to update mappings file. ' + error,
              level: ErrorLevel.ERROR,
              scope: ErrorScope.MAPPING,
              type: ErrorType.INTERNAL,
            })
          );
          resolve(false);
        });
    });
  }

  /**
   * Invoke the runtime service to validate the current active mapping.
   */
  private async validateMappings(payload: any): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      if (
        this.cfg.initCfg.baseMappingServiceUrl === null ||
        this.cfg.mappings === null
      ) {
        // validation service not configured or required
        resolve(false);
        return;
      }

      this.cfg.errorService.clearValidationErrors();
      const url: string =
        this.cfg.initCfg.baseMappingServiceUrl +
        'mapping/validate/' +
        this.cfg.mappingDefinitionId;
      this.cfg.logger!.debug(
        `Validation Service Request: ${JSON.stringify(payload)}\n`
      );
      this.api
        .put(url, { json: payload })
        .json()
        .then((body: any) => {
          this.cfg.logger!.debug(
            `Validation Service Response: ${JSON.stringify(body)}\n`
          );
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
                  validatedMapping = this.cfg.mappings.mappings.find(
                    (m) => m.uuid === validation.id
                  )!; // TODO: check this non null operator
                }
              }
              errors.push(
                new ErrorInfo({
                  message: validation.message,
                  level: level,
                  scope: scope,
                  mapping: validatedMapping,
                  type: ErrorType.VALIDATION,
                })
              );
            }
          }
          this.cfg.errorService.addError(...errors);
          resolve(true);
        })
        .catch(() => {
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
   * Validate and save complete mappings.  Triggered either as an observable
   * or directly.
   */
  async notifyMappingUpdated(): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
      if (this.cfg.mappings) {
        const activeMapping: MappingModel = this.cfg.mappings.activeMapping!; // TODO: check this non null operator
        if (
          activeMapping &&
          this.cfg.mappings.mappings.indexOf(activeMapping) === -1
        ) {
          this.cfg.mappings.mappings.push(activeMapping);
        }
        const payload: any = MappingSerializer.serializeMappings(this.cfg);
        if (await this.validateMappings(payload)) {
          await this.updateMappings(payload);
        }
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
  private updateTransition(
    mapping: MappingModel,
    position?: string,
    offset?: number
  ): void {
    for (const field of mapping.getAllFields()) {
      if (field.enumeration) {
        mapping.transition.mode = TransitionMode.ENUM;
        LookupTableUtil.populateMappingLookupTable(this.cfg.mappings!, mapping); // TODO: check this non null operator
        return;
      }
    }

    const sourceMappedFields = mapping.getMappedFields(true);
    const sourceMappedCollection = MappingUtil.hasMappedCollection(
      mapping,
      true
    );
    const targetMappedFields = mapping.getMappedFields(false);
    const targetMappedCollection = MappingUtil.hasMappedCollection(
      mapping,
      false
    );

    if (sourceMappedCollection && targetMappedCollection) {
      mapping.transition.mode = TransitionMode.FOR_EACH;
    } else if (
      sourceMappedFields.length > 1 ||
      sourceMappedCollection ||
      (mapping.transition.enableExpression && sourceMappedFields.length > 1)
    ) {
      mapping.transition.mode = TransitionMode.MANY_TO_ONE;
      if (
        !mapping.transition.enableExpression &&
        (!mapping.transition.transitionFieldAction ||
          !mapping.transition.transitionFieldAction.definition ||
          mapping.transition.transitionFieldAction.definition.multiplicity !==
            Multiplicity.MANY_TO_ONE)
      ) {
        mapping.transition.transitionFieldAction = FieldAction.create(
          this.cfg.fieldActionService.getActionDefinitionForName(
            'Concatenate',
            Multiplicity.MANY_TO_ONE
          )!
        ); // TODO: check this non null operator
        mapping.transition.transitionFieldAction.setArgumentValue(
          'delimiter',
          ' '
        );
        mapping.transition.transitionFieldAction.setArgumentValue(
          'delimitingEmptyValues',
          'true'
        );
      }
    } else if (targetMappedFields.length > 1 || targetMappedCollection) {
      mapping.transition.mode = TransitionMode.ONE_TO_MANY;
      if (
        !mapping.transition.transitionFieldAction ||
        mapping.transition.transitionFieldAction.definition.multiplicity !==
          Multiplicity.ONE_TO_MANY
      ) {
        mapping.transition.transitionFieldAction = FieldAction.create(
          this.cfg.fieldActionService.getActionDefinitionForName(
            'Split',
            Multiplicity.ONE_TO_MANY
          )!
        ); // TODO: check this non null operator
        mapping.transition.transitionFieldAction.setArgumentValue(
          'delimiter',
          ' '
        );
      }
    } else {
      mapping.transition.mode = TransitionMode.ONE_TO_ONE;
      mapping.transition.transitionFieldAction = null;
    }

    // Disable multiplicity field actions if expression box is enabled.
    if (mapping.transition.enableExpression) {
      mapping.transition.transitionFieldAction = null;

      // Update conditional expression field references.
      mapping.transition.expression?.updateFieldReference(
        mapping,
        position,
        offset
      );
    }
  }

  /**
   * Remove any trailing padding fields for the mapped field array.  This occurs when a user moves
   * a mapped element above the last padding field.
   *
   * @param mappedFields
   * @param trailing - Remove trailing padding fields only
   */
  private clearExtraPaddingFields(
    mappedFields: MappedField[],
    trailing: boolean
  ): void {
    let index = 0;
    let mField = null;

    for (index = mappedFields.length - 1; index >= 0; index--) {
      mField = mappedFields[index];
      if (mField.isPadField()) {
        CommonUtil.removeItemFromArray(mField, mappedFields);
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
  executeFieldSearch(
    configModel: ConfigModel,
    filter: string,
    isSource: boolean
  ): string[][] {
    const activeMapping = configModel.mappings!.activeMapping!; // TODO: check this non null operator
    const formattedFields: string[][] = [];
    let fields: Field[] = [];
    for (const docDef of configModel.getDocs(isSource)) {
      fields = fields.concat(docDef.getTerminalFields());
      fields = fields.concat(docDef.getComplexFields());
    }
    Field.alphabetizeFields(fields);
    let documentName = '';
    let fieldCount = -1;

    for (const field of fields) {
      const formattedField: string[] = [''];
      let displayName =
        field == null ? '' : field.getFieldLabel(configModel.showTypes, true);

      if (
        filter == null ||
        filter === '' ||
        displayName.toLowerCase().indexOf(filter.toLowerCase()) !== -1
      ) {
        if (
          !configModel.mappingService.isFieldSelectable(activeMapping, field) &&
          field.type !== 'COMPLEX'
        ) {
          continue;
        }
        if (documentName !== field.docDef.name) {
          if (fieldCount === 0) {
            formattedFields.pop();
            continue;
          } else {
            const documentField = [''];
            documentName = field.docDef.name;
            documentField[0] = documentName;
            documentField[1] = '';
            fieldCount = 0;
            formattedFields.push(documentField);
          }
        }
        displayName = CommonUtil.extractDisplayPath(field.path, 100);
        formattedField[0] = displayName;
        if (field.isProperty() && field.scope) {
          formattedField[1] = field.path + ' <' + field.scope + '>';
        } else {
          formattedField[1] = field.path;
        }
        fieldCount++;
        formattedFields.push(formattedField);
      }
      if (formattedFields.length > 19) {
        break;
      }
    }
    return formattedFields;
  }

  /**
   * Get the enumeration values for the specified mapping and return it in
   * the form of a lookup table.
   *
   * @param cfg
   * @param mapping
   */
  getEnumerationValues(
    cfg: ConfigModel,
    mapping: MappingModel
  ): LookupTableData[] {
    return LookupTableUtil.getEnumerationValues(cfg, mapping);
  }

  /**
   * Set the enumeration field value based on the specified enumeration value index.
   *
   * @param enumerationField
   * @param enumerationValue
   */
  setEnumFieldValue(enumerationField: Field, enumerationValue: number) {
    enumerationField.enumIndexValue = enumerationValue;
  }

  /**
   * Update the enumeration values for the specified mapping with the specified
   * enumeration values.
   *
   * @param cfg
   * @param mapping
   * @param enumerationValues
   */
  updateEnumerationValues(
    cfg: ConfigModel,
    mapping: MappingModel,
    enumerationValues: LookupTableData[]
  ): void {
    LookupTableUtil.updateEnumerationValues(cfg, mapping, enumerationValues);
    this.notifyMappingUpdated();
  }

  /**
   * Return true if the specified mapping is an enumeration mapping, false otherwise.
   *
   * @param mapping
   */
  isEnumerationMapping(mapping: MappingModel): boolean {
    return mapping.transition.mode === TransitionMode.ENUM;
  }

  getRuntimeVersion(): Promise<string> {
    const url = this.cfg.initCfg.baseMappingServiceUrl + 'version';
    return new Promise<string>((resolve, reject) => {
      this.api
        .get(url)
        .json()
        .then((body: any) => {
          this.cfg.logger!.debug(
            `Runtime Service Version Response: ${JSON.stringify(body)}`
          );
          resolve(body.string);
        })
        .catch((error) => {
          reject(error);
        });
    });
  }
}
