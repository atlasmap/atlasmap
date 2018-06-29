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

import { Observable, Subject, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { MappingModel, FieldMappingPair, MappedField } from '../models/mapping.model';
import { FieldActionConfig, FieldActionArgument, TransitionMode, TransitionModel } from '../models/transition.model';
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

  mappingPreviewInputSource = new Subject<FieldMappingPair>();
  mappingPreviewInput$ = this.mappingPreviewInputSource.asObservable();
  mappingPreviewOutputSource = new Subject<FieldMappingPair>();
  mappingPreviewOutput$ = this.mappingPreviewOutputSource.asObservable();
  mappingPreviewErrorSource = new Subject<ErrorInfo[]>();
  mappingPreviewError$ = this.mappingPreviewErrorSource.asObservable();

  private headers = new HttpHeaders({'Content-Type': 'application/json'});
  private mappingPreviewInputSubscription;
  private mappingUpdatedSubscription;

  constructor(private http: HttpClient) {}

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
    });
  }

  fetchMappings(mappingFileNames: string[], mappingDefinition: MappingDefinition): Observable<boolean> {
    return new Observable<boolean>((observer: any) => {
      if (mappingFileNames.length === 0) {
        observer.complete();
        return;
      }

      const baseURL: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/';
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
    });
  }

  saveCurrentMapping(): void {
    const activeMapping: MappingModel = this.cfg.mappings.activeMapping;
    if ((activeMapping != null) && (this.cfg.mappings.mappings.indexOf(activeMapping) === -1)) {
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
    this.notifyMappingUpdated();
  }

  serializeMappingsToJSON(): any {
    return MappingSerializer.serializeMappings(this.cfg);
  }

  saveMappingToService(): void {
    const payload: any = this.serializeMappingsToJSON();
    const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping';
    DataMapperUtil.debugLogJSON(payload, 'Mapping Service Request', this.cfg.initCfg.debugMappingServiceCalls, url);
    this.http.put(url, JSON.stringify(payload), { headers: this.headers }).toPromise()
      .then((res: any) => {
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
        this.cfg.errorService.warn('The selected mapping details action ' + TransitionModel.getActionName(mappingPair.transition.mode) +
                ' is not applicable from compound source selections (' + field.name +
                ').  Recommend using field action \'Combine\'.', null);
        return;
      }
      suggestedValue = mappingPair.sourceFields[mappingPair.sourceFields.length - 1].actions[0].argumentValues[0].value;

    } else if (mappingPair.transition.mode === TransitionMode.SEPARATE) {
      if (field.isSource()) {

        // Compound target mapping when not in Separate mode.
        this.cfg.errorService.warn('The selected mapping details action ' + TransitionModel.getActionName(mappingPair.transition.mode) +
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
    field.selected = true;
    if (!field.isTerminal()) {
      field.docDef.populateChildren(field);
      field.docDef.updateFromMappings(this.cfg.mappings);
      field.collapsed = !field.collapsed;
      return;
    }

    let mapping: MappingModel = this.cfg.mappings.activeMapping;
    let fieldRemoved = false;

    if (mapping != null && mapping.hasMappedFields(field.isSource())) {

      // If the user has performed a compound selection (ctrl-m1) of a previously unselected field
      // then add it to the active mapping; otherwise remove it.
      if (compoundSelection) {
          if (mapping.isFieldMapped(field, field.isSource()) && field.selected) {
            this.removeActiveMappingField(field, compoundSelection);
            field.selected = false;
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
        return;
      }
    }

    // Check to see if the field is a valid selection for this mapping
    const exclusionReason: string = mapping.getFieldSelectionExclusionReason(field);
    if (exclusionReason != null) {
      this.cfg.errorService.warn('The field \'' + field.displayName + '\' cannot be selected, ' + exclusionReason + '.', null);
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
   * @param compoundSelection - indicates a compound-selection (ctrl-M1) if true, standard mouse click if false.
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
    for (const fieldPair of mappingModel.fieldMappings) {
      DocumentDefinition.selectFields(fieldPair.getAllFields());
    }
    this.cfg.mappings.initializeMappingLookupTable(mappingModel);
    this.saveCurrentMapping();
  }

  deselectMapping(): void {
    this.cfg.showMappingDetailTray = false;
    this.cfg.mappings.activeMapping = null;
    for (const doc of this.cfg.getAllDocs()) {
      doc.clearSelectedFields();
    }
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
      this.mappingUpdatedSubscription.complete();
      this.mappingUpdatedSubscription = undefined;
    }
    if (this.mappingPreviewInputSubscription) {
      this.mappingPreviewInputSubscription.complete();
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
          for (const svcConfig of body.ActionDetails.actionDetail) {
            const fieldActionConfig: FieldActionConfig = new FieldActionConfig();
            fieldActionConfig.name = svcConfig.name;
            fieldActionConfig.sourceType = svcConfig.sourceType;
            fieldActionConfig.targetType = svcConfig.targetType;
            fieldActionConfig.method = svcConfig.method;
            fieldActionConfig.serviceObject = svcConfig;

            if (svcConfig.parameters && svcConfig.parameters.parameter
              && svcConfig.parameters.parameter.length) {
              for (const svcParameter of svcConfig.parameters.parameter) {
                const argumentConfig: FieldActionArgument = new FieldActionArgument();
                argumentConfig.name = svcParameter.name;
                argumentConfig.type = svcParameter.fieldType;
                argumentConfig.values = svcParameter.values;
                argumentConfig.serviceObject = svcParameter;
                fieldActionConfig.arguments.push(argumentConfig);
              }
            }
            actionConfigs.push(fieldActionConfig);
          }
        }
        actionConfigs = this.sortFieldActionConfigs(actionConfigs);
        observer.next(actionConfigs);
        observer.complete();
      }).catch((error: any) => {
        observer.error(error);
        observer.next(actionConfigs);
        observer.complete();
      });
    });
  }

  sortFieldActionConfigs(configs: FieldActionConfig[]): FieldActionConfig[] {
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
