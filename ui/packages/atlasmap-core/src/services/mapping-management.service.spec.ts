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
import { LookupTableData, LookupTableUtil } from '../utils';
import ky, { Options } from 'ky';

import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { InitializationService } from './initialization.service';
import { Input } from 'ky/distribution/types/options';
import { MappingDefinition } from '../models/mapping-definition.model';
import { MappingManagementService } from '../services/mapping-management.service';
import { MappingModel } from '../models/mapping.model';
import { PaddingField } from '../models/document-definition.model';
import { TestUtils } from '../../test/test-util';
import { TransitionMode } from '../models/transition.model';
import mockMappingJson from '../../../../test-resources/mapping/atlasmapping-mock.json';

describe('MappingManagementService', () => {
  let cfg: ConfigModel;
  let service: MappingManagementService;

  beforeEach(() => {
    const initService = new InitializationService(ky);
    cfg = initService.cfg;
    service = cfg.mappingService;
  });

  test('check banned fields', () => {
    const f = new Field();
    f.isCollection = true;
    f.parentField = new Field();
    f.parentField.isCollection = true;
    f.isPrimitive = true;
    expect(
      service.getFieldSelectionExclusionReason(
        new MappingModel(),
        f.parentField
      )
    ).toContain('parent');
  });

  test('fetchMappings()', (done) => {
    spyOn(ky, 'get').and.returnValue(
      new (class {
        json(): Promise<any> {
          return Promise.resolve(mockMappingJson);
        }
      })()
    );
    TestUtils.createMockDocs(service.cfg);
    service.cfg.mappings = new MappingDefinition();
    service
      .fetchMappings([''], service.cfg.mappings)
      .then((value) => {
        expect(value).toBeTruthy();
        expect(service.cfg.mappings?.mappings.length).toBeGreaterThan(0);
        const m = service.cfg.mappings?.mappings[0];
        expect(m?.transition.mode).toBe(TransitionMode.ONE_TO_ONE);
        const sf = m?.sourceFields[0].field;
        expect(sf?.name).toBe('sourceField');
        const tf = m?.targetFields[0].field;
        expect(tf?.name).toBe('targetField');
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('updateMappingsTransition()', () => {
    spyOn<any>(service, 'validateMappings').and.stub();
    TestUtils.createMockMappings(service.cfg);
    service.cfg.mappings!.mappings[0].transition.mode =
      TransitionMode.MANY_TO_ONE;
    expect(
      service.cfg.mappings!.mappings[0].transition.isOneToOneMode()
    ).toBeFalsy();
    expect(
      service.cfg.mappings!.mappings[0].transition.isManyToOneMode()
    ).toBeTruthy();
    service.updateMappingsTransition();
    expect(
      service.cfg.mappings!.mappings[0].transition.isOneToOneMode()
    ).toBeTruthy();
    expect(
      service.cfg.mappings!.mappings[0].transition.isManyToOneMode()
    ).toBeFalsy();
  });

  test('removeMapping()', (done) => {
    spyOn<any>(service, 'validateMappings').and.stub();
    TestUtils.createMockMappings(service.cfg);
    const toRemove = service.cfg.mappings!.mappings[0];
    expect(service.cfg.mappings!.mappings.length).toBe(2);
    service
      .removeMapping(toRemove)
      .then((value) => {
        expect(value).toBeTruthy();
        expect(service.cfg.mappings!.mappings.length).toBe(1);
        expect(service.cfg.mappings!.mappings[0].uuid).not.toBe(toRemove.uuid);
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('removeAllMappings()', (done) => {
    spyOn<any>(service, 'validateMappings').and.stub();
    TestUtils.createMockMappings(service.cfg);
    expect(service.cfg.mappings?.mappings[0]);
    service
      .removeAllMappings()
      .then((value) => {
        expect(value).toBeTruthy();
        expect(service.cfg.mappings?.mappings[0]).toBeUndefined();
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('updateMappedField()', (done) => {
    spyOn<any>(service, 'validateMappings').and.stub();
    const spy = spyOn<any>(service, 'updateTransition').and.stub();
    TestUtils.createMockMappings(service.cfg);
    service
      .updateMappedField(service.cfg.mappings!.mappings[0])
      .then((value) => {
        expect(value).toBeTruthy();

        expect(spy.calls.count()).toBe(1);
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('moveMappedFieldTo', () => {
    spyOn(service, 'notifyLineRefresh').and.stub();
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockMappings(service.cfg);
    const mapping = service.cfg.mappings!.mappings[1];
    const field2 = mapping.getMappedFieldForIndex('1', true);
    expect(field2?.field?.path).toBe('/sourceField2');
    const field = mapping.getMappedFieldForIndex('2', true);
    expect(field?.field?.path).toBe('/sourceField');
    service.moveMappedFieldTo(mapping, field2!, 2);
    const movedField = mapping.getMappedFieldForIndex('1', true);
    expect(movedField?.field?.path).toBe('/sourceField');
  });

  test('addPlaceHolders()', () => {
    TestUtils.createMockMappings(service.cfg);
    const mapping = service.cfg.mappings!.mappings[1];
    const field = mapping.getMappedFieldForIndex('2', true);
    expect(field?.field!.path).toBe('/sourceField');
    service.addPlaceholders(3, mapping, 1, true);
    const moved = mapping.getMappedFieldForIndex('5', true);
    expect(moved?.field?.path).toBe('/sourceField');
    const pad = mapping.getMappedFieldForIndex('4', true);
    expect(pad?.field!).toBeInstanceOf(PaddingField);
  });

  test('addFieldToActiveMapping()', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockMappings(service.cfg);
    const mapping = service.cfg.mappings!.mappings[1];
    const field2 = mapping.getMappedFieldForIndex('1', true);
    expect(field2?.field!.path).toBe('/sourceField2');
    service.selectMapping(mapping);
    expect(mapping.sourceFields.length).toBe(2);
    const field3 = field2?.field?.docDef.getField('/sourceField3');
    expect(field3).toBeTruthy();
    service.addFieldToActiveMapping(field3!);
    expect(mapping.sourceFields.length).toBe(3);
  });

  test('isFieldSelectable()', () => {
    const spy = spyOn(service, 'getFieldSelectionExclusionReason').and.stub();
    TestUtils.createMockMappings(service.cfg);
    const mapping = service.cfg.mappings!.mappings[1];
    const field2 = mapping.getMappedFieldForIndex('1', true);
    service.isFieldSelectable(mapping, field2?.field!);
    expect(spy.calls.count()).toBe(1);
  });

  test('isFieldAddableToActiveMapping() - one-to-one/one-to-many', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockDocs(service.cfg);
    service.cfg.mappings = new MappingDefinition();
    const srcDoc = service.cfg.sourceDocs[0];
    const tgtDoc = service.cfg.targetDocs[0];
    const srcF = srcDoc.getField('/sourceField');
    service.addNewMapping(srcF!, false);
    const srcCF = srcDoc.getField('/sourceCollectionField<>');
    expect(service.isFieldAddableToActiveMapping(srcCF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcF!)).toBeFalsy();
    const tgtF = tgtDoc.getField('/targetField');
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeTruthy();
    service.addFieldToActiveMapping(tgtF!);
    const tgtCF = tgtDoc.getField('/targetCollectionField<>');
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcCF!)).toBeFalsy();
    const srcF2 = srcDoc.getField('/sourceField2');
    expect(service.isFieldAddableToActiveMapping(srcF2!)).toBeTruthy();
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeFalsy();
    const tgtF2 = tgtDoc.getField('/targetField2');
    expect(service.isFieldAddableToActiveMapping(tgtF2!)).toBeTruthy();
    service.addFieldToActiveMapping(tgtF2!);
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcCF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcF2!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(tgtF2!)).toBeFalsy();
    const tgtF3 = tgtDoc.getField('/targetField3');
    expect(service.isFieldAddableToActiveMapping(tgtF3!)).toBeTruthy();
    // should be able to create a new mapping
    expect(service.isFieldDragAndDropAllowed(srcF2!, tgtF3!)).toBeTruthy();
  });

  test('isFieldAddableToActiveMapping() - one-to-collection', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockDocs(service.cfg);
    service.cfg.mappings = new MappingDefinition();
    const srcDoc = service.cfg.sourceDocs[0];
    const tgtDoc = service.cfg.targetDocs[0];
    const srcF = srcDoc.getField('/sourceField');
    service.addNewMapping(srcF!, false);
    const tgtCF = tgtDoc.getField('/targetCollectionField<>');
    service.addFieldToActiveMapping(tgtCF!);
    const srcCF = srcDoc.getField('/sourceCollectionField<>');
    expect(service.isFieldAddableToActiveMapping(srcCF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcF!)).toBeFalsy();
    const tgtF = tgtDoc.getField('/targetField');
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeFalsy();
    const tgtCF2 = tgtDoc.getField('/targetCollectionField2<>');
    expect(service.isFieldAddableToActiveMapping(tgtCF2!)).toBeFalsy();
  });

  test('isFieldAddableToActiveMapping() - many-to-one', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockDocs(service.cfg);
    service.cfg.mappings = new MappingDefinition();
    const srcDoc = service.cfg.sourceDocs[0];
    const tgtDoc = service.cfg.targetDocs[0];
    const srcF = srcDoc.getField('/sourceField');
    service.addNewMapping(srcF!, false);
    const srcF2 = srcDoc.getField('/sourceField2');
    service.addFieldToActiveMapping(srcF2!);
    const srcCF = srcDoc.getField('/sourceCollectionField<>');
    expect(service.isFieldAddableToActiveMapping(srcCF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcF2!)).toBeFalsy();
    const srcF3 = srcDoc.getField('/sourceField3');
    expect(service.isFieldAddableToActiveMapping(srcF3!)).toBeTruthy();
    const tgtF = tgtDoc.getField('/targetField');
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeTruthy();
    const tgtCF = tgtDoc.getField('/targetCollectionField<>');
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeTruthy();
    service.addFieldToActiveMapping(tgtF!);
    expect(service.isFieldAddableToActiveMapping(srcF3!)).toBeTruthy();
    expect(service.isFieldAddableToActiveMapping(srcCF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeFalsy();
    const tgtF2 = tgtDoc.getField('/targetField2<>');
    expect(service.isFieldAddableToActiveMapping(tgtF2!)).toBeFalsy();
  });

  test('isFieldAddableToActiveMapping() - collection-to-one', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockDocs(service.cfg);
    service.cfg.mappings = new MappingDefinition();
    const srcDoc = service.cfg.sourceDocs[0];
    const tgtDoc = service.cfg.targetDocs[0];
    const srcCF = srcDoc.getField('/sourceCollectionField<>');
    service.addNewMapping(srcCF!, false);
    expect(service.isFieldAddableToActiveMapping(srcCF!)).toBeFalsy();
    const srcF = srcDoc.getField('/sourceField');
    expect(service.isFieldAddableToActiveMapping(srcF!)).toBeFalsy();
    const srcCF2 = srcDoc.getField('/sourceCollectionField2<>');
    expect(service.isFieldAddableToActiveMapping(srcCF2!)).toBeFalsy();
    const tgtF = tgtDoc.getField('/targetField');
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeTruthy();
    const tgtCF = tgtDoc.getField('/targetCollectionField<>');
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeTruthy();
    service.addFieldToActiveMapping(tgtF!);
    expect(service.isFieldAddableToActiveMapping(srcCF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcCF2!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeFalsy();
  });

  test('isFieldAddableToActiveMapping() - for-each', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockDocs(service.cfg);
    service.cfg.mappings = new MappingDefinition();
    const srcDoc = service.cfg.sourceDocs[0];
    const tgtDoc = service.cfg.targetDocs[0];
    const srcCF = srcDoc.getField('/sourceCollectionField<>');
    service.addNewMapping(srcCF!, false);
    expect(service.isFieldAddableToActiveMapping(srcCF!)).toBeFalsy();
    const srcF = srcDoc.getField('/sourceField');
    expect(service.isFieldAddableToActiveMapping(srcF!)).toBeFalsy();
    const srcCF2 = srcDoc.getField('/sourceCollectionField2<>');
    expect(service.isFieldAddableToActiveMapping(srcCF2!)).toBeFalsy();
    const tgtF = tgtDoc.getField('/targetField');
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeTruthy();
    const tgtCF = tgtDoc.getField('/targetCollectionField<>');
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeTruthy();
    service.addFieldToActiveMapping(tgtCF!);
    expect(service.isFieldAddableToActiveMapping(srcCF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcCF2!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeFalsy();
    const tgtCF2 = tgtDoc.getField('/targetCollectionField2<>');
    expect(service.isFieldAddableToActiveMapping(tgtCF2!)).toBeFalsy();
  });

  test('isFieldAddableToActiveMapping() - for-each - many-to-collection', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockDocs(service.cfg);
    service.cfg.mappings = new MappingDefinition();
    const srcDoc = service.cfg.sourceDocs[0];
    const tgtDoc = service.cfg.targetDocs[0];
    const srcF = srcDoc.getField('/sourceField');
    service.addNewMapping(srcF!, false);
    const srcF2 = srcDoc.getField('/sourceField2');
    expect(service.isFieldAddableToActiveMapping(srcF!)).toBeFalsy();
    expect(service.isFieldAddableToActiveMapping(srcF2!)).toBeTruthy();
    service.addFieldToActiveMapping(srcF2!);
    const srcF3 = srcDoc.getField('/sourceField3');
    expect(service.isFieldAddableToActiveMapping(srcF3!)).toBeTruthy();
    const tgtCF = tgtDoc.getField('/targetCollectionField<>');
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeTruthy();
    service.addFieldToActiveMapping(tgtCF!);
    expect(service.isFieldAddableToActiveMapping(srcF3!)).toBeTruthy();
  });

  test('isFieldAddableToActiveMapping() - for-each - collection-to-many', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockDocs(service.cfg);
    service.cfg.mappings = new MappingDefinition();
    const srcDoc = service.cfg.sourceDocs[0];
    const tgtDoc = service.cfg.targetDocs[0];
    const srcCF = srcDoc.getField('/sourceCollectionField<>');
    service.addNewMapping(srcCF!, false);
    expect(service.isFieldAddableToActiveMapping(srcCF!)).toBeFalsy();
    const tgtF = tgtDoc.getField('/targetField');
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeTruthy();
    service.addFieldToActiveMapping(tgtF!);
    const tgtF2 = tgtDoc.getField('/targetField2');
    expect(service.isFieldAddableToActiveMapping(tgtF2!)).toBeTruthy();
    service.addFieldToActiveMapping(tgtF2!);
    const tgtF3 = tgtDoc.getField('/targetField3');
    expect(service.isFieldAddableToActiveMapping(tgtF3!)).toBeTruthy();
  });

  test('isFieldAddableToActiveMapping() - multiple mappings to the non-collection target should not be allowed', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockDocs(service.cfg);
    service.cfg.mappings = new MappingDefinition();
    const srcDoc = service.cfg.sourceDocs[0];
    const tgtDoc = service.cfg.targetDocs[0];
    const srcF = srcDoc.getField('/sourceField');
    service.addNewMapping(srcF!, false);
    const tgtF = tgtDoc.getField('/targetField');
    service.addFieldToActiveMapping(tgtF!);
    const srcF2 = srcDoc.getField('/sourceField2');
    service.addNewMapping(srcF2!, false);
    const tgtF2 = tgtDoc.getField('/targetField2');
    expect(service.isFieldDragAndDropAllowed(srcF!, tgtF2!));
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeFalsy();
    expect(service.isFieldDragAndDropAllowed(srcF2!, tgtF!)).toBeFalsy();
    service.deselectMapping();
    expect(service.isFieldDragAndDropAllowed(srcF!, tgtF2!));
    expect(service.isFieldAddableToActiveMapping(tgtF!)).toBeFalsy();
    expect(service.isFieldDragAndDropAllowed(srcF2!, tgtF!)).toBeFalsy();
  });

  test('isFieldAddableToActiveMapping() - multiple mappings to the collection target should be allowed', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockDocs(service.cfg);
    service.cfg.mappings = new MappingDefinition();
    const srcDoc = service.cfg.sourceDocs[0];
    const tgtDoc = service.cfg.targetDocs[0];
    const srcF = srcDoc.getField('/sourceField');
    service.addNewMapping(srcF!, false);
    const tgtCF = tgtDoc.getField('/targetCollectionField<>');
    service.addFieldToActiveMapping(tgtCF!);
    const srcF2 = srcDoc.getField('/sourceField2');
    service.addNewMapping(srcF2!, false);
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeTruthy();
    expect(service.isFieldDragAndDropAllowed(srcF2!, tgtCF!)).toBeTruthy();
    service.deselectMapping();
    expect(service.isFieldAddableToActiveMapping(tgtCF!)).toBeTruthy();
    expect(service.isFieldDragAndDropAllowed(srcF2!, tgtCF!)).toBeTruthy();
  });

  test('addNewMapping()', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockMappings(service.cfg);
    const doc = service.cfg.sourceDocs[0];
    const field3 = doc.getField('/sourceField3');
    expect(service.cfg.mappings?.mappings.length).toBe(2);
    service.addNewMapping(field3!, false);
    expect(service.cfg.mappings?.mappings.length).toBe(3);
    const mapping = service.cfg.mappings?.mappings[2];
    const mappedField = mapping?.getMappedFieldForField(field3!);
    expect(mappedField).toBeTruthy();
  });

  test('newMapping()', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockMappings(service.cfg);
    expect(service.cfg.mappings?.mappings.length).toBe(2);
    service.newMapping();
    expect(service.cfg.mappings?.mappings.length).toBe(3);
  });

  test('selectMapping()', () => {
    TestUtils.createMockMappings(service.cfg);
    const mapping1 = service.cfg.mappings!.mappings[1];
    expect(service.cfg.mappings?.activeMapping).toBeNull();
    service.selectMapping(mapping1);
    expect(service.cfg.mappings?.activeMapping).toBe(mapping1);
  });

  test('{select,deselect}Mapping()', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockMappings(service.cfg);
    const mapping1 = service.cfg.mappings!.mappings[1];
    expect(service.cfg.mappings?.activeMapping).toBeNull();
    service.selectMapping(mapping1);
    expect(service.cfg.mappings?.activeMapping).toBe(mapping1);
    service.deselectMapping();
    expect(service.cfg.mappings?.activeMapping).toBeNull();
  });

  test('removeDocumentReferenceFromAllMappings()', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        arrayBuffer(): Promise<ArrayBuffer> {
          return Promise.resolve(new ArrayBuffer(0));
        }
      })();
    });
    TestUtils.createMockMappings(service.cfg);
    expect(service.cfg.mappings?.mappings.length).toBe(2);
    service.removeDocumentReferenceFromAllMappings('SourceJson');
    expect(service.cfg.mappings!.mappings.length).toBe(0);
  });

  test('removeFieldFromAllMappings()', () => {
    spyOn<any>(service, 'validateMappings').and.stub();
    TestUtils.createMockMappings(service.cfg);
    expect(service.cfg.mappings?.mappings.length).toBe(2);
    const doc = service.cfg.sourceDocs[0];
    const field = doc.getField('/sourceField');
    service.removeFieldFromAllMappings(field!);
    // **WARN** mapping is not removed even if there's no source field,
    // inconsistent with removeDocumentReferenceFromAllMappings()
    expect(service.cfg.mappings?.mappings.length).toBe(2);
    const mapping = service.cfg.mappings?.mappings[0];
    expect(mapping?.isFullyMapped()).toBeFalsy();
  });

  test('notifyLineRefresh()', (done) => {
    spyOn<any>(service, 'validateMappings').and.stub();
    const subscription = service.lineRefresh$.subscribe({
      next() {
        subscription.unsubscribe();
        done();
      },
      error(error) {
        fail(error);
      },
    });
    service.notifyLineRefresh();
  });

  test('notifyMappingUpdated()', (done) => {
    spyOn<any>(service, 'validateMappings').and.stub();
    TestUtils.createMockMappings(service.cfg);
    const mapping1 = service.cfg.mappings!.mappings[1];
    expect(service.cfg.mappings?.activeMapping).toBeNull();
    service.selectMapping(mapping1);
    service
      .notifyMappingUpdated()
      .then((value) => {
        expect(value).toBeTruthy();
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('getEnumerationValues()', () => {
    const spy = spyOn<any>(LookupTableUtil, 'getEnumerationValues').and.stub();
    const mapping = new MappingModel();
    expect(spy.calls.count()).toBe(0);
    service.getEnumerationValues(service.cfg, mapping);
    expect(spy.calls.count()).toBe(1);
  });

  test('setEnumFieldValue()', () => {
    const field = new Field();
    const value = 0;
    expect(field.enumIndexValue).toBeFalsy();
    service.setEnumFieldValue(field, value);
    expect(field.enumIndexValue).toBe(0);
  });

  test('updateEnumerationValues()', () => {
    const spyUev = spyOn<any>(
      LookupTableUtil,
      'updateEnumerationValues'
    ).and.stub();
    const spyNmu = spyOn<any>(service, 'notifyMappingUpdated').and.stub();
    const mapping = new MappingModel();
    const enumValues: LookupTableData[] = [];
    expect(spyUev.calls.count()).toBe(0);
    expect(spyNmu.calls.count()).toBe(0);
    service.updateEnumerationValues(service.cfg, mapping, enumValues);
    expect(spyUev.calls.count()).toBe(1);
    expect(spyNmu.calls.count()).toBe(1);
  });

  test('isEnumerationMapping()', () => {
    const mapping = new MappingModel();
    mapping.transition.mode = TransitionMode.ENUM;
    expect(service.isEnumerationMapping(mapping)).toBeTruthy();
  });
});
