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
  DocumentDefinition,
  PaddingField,
} from '../models/document-definition.model';
import { LookupTableData, LookupTableUtil, MappingSerializer } from '../utils';
import ky, { Input, Options } from 'ky';

import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { InitializationService } from './initialization.service';
import { MappingDefinition } from '../models/mapping-definition.model';
import { MappingManagementService } from '../services/mapping-management.service';
import { MappingModel } from '../models/mapping.model';
import { TestUtils } from '../../test/test-util';
import { TransitionMode } from '../models/transition.model';
import mockMappingJson from '../../../../test-resources/mapping/atlasmapping-mock.json';

describe('MappingManagementService', () => {
  let service: MappingManagementService;

  beforeEach(() => {
    const initService = new InitializationService(ky);
    const cfg = initService.cfg;
    service = cfg.mappingService;
  });

  function setSourceFieldValues(mapping: MappingModel) {
    let sourceFields = mapping.getFields(true);
    for (let i in sourceFields) {
      sourceFields[i].value = 'value-' + i;
    }
  }

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

  test('should clear out source fields when toggling expression with a source collection', () => {
    const source = new Field();
    source.isPrimitive = true;
    source.docDef = new DocumentDefinition();
    source.docDef.isSource = true;

    source.parentField = new Field();
    source.parentField.isCollection = true;

    source.parentField.parentField = new Field();
    source.parentField.parentField.isCollection = true;

    const target = new Field();
    target.isPrimitive = true;
    target.docDef = new DocumentDefinition();
    target.docDef.isSource = false;

    target.parentField = new Field();
    target.parentField.isCollection = true;

    target.parentField.parentField = new Field();
    target.parentField.parentField.isCollection = true;

    const mappingModel = new MappingModel();
    mappingModel.addField(source, false);
    mappingModel.addField(target, false);

    const mappingDefinition = new MappingDefinition();
    mappingDefinition.mappings.push(mappingModel);
    mappingModel.cfg.mappings = mappingDefinition;
    mappingDefinition.activeMapping = mappingModel;

    service.cfg = new ConfigModel();
    service.cfg.mappings = mappingDefinition;

    service.toggleExpressionMode();

    expect(service.willClearOutSourceFieldsOnTogglingExpression()).toBeTruthy();

    service.toggleExpressionMode();

    expect(service.cfg.mappings?.activeMapping?.sourceFields?.length).toBe(0);
    expect(service.cfg.mappings?.activeMapping?.targetFields?.length).toBe(1);
  });

  test('should not clear out source fields when toggling expression without a source collection', () => {
    const source = new Field();
    source.isPrimitive = true;
    source.docDef = new DocumentDefinition();
    source.docDef.isSource = true;

    source.parentField = new Field();
    source.parentField.isCollection = false;

    source.parentField.parentField = new Field();
    source.parentField.parentField.isCollection = false;

    const target = new Field();
    target.isPrimitive = true;
    target.docDef = new DocumentDefinition();
    target.docDef.isSource = false;

    target.parentField = new Field();
    target.parentField.isCollection = true;

    target.parentField.parentField = new Field();
    target.parentField.parentField.isCollection = true;

    const mappingModel = new MappingModel();
    mappingModel.addField(source, false);
    mappingModel.addField(target, false);

    const mappingDefinition = new MappingDefinition();
    mappingDefinition.mappings.push(mappingModel);
    mappingModel.cfg.mappings = mappingDefinition;
    mappingDefinition.activeMapping = mappingModel;

    service.cfg = new ConfigModel();
    service.cfg.mappings = mappingDefinition;

    spyOn<any>(service, 'updateTransition').and.stub();

    service.toggleExpressionMode();

    expect(service.willClearOutSourceFieldsOnTogglingExpression()).toBeFalsy();

    service.toggleExpressionMode();

    expect(service.cfg.mappings?.activeMapping?.sourceFields?.length).toBe(1);
    expect(service.cfg.mappings?.activeMapping?.targetFields?.length).toBe(1);
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
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        then(): Promise<any> {
          return Promise.resolve(options.json);
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

  test('fieldSelected()', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        then(): Promise<any> {
          return Promise.resolve(options.json);
        }
      })();
    });
    TestUtils.createMockMappings(service.cfg);
    const mapping = service.cfg.mappings!.mappings[1];
    const field2 = mapping.getMappedFieldForIndex('1', true);
    expect(field2?.field!.path).toBe('/sourceField2');
    expect(service.cfg.mappings?.activeMapping).toBeFalsy();
    service.fieldSelected(field2?.field!);
    expect(service.cfg.mappings?.activeMapping).toBe(mapping);
  });

  test('isFieldSelectable()', () => {
    const spy = spyOn(service, 'getFieldSelectionExclusionReason').and.stub();
    TestUtils.createMockMappings(service.cfg);
    const mapping = service.cfg.mappings!.mappings[1];
    const field2 = mapping.getMappedFieldForIndex('1', true);
    service.isFieldSelectable(mapping, field2?.field!);
    expect(spy.calls.count()).toBe(1);
  });

  test('addNewMapping()', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        then(): Promise<any> {
          return Promise.resolve(options.json);
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
        then(): Promise<any> {
          return Promise.resolve(options.json);
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
        then(): Promise<any> {
          return Promise.resolve(options.json);
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

  test('{enable,disable}MappingPreview()', (done) => {
    spyOn<any>(service, 'validateMappings').and.stub();
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
    TestUtils.createMockMappings(service.cfg);
    service.cfg.initCfg.baseMappingServiceUrl = 'http://dummy/';
    const mapping1 = service.cfg.mappings!.mappings[1];
    setSourceFieldValues(mapping1);
    service.selectMapping(mapping1);
    service.enableMappingPreview();
    expect(requests.length).toBe(0);
    service
      .notifyMappingUpdated()
      .then((value) => {
        expect(value).toBeTruthy();
        expect(requests.length).toBe(1);
        service.disableMappingPreview();
        service
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

  test('removeDocumentReferenceFromAllMappings()', () => {
    spyOn(ky, 'put').and.callFake((_url: Input, options: Options) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve(options.json);
        }
        then(): Promise<any> {
          return Promise.resolve(options.json);
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

  test('willClearOutSourceFieldsOnTogglingExpression()', () => {
    TestUtils.createMockMappings(service.cfg);
    const mapping1 = service.cfg.mappings!.mappings[1];
    expect(service.cfg.mappings?.activeMapping).toBeNull();
    service.selectMapping(mapping1);
    expect(service.willClearOutSourceFieldsOnTogglingExpression()).toBeFalsy();
  });

  test('conditionalMappingExpressionEnabled()', () => {
    TestUtils.createMockMappings(service.cfg);
    const mapping1 = service.cfg.mappings!.mappings[1];
    expect(service.cfg.mappings?.activeMapping).toBeNull();
    service.selectMapping(mapping1);
    expect(service.conditionalMappingExpressionEnabled()).toBeFalsy();
  });

  test('toggleExpressionMode', () => {
    spyOn<any>(service, 'validateMappings').and.stub();
    TestUtils.createMockMappings(service.cfg);
    const mapping1 = service.cfg.mappings!.mappings[1];
    expect(service.cfg.mappings?.activeMapping).toBeNull();
    service.selectMapping(mapping1);
    service.toggleExpressionMode();
    expect(service.conditionalMappingExpressionEnabled()).toBeTruthy();
  });

  test('updateMappings()', () => {
    const spyValidation = spyOn<any>(service, 'validateMappings').and.stub();
    const spyPut = spyOn(ky, 'put').and.callFake(
      (_url: Input, options: Options) => {
        return new (class {
          json(): Promise<any> {
            return Promise.resolve(options.json);
          }
          then(): Promise<any> {
            return Promise.resolve(options.json);
          }
        })();
      }
    );
    TestUtils.createMockMappings(service.cfg);
    const payload: any = MappingSerializer.serializeMappings(service.cfg);
    service.updateMappings(payload);
    expect(spyValidation.calls.count()).toBe(0);
    expect(spyPut.calls.count()).toBe(1);
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

  test('executeFieldSearch()', () => {
    TestUtils.createMockMappings(service.cfg);
    const mapping1 = service.cfg.mappings!.mappings[1];
    expect(service.cfg.mappings?.activeMapping).toBeNull();
    service.selectMapping(mapping1);
    expect(
      service.executeFieldSearch(service.cfg, 'sourceField', true).length
    ).toBe(4);
    expect(
      service.executeFieldSearch(service.cfg, 'sourceField2', true).length
    ).toBe(2);
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

  test('getRuntimeVersion()', (done) => {
    spyOn(ky, 'get').and.returnValue(
      new (class {
        json(): Promise<any> {
          return Promise.resolve({ string: '10.9.8' });
        }
      })()
    );
    service
      .getRuntimeVersion()
      .then((value) => {
        expect(value).toBe('10.9.8');
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });
});
