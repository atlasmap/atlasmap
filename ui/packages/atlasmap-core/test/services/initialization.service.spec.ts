/* tslint:disable:no-unused-variable */

import ky from 'ky/umd';
import log from 'loglevel';

import { DocumentType, InspectionType } from '../../src/common/config.types';
import { DocumentManagementService } from '../../src/services/document-management.service';
import { ErrorHandlerService } from '../../src/services/error-handler.service';
import { InitializationService } from '../../src/services/initialization.service';
import { MappingManagementService } from '../../src/services/mapping-management.service';
import { FieldActionService } from '../../src/services/field-action.service';
import { FileManagementService } from '../../src/services/file-management.service';
import { DocumentInitializationModel } from '../../src/models/config.model';
import { Observable, of } from 'rxjs';

import atlasmapFieldActionJson from '../../../../test-resources/fieldActions/atlasmap-field-action.json';
import atlasmapInspectionMockJsonSchemaJson from '../../../../test-resources/inspected/atlasmap-inspection-mock-json-schema.json';
import atlasmapInspectionMockXmlInstance1Json from '../../../../test-resources/inspected/atlasmap-inspection-mock-xml-instance-1.json';
import atlasmapInspectionMockJsonInstanceJson from '../../../../test-resources/inspected/atlasmap-inspection-mock-json-instance.json';
import atlasmapInspectionMockXmlSchema1Json from '../../../../test-resources/inspected/atlasmap-inspection-mock-xml-schema-1.json';
import atlasmapInspectionOldActionSourceJson from '../../../../test-resources/inspected/atlasmap-inspection-old-action-source.json';
import atlasmapInspectionOldActionTargetJson from '../../../../test-resources/inspected/atlasmap-inspection-old-action-target.json';
import atlasmappingOldActionJson from '../../../../test-resources/mapping/atlasmapping-old-action.json';

describe('InitializationService', () => {

  const api = ky.create({ headers: { "ATLASMAP-XSRF-TOKEN": "awesome" } });
  const documentService = new DocumentManagementService(api);
  const mappingService = new MappingManagementService(api);
  const errorService = new ErrorHandlerService();
  const fieldActionService= new FieldActionService(api);
  const fileService = new FileManagementService(api);
  const service = new InitializationService(documentService, mappingService, errorService, fieldActionService, fileService);
  service.cfg.logger = log.getLogger('config');

  afterEach(() => {
      service.cfg.clearDocs();
      if (service.cfg.mappings) {
        service.cfg.mappings.mappings = [];
      }
  });

  it('load document definitions', (done) => {
        const c = service.cfg;
        c.initCfg.baseMappingServiceUrl = 'dummy';
        c.initCfg.baseJSONInspectionServiceUrl = 'dummy';
        c.initCfg.baseXMLInspectionServiceUrl = 'dummy';
        c.initCfg.baseCSVInspectionServiceUrl = 'dummy';
        const sourceJson = new DocumentInitializationModel();
        sourceJson.isSource = true;
        sourceJson.type = DocumentType.JSON;
        sourceJson.inspectionType = InspectionType.SCHEMA;
        sourceJson.inspectionResult = JSON.stringify(atlasmapInspectionMockJsonSchemaJson);
        c.addDocument(sourceJson);
        const sourceXml = new DocumentInitializationModel();
        sourceXml.isSource = true;
        sourceXml.type = DocumentType.XML;
        sourceXml.inspectionType = InspectionType.INSTANCE;
        sourceXml.inspectionResult = JSON.stringify(atlasmapInspectionMockXmlInstance1Json);
        c.addDocument(sourceXml);
        const targetJson = new DocumentInitializationModel();
        targetJson.isSource = false;
        targetJson.type = DocumentType.JSON;
        targetJson.inspectionType = InspectionType.INSTANCE;
        targetJson.inspectionResult = JSON.stringify(atlasmapInspectionMockJsonInstanceJson);
        c.addDocument(targetJson);
        const targetXml = new DocumentInitializationModel();
        targetXml.isSource = false;
        targetXml.type = DocumentType.XML;
        targetXml.inspectionType = InspectionType.SCHEMA;
        targetXml.inspectionResult = JSON.stringify(atlasmapInspectionMockXmlSchema1Json);
        c.addDocument(targetXml);
        spyOn(c.mappingService, 'runtimeServiceActive').and.returnValue(Promise.resolve(true));
        return service.initialize().then(() => {
          expect(c.sourceDocs[0].fields[0].path).toEqual('/addressList<>');
          expect(c.sourceDocs[1].fields[0].path).toEqual('/data');
          expect(c.targetDocs[0].fields[0].path).toEqual('/addressList<>');
          expect(c.targetDocs[1].fields[0].path).toEqual('/data');
          done();
        }).catch((error) => {
          fail(error);
        });
    });

  it('load mapping definition', (done) => {
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
        source.inspectionResult = JSON.stringify(atlasmapInspectionOldActionSourceJson);
        cfg.addDocument(source);
        const target = new DocumentInitializationModel();
        target.isSource = false;
        target.type = DocumentType.JSON;
        target.inspectionType = InspectionType.SCHEMA;
        target.id = 'old-action-target';
        target.inspectionResult = JSON.stringify(atlasmapInspectionOldActionTargetJson);
        cfg.addDocument(target);
        cfg.preloadedMappingJson = JSON.stringify(atlasmappingOldActionJson);

        spyOn(cfg.mappingService, 'runtimeServiceActive').and.returnValue(Promise.resolve(true));
        spyOn(cfg.fileService, 'getCurrentMappingCatalog').and.returnValue(of(null));
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
        return service.initialize();
    });

});
