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
import { DocumentDefinition, MappingDefinition } from '../models';
import { DocumentType, InspectionType } from '../contracts/common';
import { TextDecoder, TextEncoder } from 'text-encoding';

import { ADMDigest } from '../contracts/adm-digest';
import { CommonUtil } from '../utils/common-util';
import { DocumentInitializationModel } from '../models/config.model';
import { ErrorLevel } from '../models/error.model';
import { FileManagementService } from './file-management.service';
import FileSaver from 'file-saver';
import { InitializationService } from './initialization.service';
import { MAPPING_JSON_TYPE } from '../contracts/mapping';
import fs from 'fs';
import ky from 'ky';
import log from 'loglevel';
import { mocked } from 'ts-jest/utils';
import pako from 'pako';

describe('FileManagementService', () => {
  jest.mock('./initialization.service');
  const mockedInitService = mocked(InitializationService, true);
  jest.mock('ky');
  const mockedKy = mocked(ky, true);
  const service = new FileManagementService(ky);
  jest.mock('file-saver');
  jest.mock('../utils/common-util');
  const mockedCommonUtil = mocked(CommonUtil, true);
  jest.mock('pako');
  const mockedPako = mocked(pako);
  const mockedFileSaver = mocked(FileSaver);

  beforeEach(() => {
    const initService = new InitializationService(ky);
    service.cfg = initService.cfg;
    service.cfg.logger = log.getLogger('config');
    service.cfg.fileService = service;
  });

  test('findMappingFiles()', (done) => {
    mockedKy.get = jest.fn().mockReturnValue(
      new (class {
        json(): Promise<any> {
          return Promise.resolve({
            StringMap: {
              stringMapEntry: [
                { name: 'dummyMappingFile1' },
                { name: 'dummyMappingFile2' },
              ],
            },
          });
        }
      })()
    );
    service
      .findMappingFiles('UI')
      .then((value) => {
        expect(value.length).toBe(2);
        expect(value[0]).toBe('dummyMappingFile1');
        expect(value[1]).toBe('dummyMappingFile2');
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('findMappingFiles() server error', (done) => {
    mockedKy.get = jest.fn().mockReturnValue(
      new (class {
        json(): Promise<any> {
          return Promise.reject('expected error');
        }
      })()
    );
    service
      .findMappingFiles('UI')
      .then(() => {
        fail('expected to be rejected');
      })
      .catch((error) => {
        expect(error).toMatch('expected error');
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(err.message.indexOf('current mapping files')).toBeGreaterThan(0);
        done();
      });
  });

  test('getCurrentMappingDigest()', (done) => {
    mockedKy.get = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new TextEncoder().encode('test text'));
        }
      })()
    );
    mockedPako.inflate = jest.fn().mockReturnValue('dummy');
    mockedCommonUtil.objectize = jest.fn().mockReturnValue({} as ADMDigest);
    service
      .getCurrentMappingDigest()
      .then((value) => {
        expect(value?.exportMappings).toBeUndefined();
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('getCurrentMappingDigest() server error', (done) => {
    mockedKy.get = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.reject('expected error');
        }
      })()
    );
    service
      .getCurrentMappingDigest()
      .then(() => {
        fail('expected to be rejected');
      })
      .catch((error) => {
        expect(error).toMatch('expected error');
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(err.message.indexOf('Mapping digest file')).toBeGreaterThan(0);
        done();
      });
  });

  test('getCurrentADMArchive()', (done) => {
    mockedKy.get = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new TextEncoder().encode('test text'));
        }
      })()
    );
    service
      .getCurrentADMArchive()
      .then((value) => {
        expect(new TextDecoder().decode(value!)).toMatch('test text');
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('getCurrentADMArchive() server error', (done) => {
    mockedKy.get = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.reject('expected error');
        }
      })()
    );
    service
      .getCurrentADMArchive()
      .then(() => {
        fail('expected to be  rejected');
      })
      .catch((error) => {
        expect(error).toMatch('expected error');
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(err.message.indexOf('ADM archive file')).toBeGreaterThan(0);
        done();
      });
  });

  test('resetMappings()', (done) => {
    mockedKy.delete = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })()
    );
    service.resetMappings().then((value) => {
      expect(value).toBeTruthy();
      done();
    });
  });

  test('resetMappings() server error', (done) => {
    mockedKy.delete = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.reject('expected error');
        }
      })()
    );
    service
      .resetMappings()
      .then(() => {
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(err.message.indexOf('resetting mappings')).toBeGreaterThan(0);
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('resetLibs()', (done) => {
    mockedKy.delete = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })()
    );
    service.resetLibs().then((value) => {
      expect(value).toBeTruthy();
      done();
    });
  });

  test('resetLibs() server error', (done) => {
    mockedKy.delete = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.reject('expected error');
        }
      })()
    );
    service
      .resetLibs()
      .then(() => {
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(err.message.indexOf('JAR libraries')).toBeGreaterThan(0);
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('setMappingToService()', (done) => {
    mockedKy.put = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })()
    );
    const mappingJson = { AtlasMapping: { jsonType: MAPPING_JSON_TYPE } };
    service
      .setMappingToService(mappingJson)
      .then((value) => {
        expect(value).toBeTruthy();
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('setMappingToService() server error', (done) => {
    mockedKy.put = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.reject('expected error');
        }
      })()
    );
    const mappingJson = { AtlasMapping: { jsonType: MAPPING_JSON_TYPE } };
    service
      .setMappingToService(mappingJson)
      .then((value) => {
        expect(value).toBeFalsy();
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(
          err.message.indexOf('Unable to update the mappings file')
        ).toBeGreaterThanOrEqual(0);
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('importADMArchive()', (done) => {
    mockedKy.put = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })()
    );
    const binary = new TextEncoder().encode('dummy binary');
    service
      .importADMArchive(new File([new Blob([binary])], 'dummy.adm'))
      .then((value) => {
        expect(value).toBeTruthy();
        done();
      });
  });

  test('setDigestFileToService()', (done) => {
    mockedKy.put = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })()
    );
    const digest = {} as ADMDigest;
    service
      .setMappingDigestToService(digest)
      .then((value) => {
        expect(value).toBeTruthy();
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('setDigestFileToService() server error', (done) => {
    mockedKy.put = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.reject('expected error');
        }
      })()
    );
    const digest = {} as ADMDigest;
    service.setMappingDigestToService(digest).then((value) => {
      expect(value).toBeFalsy();
      const err = service.cfg.errorService.getErrors()[0];
      expect(err.level).toBe(ErrorLevel.ERROR);
      expect(
        err.message.indexOf('Unable to update the Mapping digest file')
      ).toBeGreaterThanOrEqual(0);
      done();
    });
  });

  test('importJarFile()', (done) => {
    mockedKy.put = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<Buffer> {
          return Promise.resolve(Buffer.from(''));
        }
      })()
    );
    const binary = new TextEncoder().encode('dummy binary');
    service.importJarFile(binary).then((value) => {
      expect(value).toBeTruthy();
      done();
    });
  });

  test('exportADMArchive()', (done) => {
    // put digest file
    mockedKy.put = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })()
    );
    // get ADM archive file
    mockedKy.get = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(
            fs.readFileSync(
              `${__dirname}/../../../../test-resources/adm/mockdoc.adm`
            )
          );
        }
        json(): Promise<any> {
          return Promise.resolve({ AtlasMapping: {} });
        }
      })()
    );
    jest.spyOn(FileSaver, 'saveAs').mockImplementation((_data) => {});
    service.cfg.mappings = new MappingDefinition();
    const srcDoc = new DocumentDefinition();
    srcDoc.name = 'dummy source document';
    srcDoc.inspectionType = InspectionType.SCHEMA;
    srcDoc.inspectionSource = 'dummy schema';

    const srcCSVDoc = new DocumentDefinition();
    srcCSVDoc.name = 'dummy CSV source document';
    srcCSVDoc.inspectionType = InspectionType.UNKNOWN;
    srcCSVDoc.inspectionSource = 'dummy CSV';
    srcCSVDoc.initModel = new DocumentInitializationModel();
    srcCSVDoc.initModel.isSource = true;
    srcCSVDoc.initModel.type = DocumentType.CSV;

    const tgtDoc = new DocumentDefinition();
    tgtDoc.name = 'dummy target document';
    tgtDoc.inspectionType = InspectionType.SCHEMA;
    tgtDoc.inspectionSource = 'dummy schema';

    service.cfg.sourceDocs.push(srcDoc);
    service.cfg.sourceDocs.push(srcCSVDoc);
    service.cfg.targetDocs.push(tgtDoc);
    service
      .exportADMArchive('atlasmap-mapping.adm')
      .then((value) => {
        expect(value).toBeTruthy();
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('importADMArchive()', (done) => {
    mockedCommonUtil.readBinaryFile = jest
      .fn()
      .mockResolvedValue(
        fs.readFileSync(
          `${__dirname}/../../../../test-resources/adm/mockdoc.adm`
        )
      );
    // put ADM archive file
    mockedKy.put = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })()
    );
    mockedInitService.prototype.initialize = jest
      .fn()
      .mockImplementation(() => {});
    const buf = fs.readFileSync(
      `${__dirname}/../../../../test-resources/json/schema/mock-json-schema.json`
    );
    service
      .importADMArchive(new File([new Blob([buf])], 'atlasmap-mapping.adm'))
      .then((value) => {
        expect(value).toBeTruthy();
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });
});
