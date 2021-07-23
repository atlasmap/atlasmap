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
import { DocumentType, InspectionType } from '../contracts/common';

import { DocumentDefinition } from '../models/document-definition.model';
import { ExpressionNode } from '../models/expression.model';
import { Field } from '../models/field.model';
import { InitializationService } from './initialization.service';
import { MappingDefinition } from '../models/mapping-definition.model';
import { MappingExpressionService } from './mapping-expression.service';
import { MappingModel } from '../models/mapping.model';
import { TestUtils } from '../../test/test-util';
import ky from 'ky';

describe('MappingExpressionService', () => {
  let cfg: ConfigModel;
  let service: MappingExpressionService;

  beforeEach(() => {
    const initService = new InitializationService(ky);
    initService.initialize();
    cfg = initService.cfg;
    service = cfg.expressionService;
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

    service.cfg.mappings = mappingDefinition;

    spyOn<any>(cfg.mappingService, 'updateTransition').and.stub();

    service.toggleExpressionMode();

    expect(service.willClearOutSourceFieldsOnTogglingExpression()).toBeFalsy();

    service.toggleExpressionMode();

    expect(service.cfg.mappings?.activeMapping?.sourceFields?.length).toBe(1);
    expect(service.cfg.mappings?.activeMapping?.targetFields?.length).toBe(1);
  });

  test('willClearOutSourceFieldsOnTogglingExpression()', () => {
    TestUtils.createMockMappings(service.cfg);
    const mapping1 = service.cfg.mappings!.mappings[1];
    expect(service.cfg.mappings?.activeMapping).toBeNull();
    cfg.mappingService.selectMapping(mapping1);
    expect(service.willClearOutSourceFieldsOnTogglingExpression()).toBeFalsy();
  });

  test('conditionalMappingExpressionEnabled()', () => {
    TestUtils.createMockMappings(service.cfg);
    const mapping1 = service.cfg.mappings!.mappings[1];
    expect(service.cfg.mappings?.activeMapping).toBeNull();
    cfg.mappingService.selectMapping(mapping1);
    expect(service.isExpressionEnabledForActiveMapping()).toBeFalsy();
  });

  test('toggleExpressionMode', () => {
    spyOn<any>(cfg.mappingService, 'validateMappings').and.stub();
    TestUtils.createMockMappings(service.cfg);
    const mapping1 = service.cfg.mappings!.mappings[1];
    expect(service.cfg.mappings?.activeMapping).toBeNull();
    cfg.mappingService.selectMapping(mapping1);
    service.toggleExpressionMode();
    expect(service.isExpressionEnabledForActiveMapping()).toBeTruthy();
  });

  test('executeFieldSearch()', () => {
    TestUtils.createMockMappings(service.cfg);
    const mapping1 = service.cfg.mappings!.mappings[1];
    expect(service.cfg.mappings?.activeMapping).toBeNull();
    cfg.mappingService.selectMapping(mapping1);
    expect(
      service.executeFieldSearch(service.cfg, 'sourceField', true).length
    ).toBe(4);
    expect(
      service.executeFieldSearch(service.cfg, 'sourceField2', true).length
    ).toBe(2);
  });

  test('createMappingExpression', () => {
    TestUtils.createMockMappings(cfg);
    const mapping1 = cfg.mappings!.mappings[1];
    expect(service.createMappingExpression(mapping1)).toContain('Concatenate');
  });

  test('test adding a field to an expression makes it into the mapping', () => {
    TestUtils.createMockMappings(cfg);
    const mapping = cfg.mappings!.mappings[1];

    const docDef = new DocumentInitializationModel();
    docDef.type = DocumentType.JSON;
    docDef.inspectionType = InspectionType.SCHEMA;
    docDef.name = 'JSONSchemaSource';
    docDef.isSource = true;
    docDef.id = 'JSONSchemaSource';
    docDef.description = 'random desc';
    const jsonSchemaSource = cfg.addDocument(docDef);

    const source = new Field();
    source.isPrimitive = true;
    source.path = '/addressList<>/city';
    source.docDef = jsonSchemaSource;
    source.parentField = new Field();
    source.parentField.isCollection = true;
    source.parentField.parentField = new Field();
    source.parentField.parentField.isCollection = true;
    jsonSchemaSource.addField(source);

    expect(mapping).toBeDefined();
    cfg.mappingService.selectMapping(mapping);

    const textNode: ExpressionNode = {
      uuid: '0',
      str: 'mockstr',
      getUuid: () => '',
      toText: () => '',
      toHTML: () => '',
    };
    service.addFieldToExpression(
      mapping,
      source.docDef.id,
      source.path,
      textNode,
      0,
      true
    );
    const mappedField = mapping.getMappedFieldByPath(
      source.path,
      true,
      source.docDef.id
    );
    expect(mappedField?.field?.path).toEqual(source.path);
  });
});
