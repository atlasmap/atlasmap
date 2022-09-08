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
import ky, { Options } from 'ky';
import { ConfigModel } from '../models';
import { InitializationService } from './initialization.service';
import { Input } from 'ky/distribution/types/options';
import { MappingModel } from '../models/mapping.model';
import { MappingPreviewService } from './mapping-preview.service';
import { TestUtils } from '../../test/test-util';

describe('MappingPreviewService', () => {
  let cfg: ConfigModel;
  let service: MappingPreviewService;

  function setSourceFieldValues(mapping: MappingModel) {
    let sourceFields = mapping.getFields(true);
    for (let i in sourceFields) {
      sourceFields[i].value = 'value-' + i;
    }
  }

  beforeEach(() => {
    const initService = new InitializationService(ky);
    initService.initialize();
    cfg = initService.cfg;
    service = cfg.previewService;
  });

  test('{enable,disable}MappingPreview()', (done) => {
    spyOn<any>(cfg.mappingService, 'validateMappings').and.stub();
    const requests = [];
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      requests.push(options.json);
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        then(): Promise<any> {
          return Promise.resolve(options.json);
        }
      })();
    });
    TestUtils.createMockMappings(cfg);
    service.cfg.initCfg.baseMappingServiceUrl = 'http://dummy/';
    const mapping1 = service.cfg.mappings!.mappings[1];
    setSourceFieldValues(mapping1);
    cfg.mappingService.selectMapping(mapping1);
    service.enableMappingPreview();
    expect(requests.length).toBe(0);
    cfg.mappingService
      .notifyMappingUpdated()
      .then((value) => {
        expect(value).toBeTruthy();
        expect(requests.length).toBe(1);
        service.disableMappingPreview();
        cfg.mappingService
          .notifyMappingUpdated()
          .then((value2) => {
            expect(value2).toBeTruthy();
            expect(requests.length).toBe(1);
            done();
          })
          .catch((error) => {
            fail(error);
          });
      })
      .catch((error) => {
        fail(error);
      });
  });
});
