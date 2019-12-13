/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { MappingManagementService } from '../src/services/mapping-management.service';
import { ErrorHandlerService } from '../src/services/error-handler.service';
import { Field } from '../src/models/field.model';
import { DocumentDefinition, MappingDefinition, MappingModel } from '../src';

describe('MappingManagementService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot({ level: NgxLoggerLevel.DEBUG })],
      providers: [ErrorHandlerService, MappingManagementService, NGXLogger],
    });
  });

  it('should check banned fields', inject(
    [MappingManagementService],
    (service: MappingManagementService) => {
      const f = new Field();
      f.isCollection = true;
      f.parentField = new Field();
      f.parentField.isCollection = true;
      f.isPrimitive = true;
      expect(
        service.getFieldSelectionExclusionReason(null, f.parentField)
      ).toContain('parent');
    }
  ));

  it('should reject if source and target have different collection counts and target has more than one collection', inject(
    [MappingManagementService],
    (service: MappingManagementService) => {
      const source = new Field();
      source.isPrimitive = true;
      source.docDef = new DocumentDefinition();
      source.docDef.isSource = true;

      source.parentField = new Field();
      source.parentField.isCollection = true;

      const target = new Field();
      target.isPrimitive = true;
      target.docDef = new DocumentDefinition();
      target.docDef.isSource = false;

      target.parentField = new Field();
      target.parentField.isCollection = true;

      target.parentField.parentField = new Field();
      target.parentField.parentField.isCollection = true;

      const mappingModel = new MappingModel();
      mappingModel.addField(target, true);

      const mappingDefinition = new MappingDefinition();
      mappingDefinition.mappings.push(mappingModel);
      mappingModel.cfg.mappings = mappingDefinition;

      expect(
        service.getFieldSelectionExclusionReason(mappingModel, source)
      ).toContain(
        'source and target must ' +
          'have the same nested collection count or target must have a single nested collection on the path'
      );
    }
  ));

  it('should not reject if source and target have same collection counts', inject(
    [MappingManagementService],
    (service: MappingManagementService) => {
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
      mappingModel.addField(target, true);

      const mappingDefinition = new MappingDefinition();
      mappingDefinition.mappings.push(mappingModel);
      mappingModel.cfg.mappings = mappingDefinition;

      expect(
        service.getFieldSelectionExclusionReason(mappingModel, source)
      ).toBeNull();
    }
  ));

  it('should not reject, if source has many collections, but target has one collection on the path', inject(
    [MappingManagementService],
    (service: MappingManagementService) => {
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

      const mappingModel = new MappingModel();
      mappingModel.addField(target, true);

      const mappingDefinition = new MappingDefinition();
      mappingDefinition.mappings.push(mappingModel);
      mappingModel.cfg.mappings = mappingDefinition;

      expect(
        service.getFieldSelectionExclusionReason(mappingModel, source)
      ).toBeNull();
    }
  ));
});
