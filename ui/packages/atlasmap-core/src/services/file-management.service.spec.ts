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
  ConfigModel,
  DocumentInitializationModel,
} from '../models/config.model';
import { DocumentDefinition, MappingDefinition } from '../models';
import { DocumentType, InspectionType } from '../common/config.types';
import { TextDecoder, TextEncoder } from 'text-encoding';

import { CommonUtil } from '../utils/common-util';
import { ErrorHandlerService } from './error-handler.service';
import { ErrorLevel } from '../models/error.model';
import { FileManagementService } from './file-management.service';
import FileSaver from 'file-saver';
import { InitializationService } from './initialization.service';
import fs from 'fs';
import ky from 'ky/umd';
import log from 'loglevel';
import { mocked } from 'ts-jest/utils';

describe('FileManagementService', () => {
  jest.mock('./initialization.service');
  const mockedInitService = mocked(InitializationService, true);
  jest.mock('ky');
  const mockedKy = mocked(ky, true);
  const service = new FileManagementService(ky);
  jest.mock('file-saver');
  const mockedFileSaver = mocked(FileSaver);
  jest.mock('../utils/common-util');
  const mockedDataMapperUtil = mocked(CommonUtil, true);

  beforeEach(() => {
    service.cfg = new ConfigModel();
    service.cfg.errorService = new ErrorHandlerService();
    service.cfg.logger = log.getLogger('config');
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
    service.findMappingFiles('UI').subscribe((value) => {
      expect(value.length).toBe(2);
      expect(value[0]).toBe('dummyMappingFile1');
      expect(value[1]).toBe('dummyMappingFile2');
      done();
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
    service.findMappingFiles('UI').subscribe({
      error: (error) => {
        expect(error).toMatch('expected error');
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(err.message.indexOf('current mapping files')).toBeGreaterThan(0);
        done();
      },
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
    service.getCurrentMappingDigest().subscribe((value) => {
      expect(new TextDecoder().decode(value)).toMatch('test text');
      done();
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
    service.getCurrentMappingDigest().subscribe({
      error: (error) => {
        expect(error).toMatch('expected error');
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(err.message.indexOf('Mapping digest file')).toBeGreaterThan(0);
        done();
      },
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
    service.getCurrentADMArchive().subscribe((value) => {
      expect(new TextDecoder().decode(value)).toMatch('test text');
      done();
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
    service.getCurrentADMArchive().subscribe({
      error: (error) => {
        expect(error).toMatch('expected error');
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(err.message.indexOf('ADM archive file')).toBeGreaterThan(0);
        done();
      },
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
    service.resetMappings().subscribe((value) => {
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
    service.resetMappings().subscribe({
      error: (error) => {
        fail(error);
      },
      complete: () => {
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(err.message.indexOf('resetting mappings')).toBeGreaterThan(0);
        done();
      },
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
    service.resetLibs().subscribe((value) => {
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
    service.resetLibs().subscribe({
      error: (error) => {
        fail(error);
      },
      complete: () => {
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(err.message.indexOf('JAR libraries')).toBeGreaterThan(0);
        done();
      },
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
    const mappingJson = '{"AtlasMapping": {}}';
    service.setMappingToService(mappingJson).subscribe((value) => {
      expect(value).toBeTruthy();
      done();
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
    const mappingJson = '{"AtlasMapping": {}}';
    service.setMappingToService(mappingJson).subscribe({
      error: (error) => {
        expect(error).toMatch('expected error');
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(err.message.indexOf('establishing mappings')).toBeGreaterThan(0);
        done();
      },
    });
  });

  test('setBinaryFileToService()', (done) => {
    mockedKy.put = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })()
    );
    const binary = new TextEncoder().encode('dummy binary');
    const url = service.cfg.initCfg.baseMappingServiceUrl + 'mapping/ZIP/';
    service.setBinaryFileToService(binary, url).subscribe((value) => {
      expect(value).toBeTruthy();
      done();
    });
  });

  test('setBinaryFileToService() server error', (done) => {
    mockedKy.put = jest.fn().mockReturnValue(
      new (class {
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.reject('expected error');
        }
      })()
    );
    const binary = new TextEncoder().encode('dummy binary');
    const url = service.cfg.initCfg.baseMappingServiceUrl + 'mapping/GZ/';
    service.setBinaryFileToService(binary, url).subscribe({
      error: (error) => {
        expect(error).toMatch('expected error');
        const err = service.cfg.errorService.getErrors()[0];
        expect(err.level).toBe(ErrorLevel.ERROR);
        expect(err.message.indexOf('saving mapping')).toBeGreaterThan(0);
        done();
      },
    });
  });

  test('setLibraryToService()', (done) => {
    mockedKy.put = jest.fn().mockReturnValue(
      new (class {
        blob(): Promise<Blob> {
          return Promise.resolve(new Blob());
        }
      })()
    );
    const binary = new TextEncoder().encode('dummy binary');
    service.setLibraryToService(binary, (success, _res) => {
      expect(success).toBeTruthy();
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
    mockedFileSaver.saveAs = jest.fn().mockImplementation((_data) => {});
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
    mockedDataMapperUtil.readBinaryFile = jest
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
    service
      .importADMArchive('atlasmap-mapping.adm')
      .then((value) => {
        expect(value).toBeTruthy();
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });
});
