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
import { FunctionDefinition, FunctionArgument } from '../models/function.model';
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

export class FunctionService {
  cfg: ConfigModel = ConfigModel.getConfig();
  functions: FunctionDefinition[] = [];

  isInitialized = false;
  private headers = {
    'Content-Type': 'application/json; application/octet-stream',
    Accept: 'application/json; application/octet-stream',
  };

  constructor(private api: typeof ky) {}

  async fetchFunctions(): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      if (this.cfg.preloadedFunctionMetadata) {
        this.clearFunctionDefinitions();
        if (
          this.cfg.preloadedFunctionMetadata &&
          this.cfg.preloadedFunctionMetadata.FunctionDetails
        ) {
          for (const functionDetail of this.cfg.preloadedFunctionMetadata
            .FunctionDetails.functionDetail) {
            const functionDefinition = this.extractFunctionDefinition(
              functionDetail
            );
            this.functions.push(functionDefinition);
          }
        }
        this.sortFunctionDefinitions();
        this.isInitialized = true;
        resolve(true);
        return;
      }

      if (this.cfg.mappingService == null) {
        this.cfg.errorService.addError(
          new ErrorInfo({
            message:
              'Mapping service is not provided. Functions will not be used.',
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
              'Mapping service URL is not provided. Functions will not be used.',
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
      this.doFetchFunctions()
        .toPromise()
        .then((fetchedDefinitions: FunctionDefinition[]) => {
          if (fetchedDefinitions.length === 1) {
            this.cfg.logger!.info('No function was returned from backend');
            resolve(false);
          }
          this.clearFunctionDefinitions();
          fetchedDefinitions.forEach((func) => {
            this.functions.push(func);
          });
          this.sortFunctionDefinitions();
          this.isInitialized = true;
          resolve(true);
        })
        .catch((error: any) => {
          if (error.status === 0) {
            reject(
              `Fatal network error: Could not connect to AtlasMap design runtime service. (${error})`
            );
          } else {
            reject(`Could not load function definitions: (${error.message})`);
          }
        });
    });
  }

  getFunctionDefinitionForName(name: string): FunctionDefinition | null {
    if (!this.functions || !name) {
      return null;
    }
    let functions: FunctionDefinition[] = [];
    Object.values(this.functions).forEach(
      (arr) => (functions = functions.concat(arr))
    );
    for (const definition of functions) {
      if (name === definition.name) {
        return definition;
      }
    }
    return null;
  }

  private doFetchFunctions(): Observable<FunctionDefinition[]> {
    return new Observable<FunctionDefinition[]>((observer: any) => {
      const definitions: FunctionDefinition[] = [];
      const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'functions';
      this.cfg.logger!.debug('Function Definition Request');
      this.api
        .get(url, { headers: this.headers })
        .json()
        .then((body: any) => {
          this.cfg.logger!.debug(
            `Function Definition Response: ${JSON.stringify(body)}`
          );
          if (
            body &&
            body.FunctionDetails &&
            body.FunctionDetails.functionDetail &&
            body.FunctionDetails.functionDetail.length
          ) {
            for (const functionDetail of body.FunctionDetails.functionDetail) {
              const definition = this.extractFunctionDefinition(functionDetail);
              definitions.push(definition);
            }
          }
          observer.next(definitions);
          observer.complete();
        })
        .catch((error: any) => {
          observer.error(error);
          observer.next(definitions);
          observer.complete();
        });
    });
  }

  private extractFunctionDefinition(functionDetail: any): FunctionDefinition {
    this.cfg.logger!.info(
      `Deserializing function definition: ${JSON.stringify(functionDetail)}`
    );

    const definition = new FunctionDefinition();
    definition.name = functionDetail.name;
    definition.isCustom = functionDetail.custom;
    definition.method = functionDetail.method;
    definition.serviceObject = functionDetail;

    if (functionDetail.functionSchema) {
      for (const key of Object.keys(functionDetail.functionSchema.properties)) {
        const propertyObject = functionDetail.functionSchema.properties[key];
        if (key === '@type') {
          definition.name = propertyObject.const;
          continue;
        }
        const argumentDefinition = new FunctionArgument();
        argumentDefinition.name = key;
        argumentDefinition.type = propertyObject.type;
        argumentDefinition.values = propertyObject.enum;
        argumentDefinition.serviceObject = propertyObject;
        definition.arguments.push(argumentDefinition);
      }
    }
    return definition;
  }

  private sortFunctionDefinitions() {
    const definitions = this.functions;
    const sortedDefinitions: FunctionDefinition[] = [];
    if (definitions == null || definitions.length === 0) {
      return;
    }

    const defsByName: { [key: string]: FunctionDefinition[] } = {};
    const defNames: string[] = [];
    for (const definition of definitions) {
      const name: string = definition.name;
      let sameNamedDefs: FunctionDefinition[] = defsByName[name];
      if (!sameNamedDefs) {
        sameNamedDefs = [];
        defNames.push(name);
      }
      sameNamedDefs.push(definition);
      defsByName[name] = sameNamedDefs;
    }

    defNames.sort();

    for (const name of defNames) {
      const sameNamedDefs: FunctionDefinition[] = defsByName[name];
      for (const definition of sameNamedDefs) {
        sortedDefinitions.push(definition);
      }
    }
    this.functions = sortedDefinitions;
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

  private clearFunctionDefinitions() {
    this.functions = [];
  }
}
