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
import { ConfigModel } from '../models/config.model';
import {
  FieldActionDefinition,
  FieldActionArgument,
  Multiplicity,
} from '../models/field-action.model';
import { Observable } from 'rxjs';
import ky from 'ky';
import { MappingModel } from '../models/mapping.model';
import { Field } from '../models/field.model';
import {
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
} from '../models/error.model';

export class FieldActionService {
  cfg: ConfigModel = ConfigModel.getConfig();
  actions: { [key in Multiplicity]: FieldActionDefinition[] } = {
    [Multiplicity.ONE_TO_ONE]: [],
    [Multiplicity.ONE_TO_MANY]: [],
    [Multiplicity.MANY_TO_ONE]: [],
    [Multiplicity.ZERO_TO_ONE]: [],
  };

  isInitialized = false;
  private headers = {
    'Content-Type': 'application/json; application/octet-stream',
    Accept: 'application/json; application/octet-stream',
  };

  constructor(private api: typeof ky) {}

  async fetchFieldActions(): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      if (this.cfg.preloadedFieldActionMetadata) {
        this.clearActionDefinitions();
        if (
          this.cfg.preloadedFieldActionMetadata &&
          this.cfg.preloadedFieldActionMetadata.ActionDetails
        ) {
          for (const actionDetail of this.cfg.preloadedFieldActionMetadata
            .ActionDetails.actionDetail) {
            const fieldActionDefinition = this.extractFieldActionDefinition(
              actionDetail
            );
            if (!fieldActionDefinition.multiplicity) {
              this.cfg.logger!.info(
                `Field action (${fieldActionDefinition.name}) is missing multiplicity, ingoring`
              );
              continue;
            }
            if (fieldActionDefinition.name === 'Expression') {
              // Expression is handled in special manner
              continue;
            }
            this.actions[fieldActionDefinition.multiplicity].push(
              fieldActionDefinition
            );
          }
        }
        this.sortFieldActionDefinitions();
        this.isInitialized = true;
        resolve(true);
        return;
      }

      if (this.cfg.mappingService == null) {
        this.cfg.errorService.addError(
          new ErrorInfo({
            message:
              'Mapping service is not provided. Field Actions will not be used.',
            level: ErrorLevel.WARN,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.INTERNAL,
          })
        );
        this.isInitialized = true;
        resolve(true);
        return;
      } else if (this.cfg.initCfg.baseMappingServiceUrl == null) {
        this.cfg.errorService.addError(
          new ErrorInfo({
            message:
              'Mapping service URL is not provided. Field Actions will not be used.',
            level: ErrorLevel.WARN,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.INTERNAL,
          })
        );
        this.isInitialized = true;
        resolve(true);
        return;
      }

      // Fetch the field actions from the runtime service.
      this.doFetchFieldActions()
        .toPromise()
        .then((fetchedActionConfigs: FieldActionDefinition[]) => {
          if (fetchedActionConfigs.length === 1) {
            this.cfg.logger!.info('No field action was returned from backend');
            resolve(false);
          }
          this.clearActionDefinitions();
          fetchedActionConfigs.forEach(action => {
            if (!action.multiplicity) {
              this.cfg.logger!.info(
                `Field action  (${action.name}) is missing multiplicity, ignoring`
              );
              return;
            }
            if (action.name === 'Expression') {
              // Expression is handled in special manner
              return;
            }
            this.actions[action.multiplicity].push(action);
          });
          this.sortFieldActionDefinitions();
          this.isInitialized = true;
          resolve(true);
        })
        .catch((error: any) => {
          if (error.status === 0) {
            reject(
              `Fatal network error: Could not connect to AtlasMap design runtime service. (${error})`
            );
          } else {
            reject(`Could not load field action configs: (${error.message})`);
          }
        });
    });
  }

  getActionDefinitionForName(
    actionName: string,
    multiplicity?: Multiplicity
  ): FieldActionDefinition | null {
    if (!this.actions || !actionName) {
      return null;
    }
    let actions: FieldActionDefinition[] = [];
    if (multiplicity) {
      actions = this.actions[multiplicity];
    } else {
      Object.values(this.actions).forEach(
        arr => (actions = actions.concat(arr))
      );
    }
    for (const actionDef of actions) {
      if (actionName === actionDef.name) {
        return actionDef;
      }
    }
    return null;
  }

  /**
   * Return the field action Definitions applicable to the specified field mapping pair.
   * @param mapping
   * @param isSource
   * @param multiplicity
   */
  getActionsAppliesToField(
    mapping: MappingModel,
    isSource: boolean = true,
    multiplicity: Multiplicity = Multiplicity.ONE_TO_ONE
  ): FieldActionDefinition[] {
    if (!mapping || !this.actions) {
      return [];
    }
    return this.actions[multiplicity].filter(d =>
      this.appliesToField(d, mapping, isSource)
    );
  }

  private doFetchFieldActions(): Observable<FieldActionDefinition[]> {
    return new Observable<FieldActionDefinition[]>((observer: any) => {
      const actionConfigs: FieldActionDefinition[] = [];
      const url: string =
        this.cfg.initCfg.baseMappingServiceUrl + 'fieldActions';
      this.cfg.logger!.debug('Field Action Config Request');
      this.api
        .get(url, { headers: this.headers })
        .json()
        .then((body: any) => {
          this.cfg.logger!.debug(
            `Field Action Config Response: ${JSON.stringify(body)}`
          );
          if (
            body &&
            body.ActionDetails &&
            body.ActionDetails.actionDetail &&
            body.ActionDetails.actionDetail.length
          ) {
            for (const actionDetail of body.ActionDetails.actionDetail) {
              const fieldActionConfig = this.extractFieldActionDefinition(
                actionDetail
              );
              actionConfigs.push(fieldActionConfig);
            }
          }
          observer.next(actionConfigs);
          observer.complete();
        })
        .catch((error: any) => {
          observer.error(error);
          observer.next(actionConfigs);
          observer.complete();
        });
    });
  }

  private extractFieldActionDefinition(
    actionDetail: any
  ): FieldActionDefinition {
    this.cfg.logger!.info(
      `Deserializing field action definition: ${JSON.stringify(actionDetail)}`
    );

    const fieldActionDefinition = new FieldActionDefinition();
    fieldActionDefinition.name = actionDetail.name;
    fieldActionDefinition.isCustom = actionDetail.custom;
    fieldActionDefinition.sourceType = actionDetail.sourceType;
    fieldActionDefinition.targetType = actionDetail.targetType;
    fieldActionDefinition.method = actionDetail.method;
    fieldActionDefinition.multiplicity = actionDetail.multiplicity;
    fieldActionDefinition.serviceObject = actionDetail;

    if (actionDetail.actionSchema) {
      for (const key of Object.keys(actionDetail.actionSchema.properties)) {
        const propertyObject = actionDetail.actionSchema.properties[key];
        if (key === '@type') {
          fieldActionDefinition.name = propertyObject.const;
          continue;
        }
        const argumentDefinition = new FieldActionArgument();
        argumentDefinition.name = key;
        argumentDefinition.type = propertyObject.type;
        argumentDefinition.values = propertyObject.enum;
        argumentDefinition.serviceObject = propertyObject;
        fieldActionDefinition.arguments.push(argumentDefinition);
      }
    }
    return fieldActionDefinition;
  }

  private sortFieldActionDefinitions() {
    (Object.keys(this.actions) as [keyof typeof Multiplicity]).forEach(
      multiplicity => {
        const definitions = this.actions[multiplicity];
        const sortedActionDefinitions: FieldActionDefinition[] = [];
        if (definitions == null || definitions.length === 0) {
          return;
        }

        const defsByName: { [key: string]: FieldActionDefinition[] } = {};
        const defNames: string[] = [];
        for (const fieldActionConfig of definitions) {
          const name: string = fieldActionConfig.name;
          let sameNamedDefs: FieldActionDefinition[] = defsByName[name];
          if (!sameNamedDefs) {
            sameNamedDefs = [];
            defNames.push(name);
          }
          sameNamedDefs.push(fieldActionConfig);
          defsByName[name] = sameNamedDefs;
        }

        defNames.sort();

        for (const name of defNames) {
          const sameNamedDefs: FieldActionDefinition[] = defsByName[name];
          for (const fieldActionDefinition of sameNamedDefs) {
            sortedActionDefinitions.push(fieldActionDefinition);
          }
        }
        this.actions[multiplicity] = sortedActionDefinitions;
      }
    );
  }

  /**
   * Return true if the action's source/target types and collection types match the respective source/target
   * field properties for source transformations, or matches the respective target field properties only for
   * a target transformation.
   *
   * @param action
   * @param mapping
   * @param isSource
   */
  appliesToField(
    action: FieldActionDefinition,
    mapping: MappingModel,
    isSource: boolean
  ): boolean {
    if (mapping == null) {
      return false;
    }
    const selectedSourceField: Field = this.getActualField(mapping, true);
    const selectedTargetField: Field = this.getActualField(mapping, false);

    if (
      (isSource && selectedSourceField == null) ||
      (!isSource && selectedTargetField == null)
    ) {
      return false;
    }

    return isSource
      ? this.appliesToSourceField(action, mapping, selectedSourceField)
      : this.appliesToTargetField(action, mapping, selectedTargetField);
  }

  /**
   * Return the first non-padding field in either the source or target mappings.
   *
   * @param mapping
   * @param isSource
   */
  private getActualField(mapping: MappingModel, isSource: boolean): Field {
    const targetField = mapping
      .getFields(isSource)
      .find(f => f.name !== '<padding field>');
    // TODO: maybe throw an exception instead of assuming the field will be found?
    return targetField!;
  }

  /**
   * Check if it could be applied to source field.
   * @param action
   * @param _
   * @param selectedSourceField selected source field
   */
  private appliesToSourceField(
    action: FieldActionDefinition,
    _: MappingModel,
    selectedSourceField: Field
  ): boolean {
    // Check for matching types - date.
    if (this.matchesDate(action.sourceType, selectedSourceField.type)) {
      return true;
    }

    // Check for matching types - numeric.
    if (this.matchesNumeric(action.sourceType, selectedSourceField.type)) {
      return true;
    }

    // First check if the source types match.
    if (
      action.sourceType === 'ANY' ||
      selectedSourceField.type === action.sourceType
    ) {
      return true;
    }

    return false;
  }

  /**
   * Check if it could be applied for target field. Target type may not change.
   * @param action
   * @param _
   * @param selectedTargetField selected target field
   */
  private appliesToTargetField(
    action: FieldActionDefinition,
    _: MappingModel,
    selectedTargetField: Field
  ): boolean {
    if (selectedTargetField == null) {
      return false;
    }

    if (action.multiplicity !== Multiplicity.ONE_TO_ONE) {
      return false;
    }

    // Check for matching types - date.
    if (
      this.matchesDate(action.sourceType, selectedTargetField.type) &&
      this.matchesDate(action.targetType, selectedTargetField.type)
    ) {
      return true;
    }

    // Check for matching types - numeric.
    if (
      this.matchesNumeric(action.sourceType, selectedTargetField.type) &&
      this.matchesNumeric(action.targetType, selectedTargetField.type)
    ) {
      return true;
    }

    if (
      action.sourceType !== 'ANY' &&
      action.sourceType !== selectedTargetField.type
    ) {
      return false;
    }

    // All other types must match the selected field types with the candidate field action types.
    return (
      action.targetType === 'ANY' ||
      selectedTargetField.type === action.targetType
    );
  }

  /**
   * Return true if the candidate type and selected type are generically a date, false otherwise.
   *
   * @param candidateType
   * @param selectedType
   */
  private matchesDate(candidateType: string, selectedType: string): boolean {
    return (
      candidateType === 'ANY' ||
      (candidateType === 'ANY_DATE' &&
        ['DATE', 'DATE_TIME', 'DATE_TIME_TZ', 'TIME'].indexOf(selectedType) !==
          -1)
    );
  }

  /**
   * Return true if the candidate type and selected type are generically numeric, false otherwise.
   *
   * @param candidateType
   * @param selectedType
   */
  private matchesNumeric(candidateType: string, selectedType: string): boolean {
    return (
      candidateType === 'ANY' ||
      (candidateType === 'NUMBER' &&
        [
          'LONG',
          'INTEGER',
          'FLOAT',
          'DOUBLE',
          'SHORT',
          'BYTE',
          'DECIMAL',
          'NUMBER',
        ].indexOf(selectedType) !== -1)
    );
  }

  private clearActionDefinitions() {
    (Object.keys(Multiplicity) as [keyof typeof Multiplicity]).forEach(
      m => (this.actions[m] = [])
    );
  }
}
