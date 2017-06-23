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

import { ConfigModel } from '../models/config.model';
import { FieldActionConfig } from '../models/transition.model';

import { DataMapperUtil } from '../common/data.mapper.util';

@Injectable()
export class FieldActionService {
    public cfg: ConfigModel;

    private headers: Headers = new Headers();

    constructor(private http: Http) {
        this.headers.append("Content-Type", "application/json");
    }

    public initialize(): void {}    

    public fetchFieldActions(): Observable<FieldActionConfig[]> {
        return new Observable<FieldActionConfig[]>((observer:any) => {
            var actionConfigs: FieldActionConfig[] = [];
            var startTime: number = Date.now();
            var url: string = this.cfg.initCfg.baseFieldMappingServiceUrl + "fieldActions";
            DataMapperUtil.debugLogJSON(null, "Field Action Config Request", this.cfg.debugFieldActionJSON, url);
            this.http.get(url, { headers: this.headers }).toPromise()
                .then((res: Response) => {
                    let body: any = res.json();
                    DataMapperUtil.debugLogJSON(body, "Field Action Config Response", this.cfg.debugFieldActionJSON, url);
                    if (body && body.ActionDetails 
                        && body.ActionDetails.actionDetail 
                        && body.ActionDetails.actionDetail.length) {
                        for (let svcConfig of body.ActionDetails.actionDetail) {
                            var fieldActionConfig: FieldActionConfig = new FieldActionConfig();
                            fieldActionConfig.identifier = svcConfig.name;
                            fieldActionConfig.name = svcConfig.name;
                            fieldActionConfig.forString = true;
                            fieldActionConfig.method = svcConfig.method;
                            fieldActionConfig.serviceObject = svcConfig;
                            actionConfigs.push(fieldActionConfig);
                        }
                    }
                    console.log("Finished fetching and parsing " + actionConfigs.length + " field actionConfigs in "
                        + (Date.now() - startTime) + "ms.");
                    observer.next(actionConfigs);
                    observer.complete();
                })
                .catch((error: any) => {
                    observer.error(error);
                    observer.next(actionConfigs);
                    observer.complete();
                }
            );
        });
    }
}
