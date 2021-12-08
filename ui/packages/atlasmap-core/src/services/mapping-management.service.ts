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
import {
  IAtlasMappingContainer,
  IValidationsContainer,
} from '../contracts/mapping';
import { LookupTableData, LookupTableUtil } from '../utils/lookup-table-util';
import { MappedField, MappingModel } from '../models/mapping.model';
import { Subject, Subscription } from 'rxjs';

import { CommonUtil } from '../utils/common-util';
import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { FieldAction } from '../models/field-action.model';
import { FieldType } from '../contracts/common';
import { MappingDefinition } from '../models/mapping-definition.model';
import { MappingSerializer } from '../utils/mapping-serializer';
import { MappingUtil } from '../utils/mapping-util';
import { Multiplicity } from '../contracts/field-action';
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
    return new Promise<boolean>((resolve) => {
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
        .catch((error) => {
          this.cfg.errorService.addBackendError(
            'Failed to load mapping definition from backend:',
            error
          );
          resolve(false);
        });
    });
  }

  updateMappingsTransition() {
    this.cfg
      .mappings!.getAllMappings(true)
      .forEach((m) => this.updateTransition(m)); // TODO: check this non null operator
  }

  updateActiveMappingTransition() {
    if (this.cfg.mappings?.activeMapping) {
      this.updateTransition(this.cfg.mappings?.activeMapping);
    }
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
      if (isSource) {
        mapping.sourceFields.splice(basePadIndex + i, 0, padField);
      } else {
        mapping.targetFields.splice(basePadIndex + i, 0, padField);
      }
    }
  }

  addFieldToActiveMapping(field: Field): void {
    let mapping: MappingModel | null = this.cfg.mappings!.activeMapping;
    if (!mapping) {
      this.cfg.errorService.addError(
        new ErrorInfo({
          message: `No mapping is selected to add a field '${field.name}'`,
          level: ErrorLevel.ERROR,
          scope: ErrorScope.MAPPING,
          type: ErrorType.USER,
        })
      );
      return;
    }

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

    this.updateTransition(mapping);
    if (mapping.sourceFields.length > 0 || mapping.targetFields.length > 0) {
      this.notifyMappingUpdated();
    }
    return;
  }

  /**
   * @FIXME Migrate with isFieldAddableToActiveMapping() - https://github.com/atlasmap/atlasmap/issues/3442
   */
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

  /**
   * @FIXME Migrate with isFieldAddableToActiveMapping() - https://github.com/atlasmap/atlasmap/issues/3442
   */
  isFieldSelectable(mapping: MappingModel, field: Field): boolean {
    return this.getFieldSelectionExclusionReason(mapping, field) == null;
  }

  /**
   * Return true if it's possible to add a source or target field to the active
   * mapping, false otherwise.
   * @param isSource true if it's source field, or false
   * @returns
   */
  canAddToActiveMapping(isSource: boolean): boolean {
    const selectedMapping = this.cfg.mappings?.activeMapping;
    if (
      !selectedMapping ||
      (selectedMapping.transition.mode === TransitionMode.ENUM &&
        selectedMapping.sourceFields.length > 0 &&
        selectedMapping.targetFields.length > 0)
    ) {
      return false;
    }
    if (
      selectedMapping.sourceFields.length <= 1 &&
      selectedMapping.targetFields.length <= 1
    ) {
      return true;
    } else if (
      isSource &&
      (selectedMapping.targetFields.length <= 1 ||
        selectedMapping.sourceFields.length === 0)
    ) {
      return true;
    } else if (
      !isSource &&
      (selectedMapping.sourceFields.length <= 1 ||
        selectedMapping.targetFields.length === 0)
    ) {
      return true;
    }
    return false;
  }

  /**
   * Return true if it's possible to add the specified field to the
   * specified mapping, or if it's possible to create a new mapping
   * with the specified field when the specified mapping is null or undefined,
   * false otherwise.
   * @param field field
   * @param mapping a mapping to add the field
   */
  isFieldAddableToMapping(
    field: Field,
    mapping?: MappingModel | null
  ): boolean {
    if (
      !field ||
      !field.isTerminal() ||
      field.type === FieldType.UNSUPPORTED ||
      (mapping &&
        mapping.transition.mode === TransitionMode.ENUM &&
        mapping.sourceFields.length > 0 &&
        mapping.targetFields.length > 0)
    ) {
      return false;
    }

    // non-collection target field can't participate to more than one mapping
    if (
      !field.isSource() &&
      !field.isCollection &&
      !field.isInCollection() &&
      this.cfg.mappings?.findMappingsForField(field).length !== 0
    ) {
      return false;
    }

    if (!mapping) {
      return true;
    }
    if (mapping.transition.mode === TransitionMode.EXPRESSION) {
      return field.isSource() || mapping.targetFields.length === 0;
    }
    // skip if already in the mapping
    if (
      (field.isSource() &&
        mapping.sourceFields.find((f) => f.field === field)) ||
      (!field.isSource() && mapping.targetFields.find((f) => f.field === field))
    ) {
      return false;
    }
    // adding a collection field
    if (field.isCollection || field.isInCollection()) {
      return field.isSource()
        ? mapping.sourceFields.length === 0
        : mapping.targetFields.length === 0;
    }

    if (field.isSource()) {
      // adding a source non-collection field
      if (mapping.sourceFields.length === 0) {
        return true;
      }
      return (
        !mapping.sourceFields.find(
          (f) => f.field?.isCollection || f.field?.isInCollection()
        ) && mapping.targetFields.length <= 1
      );
    } else {
      // adding a target non-collection field
      if (mapping.targetFields.length === 0) {
        return true;
      }
      return (
        !mapping.targetFields.find(
          (f) => f.field?.isCollection || f.field?.isInCollection()
        ) && mapping.sourceFields.length <= 1
      );
    }
  }

  /**
   * Return true if it's possible to add the specified source field to the
   * active mapping, false otherwise.
   * @param field field
   * @param dropTarget a destination field to drop if it's drag & drop
   */
  isFieldAddableToActiveMapping(field: Field): boolean {
    return this.isFieldAddableToMapping(
      field,
      this.cfg.mappings?.activeMapping
    );
  }

  /**
   * Return true if it's possible to drag a field to the destination field, false otherwise.
   * @param src dragging field
   * @param dest a destination field to drop
   */
  isFieldDragAndDropAllowed(src?: Field, dest?: Field): boolean {
    if (
      !src ||
      !dest ||
      (src.isSource() && dest.isSource()) ||
      (!src.isSource() && !dest.isSource())
    ) {
      return false;
    }
    const source = src.isSource() ? src : dest;
    const target = src.isSource() ? dest : src;

    const activeMapping = this.cfg.mappings?.activeMapping;
    if (activeMapping) {
      if (
        activeMapping.isFieldMapped(source) &&
        activeMapping.isFieldMapped(target)
      ) {
        return false;
      }
      if (activeMapping.isFieldMapped(source)) {
        return this.isFieldAddableToMapping(target, activeMapping);
      }
      if (activeMapping.isFieldMapped(target)) {
        return this.isFieldAddableToMapping(source, activeMapping);
      }
    }
    // It'll create a new mapping
    return (
      this.isFieldAddableToMapping(source, null) &&
      this.isFieldAddableToMapping(target, null)
    );
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
          mappedField instanceof PaddingField ||
          mappedField.docDef.id !== docId
        ) {
          continue;
        }
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

  /**
   * Invoke the runtime service to validate the current active mapping.
   */
  private async validateMappings(
    payload: IAtlasMappingContainer
  ): Promise<boolean> {
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
        .json<IValidationsContainer>()
        .then((body) => {
          this.cfg.logger!.debug(
            `Validation Service Response: ${JSON.stringify(body)}\n`
          );
          this.processValidationResponse(body, resolve);
        })
        .catch(() => {
          this.cfg.logger!.warn('Unable to fetch validation data.');
          resolve(false);
        });
    });
  }

  private processValidationResponse(
    body: IValidationsContainer,
    resolve: (value: boolean) => void
  ) {
    if (this.cfg.mappings === null) {
      resolve(false);
      return;
    }
    const errors: ErrorInfo[] = [];

    // This should be eventually turned into mapping entry level validation.
    // https://github.com/atlasmap/atlasmap-ui/issues/116
    if (body && body.Validations && body.Validations.validation) {
      for (const validation of body.Validations.validation) {
        const level: ErrorLevel =
          ErrorLevel[validation.status ? validation.status : 'ERROR'];
        let scope: ErrorScope =
          ErrorScope[validation.scope ? validation.scope : 'MAPPING'];
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
  notifyMappingUpdated(): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
      if (this.cfg.mappings) {
        const activeMapping: MappingModel = this.cfg.mappings.activeMapping!; // TODO: check this non null operator
        if (
          activeMapping &&
          this.cfg.mappings.mappings.indexOf(activeMapping) === -1
        ) {
          this.cfg.mappings.mappings.push(activeMapping);
        }
        const payload = MappingSerializer.serializeMappings(this.cfg);
        if (await this.validateMappings(payload)) {
          await this.cfg.fileService.setMappingToService(payload);
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
  private updateTransition(mapping: MappingModel): void {
    if (mapping.transition.enableExpression) {
      return;
    }

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
      return;
    }
    if (sourceMappedFields.length > 1 || sourceMappedCollection) {
      mapping.transition.mode = TransitionMode.MANY_TO_ONE;
      if (
        !mapping.transition.transitionFieldAction ||
        !mapping.transition.transitionFieldAction.definition ||
        mapping.transition.transitionFieldAction.definition.multiplicity !==
          Multiplicity.MANY_TO_ONE
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
      return;
    }
    if (targetMappedFields.length > 1 || targetMappedCollection) {
      mapping.transition.mode = TransitionMode.ONE_TO_MANY;
      if (
        !mapping.transition.transitionFieldAction ||
        mapping.transition.transitionFieldAction.definition?.multiplicity !==
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
        mapping.transition.transitionFieldAction.setArgumentValue(
          'collapseRepeatingDelimiters',
          'false'
        );
      }
      return;
    }
    mapping.transition.mode = TransitionMode.ONE_TO_ONE;
    mapping.transition.transitionFieldAction = null;
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
}
