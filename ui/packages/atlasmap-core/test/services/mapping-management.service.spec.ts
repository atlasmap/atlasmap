/* tslint:disable:no-unused-variable */

import ky from 'ky/umd';
import log from 'loglevel';

import { MappingManagementService } from '../../src/services/mapping-management.service';
import { Field } from '../../src/models/field.model';
import { DocumentDefinition, MappingDefinition, MappingModel } from '../../src';
import { ConfigModel } from '../../src/models/config.model';

describe('MappingManagementService', () => {
  const api = ky.create({ headers: { 'ATLASMAP-XSRF-TOKEN': 'awesome' } });
  const service = new MappingManagementService(api);
  const cfg = ConfigModel.getConfig();
  cfg.logger = log.getLogger('config');
  service.cfg = cfg;

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

  test('should not validate if no mappings', async () => {
    const mappingDefinition = new MappingDefinition();

    service.cfg = new ConfigModel();
    service.cfg.mappings = mappingDefinition;

    let result = await service.validateMappings();

    expect(result).toBe(false);
  });

  test('should not validate if no complete mapping', async () => {
    const source = new Field();

    const mappingModel = new MappingModel();
    mappingModel.addField(source, false);

    const mappingDefinition = new MappingDefinition();
    mappingDefinition.mappings.push(mappingModel);
    mappingModel.cfg.mappings = mappingDefinition;
    mappingDefinition.activeMapping = mappingModel;

    service.cfg = new ConfigModel();
    service.cfg.mappings = mappingDefinition;

    let result = await service.validateMappings();

    expect(result).toBe(false);
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
});
