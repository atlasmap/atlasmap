/* tslint:disable:no-unused-variable */

import { TestBed, inject } from '@angular/core/testing';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';
import { MappingSerializer } from './mapping-serializer';
import { ConfigModel } from '../models/config.model';
import { ErrorHandlerService } from '../services/error-handler.service';
import { DocumentType } from '../common/config.types';
import { DocumentDefinition } from '../models/document-definition.model';
import { Field } from '../models/field.model';
import { LoggerModule, NgxLoggerLevel, NGXLogger } from 'ngx-logger';
import { MappingManagementService } from '../services/mapping-management.service';
import { FieldActionService } from '../services/field-action.service';
import { MappingUtil } from './mapping-util';
import { MappingModel } from '../models/mapping.model';
import { TransitionMode } from '../models/transition.model';
import { FieldAction, Multiplicity } from '../models/field-action.model';
import { ExpressionModel } from '../models/expression.model';

describe('MappingSerializer', () => {
  let cfg: ConfigModel;
  beforeEach(() => {
    TestBed.resetTestEnvironment();
    TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
    TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot({ level: NgxLoggerLevel.DEBUG })],
      providers: [
        ErrorHandlerService,
        FieldActionService,
        MappingManagementService,
        NGXLogger,
      ],
    });
    cfg = ConfigModel.getConfig();
    cfg.errorService = TestBed.get(ErrorHandlerService);
    cfg.fieldActionService = TestBed.get(FieldActionService);
    cfg.fieldActionService.cfg = cfg;
    cfg.logger = TestBed.get(NGXLogger);

    const twitter = new DocumentDefinition();
    twitter.type = DocumentType.JAVA;
    twitter.name = 'twitter4j.Status';
    twitter.isSource = true;
    twitter.id = 'twitter4j.Status';
    twitter.uri = 'atlas:java?className=twitter4j.Status';
    const user = new Field();
    user.name = 'User';
    user.path = '/User';
    user.type = 'COMPLEX';
    twitter.addField(user);
    const userName = new Field();
    userName.name = 'Name';
    userName.path = '/User/Name';
    userName.type = 'STRING';
    userName.parentField = user;
    twitter.addField(userName);
    const userScreenName = new Field();
    userScreenName.name = 'ScreenName';
    userScreenName.path = '/User/ScreenName';
    userScreenName.type = 'STRING';
    userScreenName.parentField = user;
    twitter.addField(userScreenName);
    const text = new Field();
    text.name = 'Text';
    text.path = '/Text';
    text.type = 'STRING';
    twitter.addField(text);
    twitter.initializeFromFields();
    cfg.sourceDocs.push(twitter);
    const jsonSource = new DocumentDefinition();
    jsonSource.type = DocumentType.JSON;
    jsonSource.name = 'SomeJsonSource';
    jsonSource.isSource = true;
    jsonSource.id = 'SomeJsonSource';
    jsonSource.uri = 'atlas:json:SomeJsonSource';
    const js0 = new Field();
    js0.name = 'js0';
    js0.path = '/js0';
    js0.type = 'STRING';
    jsonSource.addField(js0);
    const js1 = new Field();
    js1.name = 'js1';
    js1.path = '/js1';
    js1.type = 'STRING';
    jsonSource.addField(js1);
    jsonSource.initializeFromFields();
    cfg.sourceDocs.push(jsonSource);
    const xmlSource = new DocumentDefinition();
    xmlSource.type = DocumentType.XML;
    xmlSource.name = 'SomeXmlSource';
    xmlSource.isSource = true;
    xmlSource.id = 'SomeXmlSource';
    xmlSource.uri = 'atlas:xml:SomeXmlSource';
    const xs0 = new Field();
    xs0.name = 'xs0';
    xs0.path = '/xs0';
    xs0.type = 'STRING';
    xmlSource.addField(xs0);
    const xs1 = new Field();
    xs1.name = 'xs1';
    xs1.path = '/xs1';
    xs1.type = 'STRING';
    xmlSource.addField(xs1);
    xmlSource.initializeFromFields();
    cfg.sourceDocs.push(xmlSource);
    const contact = new DocumentDefinition();
    contact.type = DocumentType.JAVA;
    contact.name = 'salesforce.Contact';
    contact.isSource = false;
    contact.id = 'salesforce.Contact';
    contact.uri = 'atlas:java?className=org.apache.camel.salesforce.dto.Contact';
    const desc = new Field();
    desc.name = 'Description';
    desc.path = '/Description';
    desc.type = 'STRING';
    contact.addField(desc);
    const title = new Field();
    title.name = 'Title';
    title.path = '/Title';
    title.type = 'STRING';
    contact.addField(title);
    const firstName = new Field();
    firstName.name = 'FirstName';
    firstName.path = '/FirstName';
    firstName.type = 'STRING';
    contact.addField(firstName);
    const lastName = new Field();
    lastName.name = 'LastName';
    lastName.path = '/LastName';
    lastName.type = 'STRING';
    contact.addField(lastName);
    contact.initializeFromFields();
    cfg.targetDocs.push(contact);
    const jsonTarget = new DocumentDefinition();
    jsonTarget.type = DocumentType.JSON;
    jsonTarget.name = 'SomeJsonTarget';
    jsonTarget.isSource = false;
    jsonTarget.id = 'SomeJsonTarget';
    jsonTarget.uri = 'atlas:json:SomeJsonTarget';
    const jt0 = new Field();
    jt0.name = 'jt0';
    jt0.path = '/jt0';
    jt0.type = 'STRING';
    jsonTarget.addField(jt0);
    const jt1 = new Field();
    jt1.name = 'jt1';
    jt1.path = '/jt1';
    jt1.type = 'STRING';
    jsonTarget.addField(jt1);
    jsonTarget.initializeFromFields();
    cfg.targetDocs.push(jsonTarget);
    const xmlTarget = new DocumentDefinition();
    xmlTarget.type = DocumentType.XML;
    xmlTarget.name = 'SomeXmlTarget';
    xmlTarget.isSource = true;
    xmlTarget.id = 'SomeXmlTarget';
    xmlTarget.uri = 'atlas:xml:SomeXmlTarget';
    const xt0 = new Field();
    xt0.name = 'xt0';
    xt0.path = '/xt0';
    xt0.type = 'STRING';
    xmlTarget.addField(xt0);
    const xt1 = new Field();
    xt1.name = 'xt1';
    xt1.path = '/xt1';
    xt1.type = 'STRING';
    xmlTarget.addField(xt1);
    const xt2 = new Field();
    xt2.name = 'xt2';
    xt2.path = '/xt2';
    xt2.type = 'STRING';
    xmlTarget.addField(xt2);
    xmlTarget.initializeFromFields();
    cfg.targetDocs.push(xmlTarget);
  });

  it('should deserialize & serialize mapping definition', (done) => {
    inject([], () => {
      jasmine.getFixtures().fixturesPath = 'base/test-resources/fieldActions';
      cfg.preloadedFieldActionMetadata = JSON.parse(jasmine.getFixtures().read('atlasmap-field-action.json'));
      return cfg.fieldActionService.fetchFieldActions().then(() => {
        jasmine.getFixtures().fixturesPath = 'base/test-resources/mapping';
        const mappingJson = JSON.parse(jasmine.getFixtures().read('atlasmapping-test.json'));
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);
        expect(cfg.mappings.mappings.length).toEqual(Object.keys(mappingJson.AtlasMapping.mappings.mapping).length);

        const serialized = MappingSerializer.serializeMappings(cfg);
        console.log(JSON.stringify(serialized, null, 2));
        expect(Object.keys(serialized.AtlasMapping.mappings.mapping).length).toEqual(cfg.mappings.mappings.length);
        done();
      }).catch((error) => {
        fail(error);
        done();
      });
    })();
  });

  it('should deserialize & serialize conditional expression mapping definitions', (done) => {
      inject([], () => {
      jasmine.getFixtures().fixturesPath = 'base/test-resources/fieldActions';
      cfg.preloadedFieldActionMetadata = JSON.parse(jasmine.getFixtures().read('atlasmap-field-action.json'));
      return cfg.fieldActionService.fetchFieldActions().then(() => {
        cfg.mappings = null;
        let fieldMapping = null;
        jasmine.getFixtures().fixturesPath = 'base/test-resources/mapping';
        const mappingJson = JSON.parse(jasmine.getFixtures().read('atlasmapping-expr-prop.json'));
        let expressionIndex = 0;

        // Find the expression input field group from the raw JSON.
        for (fieldMapping of mappingJson.AtlasMapping.mappings.mapping) {

          if (fieldMapping.inputFieldGroup) {
            const firstAction = fieldMapping.inputFieldGroup.actions[0];
            if (firstAction) {
              if (firstAction.Expression || firstAction['@type'] === 'Expression') {
                break;
              }
            }
          }
          expressionIndex++;
        }

        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);
        expect(cfg.mappings.mappings.length).toEqual(Object.keys(mappingJson.AtlasMapping.mappings.mapping).length);

        const mapping = cfg.mappings.mappings[expressionIndex];
        expect(mapping).toBeDefined();

        const mfields = mapping.getMappedFields(true);
        let i = 0;
        for (const field of fieldMapping.inputFieldGroup.field) {

          // Constants have only a path - no name.
          if (!field.name) {
            expect(mfields[i].parsedData.parsedPath).toEqual(field.path);
          } else {
            expect(mfields[i].parsedData.parsedName).toEqual(field.name);
          }
          i++;
        }
        expect(fieldMapping.inputFieldGroup.field[0].docId).toContain('DOC.Properties');

        const serialized = MappingSerializer.serializeMappings(cfg);
        console.log(JSON.stringify(serialized, null, 2));
        expect(Object.keys(serialized.AtlasMapping.mappings.mapping).length).toEqual(cfg.mappings.mappings.length);
        done();
      }).catch((error) => {
        fail(error);
        done();
      });
    })();
  });

  it('should serialize many-to-one action', (done) => {
    inject([], () => {
      return cfg.fieldActionService.fetchFieldActions().then(() => {
        const mapping = new MappingModel();
        mapping.transition.mode = TransitionMode.MANY_TO_ONE;
        mapping.transition.transitionFieldAction =
          FieldAction.create(cfg.fieldActionService.getActionDefinitionForName('Concatenate', Multiplicity.MANY_TO_ONE));
        const f = new Field();
        f.path = '/Text';
        f.docDef = cfg.getDocForIdentifier('twitter4j.Status', true);
        mapping.addField(f, true);
        const json = MappingSerializer.serializeFieldMapping(cfg, mapping, 'm1', true);
        expect(json.inputField).toBeFalsy();
        expect(json.inputFieldGroup.field.length).toEqual(1);
        expect(json.inputFieldGroup.field[0].actions).toBeFalsy();
        expect(json.inputFieldGroup.actions.length).toEqual(1);
        expect(json.inputFieldGroup.actions[0]['@type']).toEqual('Concatenate');
        const f2 = new Field();
        f2.path = '/User/Name';
        f2.docDef = f.docDef;
        mapping.addField(f, true);
        const json2 = MappingSerializer.serializeFieldMapping(cfg, mapping, 'm1', true);
        expect(json2.inputField).toBeFalsy();
        expect(json2.inputFieldGroup.field.length).toEqual(2);
        expect(json2.inputFieldGroup.field[0].actions).toBeFalsy();
        expect(json2.inputFieldGroup.actions.length).toEqual(1);
        expect(json2.inputFieldGroup.actions[0]['@type']).toEqual('Concatenate');
        done();
      }).catch((error) => {
        fail(error);
        done();
      });
    })();
  });

  it('should serialize one-to-many action', (done) => {
    inject([], () => {
      return cfg.fieldActionService.fetchFieldActions().then(() => {
        const mapping = new MappingModel();
        mapping.transition.mode = TransitionMode.ONE_TO_MANY;
        mapping.transition.transitionFieldAction =
          FieldAction.create(cfg.fieldActionService.getActionDefinitionForName('Split', Multiplicity.ONE_TO_MANY));
          const f = new Field();
          f.path = '/Text';
          f.docDef = cfg.getDocForIdentifier('twitter4j.Status', true);
          mapping.addField(f, true);
          const json = MappingSerializer.serializeFieldMapping(cfg, mapping, 'm1', true);
          expect(json.inputField[0].actions.length).toEqual(1);
          expect(json.inputField[0].actions[0]['@type']).toEqual('Split');
          done();
      }).catch((error) => {
        fail(error);
        done();
      });
    })();
  });

  it('should serialize expression action', (done) => {
    inject([], () => {
      return cfg.fieldActionService.fetchFieldActions().then(() => {
        const mapping = new MappingModel();
        mapping.transition.mode = TransitionMode.ONE_TO_ONE;
        mapping.transition.enableExpression = true;
        mapping.transition.expression = new ExpressionModel(mapping, cfg);
        mapping.transition.expression.insertText('{0}');
        const f = new Field();
        f.path = '/Text';
        f.docDef = cfg.getDocForIdentifier('twitter4j.Status', true);
        mapping.addField(f, true);
        const json = MappingSerializer.serializeFieldMapping(cfg, mapping, 'm1', true);
        expect(json.inputField[0].actions.length).toEqual(1);
        expect(json.inputField[0].actions[0]['@type']).toEqual('Expression');
        expect(json.inputField[0].actions[0].expression).toEqual('{0}');
        const f2 = new Field();
        f2.path = '/User/Name';
        f2.docDef = f.docDef;
        mapping.addField(f2, true);
        mapping.transition.mode = TransitionMode.MANY_TO_ONE;
        mapping.transition.expression = new ExpressionModel(mapping, cfg);
        mapping.transition.expression.insertText('{0} + {1}');
        const json2 = MappingSerializer.serializeFieldMapping(cfg, mapping, 'm1', true);
        expect(json2.inputField).toBeFalsy();
        expect(json2.inputFieldGroup.field.length).toEqual(2);
        expect(json2.inputFieldGroup.field[0].actions).toBeFalsy();
        expect(json2.inputFieldGroup.actions.length).toEqual(1);
        expect(json2.inputFieldGroup.actions[0]['@type']).toEqual('Expression');
        expect(json2.inputFieldGroup.actions[0].expression).toEqual('{0} + {1}');
        done();
      }).catch((error) => {
        fail(error);
        done();
      });
    })();
  });

});
