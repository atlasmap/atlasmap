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
import { DocumentType, InspectionType } from '../common/config.types';

import { DocumentInitializationModel } from '../models/config.model';
import { InitializationService } from '../services/initialization.service';
import { TextEncoder } from 'text-encoding';
import atlasmapFieldActionJson from '../../../../test-resources/fieldActions/atlasmap-field-action.json';
import atlasmapInspectionMockJsonInstanceJson from '../../../../test-resources/inspected/atlasmap-inspection-mock-json-instance.json';
import atlasmapInspectionMockJsonSchemaJson from '../../../../test-resources/inspected/atlasmap-inspection-mock-json-schema.json';
import atlasmapInspectionMockXmlInstance1Json from '../../../../test-resources/inspected/atlasmap-inspection-mock-xml-instance-1.json';
import atlasmapInspectionMockXmlSchema1Json from '../../../../test-resources/inspected/atlasmap-inspection-mock-xml-schema-1.json';
import atlasmapInspectionOldActionSourceJson from '../../../../test-resources/inspected/atlasmap-inspection-old-action-source.json';
import atlasmapInspectionOldActionTargetJson from '../../../../test-resources/inspected/atlasmap-inspection-old-action-target.json';
import atlasmappingOldActionJson from '../../../../test-resources/mapping/atlasmapping-old-action.json';
import ky from 'ky/umd';

describe('InitializationService', () => {
  let service: InitializationService;

  beforeEach(() => {
    service = new InitializationService(ky);
  });

  afterEach(() => {
    service.cfg.clearDocs();
    if (service.cfg.mappings) {
      service.cfg.mappings.mappings = [];
    }
  });

  test('runtimeServiceActive()', (done) => {
    spyOn(ky, 'get').and.returnValue(
      new (class {
        json(): Promise<any> {
          return Promise.resolve({ string: 'pong' });
        }
      })()
    );
    service
      .runtimeServiceActive()
      .then((value) => {
        expect(value).toBeTruthy();
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('load document definitions', (done) => {
    const c = service.cfg;
    c.initCfg.baseMappingServiceUrl = 'dummy';
    c.initCfg.baseJSONInspectionServiceUrl = 'dummy';
    c.initCfg.baseXMLInspectionServiceUrl = 'dummy';
    c.initCfg.baseCSVInspectionServiceUrl = 'dummy';
    const sourceJson = new DocumentInitializationModel();
    sourceJson.isSource = true;
    sourceJson.type = DocumentType.JSON;
    sourceJson.inspectionType = InspectionType.SCHEMA;
    sourceJson.inspectionResult = JSON.stringify(
      atlasmapInspectionMockJsonSchemaJson
    );
    c.addDocument(sourceJson);
    const sourceXml = new DocumentInitializationModel();
    sourceXml.isSource = true;
    sourceXml.type = DocumentType.XML;
    sourceXml.inspectionType = InspectionType.INSTANCE;
    sourceXml.inspectionResult = JSON.stringify(
      atlasmapInspectionMockXmlInstance1Json
    );
    c.addDocument(sourceXml);
    const targetJson = new DocumentInitializationModel();
    targetJson.isSource = false;
    targetJson.type = DocumentType.JSON;
    targetJson.inspectionType = InspectionType.INSTANCE;
    targetJson.inspectionResult = JSON.stringify(
      atlasmapInspectionMockJsonInstanceJson
    );
    c.addDocument(targetJson);
    const targetXml = new DocumentInitializationModel();
    targetXml.isSource = false;
    targetXml.type = DocumentType.XML;
    targetXml.inspectionType = InspectionType.SCHEMA;
    targetXml.inspectionResult = JSON.stringify(
      atlasmapInspectionMockXmlSchema1Json
    );
    c.addDocument(targetXml);
    spyOn(service, 'runtimeServiceActive').and.returnValue(
      Promise.resolve(true)
    );
    return service
      .initialize()
      .then(() => {
        expect(c.sourceDocs[0].fields[0].path).toEqual('/addressList<>');
        expect(c.sourceDocs[1].fields[0].path).toEqual('/data');
        expect(c.targetDocs[0].fields[0].path).toEqual('/addressList<>');
        expect(c.targetDocs[1].fields[0].path).toEqual('/data');
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('load mapping definition', (done) => {
    const cfg = service.cfg;
    cfg.clearDocs();
    cfg.initCfg.baseMappingServiceUrl = 'dummy';
    cfg.initCfg.baseJSONInspectionServiceUrl = 'dummy';
    cfg.initCfg.baseXMLInspectionServiceUrl = 'dummy';
    cfg.initCfg.baseCSVInspectionServiceUrl = 'dummy';
    cfg.preloadedFieldActionMetadata = JSON.stringify(atlasmapFieldActionJson);

    const source = new DocumentInitializationModel();
    source.isSource = true;
    source.type = DocumentType.JSON;
    source.inspectionType = InspectionType.SCHEMA;
    source.id = 'old-action-source';
    source.inspectionResult = JSON.stringify(
      atlasmapInspectionOldActionSourceJson
    );
    cfg.addDocument(source);
    const target = new DocumentInitializationModel();
    target.isSource = false;
    target.type = DocumentType.JSON;
    target.inspectionType = InspectionType.SCHEMA;
    target.id = 'old-action-target';
    target.inspectionResult = JSON.stringify(
      atlasmapInspectionOldActionTargetJson
    );
    cfg.addDocument(target);
    cfg.preloadedMappingJson = JSON.stringify(atlasmappingOldActionJson);

    spyOn(service, 'runtimeServiceActive').and.returnValue(
      Promise.resolve(true)
    );
    spyOn(cfg.fileService, 'getCurrentMappingDigest').and.returnValue(
      Promise.resolve(null)
    );
    service.systemInitialized$.subscribe(() => {
      expect(cfg.sourceDocs[0].fields.length).toEqual(1);
      expect(cfg.sourceDocs[0].fields[0].path).toEqual('/<>');
      expect(cfg.targetDocs[0].fields[0].path).toEqual('/id');
      expect(cfg.mappings?.mappings?.length).toEqual(1);
      const mapping = cfg.mappings?.mappings[0];
      expect(mapping?.sourceFields?.length).toEqual(1);
      const sourceField = mapping?.sourceFields[0];
      expect(sourceField?.field).toBeTruthy();
      expect(mapping?.targetFields?.length).toEqual(1);
      const targetField = mapping?.targetFields[0];
      expect(targetField?.field).toBeTruthy();
      expect(cfg.errorService.getErrors().length).toEqual(0);
      done();
    });
    service.initialize().catch((error) => {
      fail(error);
    });
  });

  test('initializeWithADMArchiveFile()', (done) => {
    const spyImportAdm = spyOn(
      service.cfg.fileService,
      'importADMArchive'
    ).and.returnValue(Promise.resolve(true));
    const spyInitialize = spyOn(service, 'initialize').and.returnValue(
      Promise.resolve(true)
    );
    const binary = new TextEncoder().encode('dummy binary');
    service
      .initializeWithADMArchiveFile(new File([new Blob([binary])], 'dummy.adm'))
      .then((value) => {
        expect(value).toBeTruthy();
        expect(spyImportAdm.calls.count()).toBe(1);
        expect(spyInitialize.calls.count()).toBe(1);
        done();
      });
  });

  test('resetAtlasMap()', (done) => {
    const spyResetAll = spyOn(
      service.cfg.fileService,
      'resetAll'
    ).and.returnValue(Promise.resolve(true));
    const spyInitialize = spyOn(service, 'initialize').and.returnValue(
      Promise.resolve(true)
    );
    service.resetAtlasMap().then((value) => {
      expect(value).toBeTruthy();
      expect(spyResetAll.calls.count()).toBe(1);
      expect(spyInitialize.calls.count()).toBe(1);
      done();
    });
  });
});
