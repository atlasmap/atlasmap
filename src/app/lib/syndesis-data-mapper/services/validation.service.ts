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

import { Injectable } from '@angular/core';
import { Headers, Http, RequestOptions, Response, HttpModule } from '@angular/http';

import 'rxjs/add/operator/toPromise';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/observable/forkJoin';
import { Subject } from 'rxjs/Subject';

import { ErrorInfo } from '../models/error.model';
import { ConfigModel } from '../models/config.model';
import { MappingModel, FieldMappingPair } from '../models/mapping.model';

import { MappingSerializer } from './mapping.serializer';

import { DataMapperUtil } from '../common/data.mapper.util';

@Injectable()
export class ValidationService {
    public cfg: ConfigModel;

    private headers: Headers = new Headers();

    constructor(private http: Http) {
        this.headers.append("Content-Type", "application/json");
    }

    public initialize(): void {
        this.cfg.mappingService.mappingUpdated$.subscribe(mappingDefinition => {
            this.validateMappings();
        });
    }

    public validateMappings(): void {
        var skipValidation: boolean = true;
        if (skipValidation) { //FIXME: commented out validation for june 30 demo
            return;
        }
        var startTime: number = Date.now();
        var payload: any = MappingSerializer.serializeMappings(this.cfg);
        var url: string = this.cfg.initCfg.baseValidationServiceUrl + "mapping/validate";
        DataMapperUtil.debugLogJSON(payload, "Validation Service Request", this.cfg.debugValidationJSON, url);
        this.http.put(url, payload, { headers: this.headers }).toPromise()
            .then((res: Response) => {
                DataMapperUtil.debugLogJSON(res, "Validation Service Response", this.cfg.debugValidationJSON, url);
                var mapping: MappingModel = this.cfg.mappings.activeMapping;
                let body: any = res.json();
                mapping.clearValidationErrors();
                if (body && body.Validations && body.Validations.validation) {
                    for (let error of body.Validations.validation) {
                        mapping.addValidationError(error.message);
                    }
                }
                console.log("Finished fetching and parsing validation errors in " + (Date.now() - startTime) + "ms.");
            })
            .catch((error: any) => {
                console.error("Error fetching validation data.", { "error": error, "url": url, "request": payload});
            }
        );
    }
}
