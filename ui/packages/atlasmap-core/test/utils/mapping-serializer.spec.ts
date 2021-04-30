/* tslint:disable:no-unused-variable */

import ky from 'ky/umd';
import log from 'loglevel';

import { MappingSerializer } from '../../src/utils/mapping-serializer';
import { ConfigModel } from '../../src/models/config.model';
import { ErrorHandlerService } from '../../src/services/error-handler.service';
import { DocumentType } from '../../src/common/config.types';
import { DocumentDefinition } from '../../src/models/document-definition.model';
import { Field } from '../../src/models/field.model';
import { FieldActionService } from '../../src/services/field-action.service';
import { MappingUtil } from '../../src/utils/mapping-util';
import { MappingModel } from '../../src/models/mapping.model';
import { TransitionMode } from '../../src/models/transition.model';
import { FieldAction, Multiplicity } from '../../src/models/field-action.model';
import { ExpressionModel } from '../../src/models/expression.model';
import { MappingDefinition } from '../../src/models/mapping-definition.model';

import atlasmapFieldActionJson from '../../../../test-resources/fieldActions/atlasmap-field-action.json';
import atlasMappingTestJson from '../../../../test-resources/mapping/atlasmapping-test.json';
import atlasMappingExprPropJson from '../../../../test-resources/mapping/atlasmapping-expr-prop.json';
import atlasMappingCollExprPreview from '../../../../test-resources/mapping/atlasmapping-coll-expr-preview.json';
import atlasMappingCollExprMapping from '../../../../test-resources/mapping/atlasmapping-coll-expr-mapping.json';
import atlasMappingCollRefExprPreview from '../../../../test-resources/mapping/atlasmapping-coll-ref-expr-preview.json';
import atlasMappingCollRefExprMapping from '../../../../test-resources/mapping/atlasmapping-coll-ref-expr-mapping.json';

describe('MappingSerializer', () => {
  let cfg: ConfigModel;
  beforeEach(() => {
    cfg = ConfigModel.getConfig();
    cfg.clearDocs();
    cfg.mappings = new MappingDefinition();
    cfg.errorService = new ErrorHandlerService();
    const api = ky.create({ headers: { 'ATLASMAP-XSRF-TOKEN': 'awesome' } });
    cfg.fieldActionService = new FieldActionService(api);
    cfg.fieldActionService.cfg = cfg;
    cfg.logger = log.getLogger('config');

    // Source Java doc
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

    // Source JSON doc
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

    // Source JSON doc - collection
    const jsonSchemaSource = new DocumentDefinition();
    jsonSchemaSource.type = DocumentType.JSON;
    jsonSchemaSource.name = 'JSONSchemaSource';
    jsonSchemaSource.isSource = true;
    jsonSchemaSource.id = 'JSONSchemaSource';
    jsonSchemaSource.uri = 'atlas:json:JSONSchemaSource';
    const jc0 = new Field();
    jc0.name = 'addressList';
    jc0.path = '/addressList<>';
    jc0.type = 'COMPLEX';
    jc0.isCollection = true;
    jc0.serviceObject.status = 'SUPPORTED';
    jsonSchemaSource.addField(jc0);
    const jcc0 = new Field();
    jcc0.name = 'city';
    jcc0.path = '/addressList<>/city';
    jcc0.type = 'STRING';
    jcc0.isPrimitive = true;
    jcc0.parentField = jc0;
    jc0.children.push(jcc0);
    jsonSchemaSource.addField(jcc0);
    const jcc1 = new Field();
    jcc1.name = 'state';
    jcc1.path = '/addressList<>/state';
    jcc1.type = 'STRING';
    jcc1.isPrimitive = true;
    jcc1.parentField = jc0;
    jc0.children.push(jcc1);
    jsonSchemaSource.addField(jcc1);
    const jcc2 = new Field();
    jcc2.name = 'street';
    jcc2.path = '/addressList<>/street';
    jcc2.type = 'STRING';
    jcc2.isPrimitive = true;
    jcc2.parentField = jc0;
    jc0.children.push(jcc2);
    jsonSchemaSource.addField(jcc2);
    const jcc3 = new Field();
    jcc3.name = 'zip';
    jcc3.path = '/addressList<>/zip';
    jcc3.type = 'STRING';
    jcc3.isPrimitive = true;
    jcc3.parentField = jc0;
    jc0.children.push(jcc3);
    jsonSchemaSource.addField(jcc3);
    jsonSchemaSource.initializeFromFields();
    cfg.sourceDocs.push(jsonSchemaSource);

    // Source XML doc
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

    // Target Java doc
    const contact = new DocumentDefinition();
    contact.type = DocumentType.JAVA;
    contact.name = 'salesforce.Contact';
    contact.isSource = false;
    contact.id = 'salesforce.Contact';
    contact.uri =
      'atlas:java?className=org.apache.camel.salesforce.dto.Contact';
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

    // Target JSON doc
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

    // Target JSON doc - collection
    const jsonSchemaSource2 = new DocumentDefinition();
    jsonSchemaSource2.type = DocumentType.JSON;
    jsonSchemaSource2.name = 'JSONSchemaSource';
    jsonSchemaSource2.isSource = false;
    jsonSchemaSource2.id = 'JSONSchemaSource';
    jsonSchemaSource2.uri = 'atlas:json:JSONSchemaSource';
    const jtc0 = new Field();
    jtc0.name = 'addressList';
    jtc0.path = '/addressList<>';
    jtc0.type = 'COMPLEX';
    jtc0.isCollection = true;
    jtc0.serviceObject.status = 'SUPPORTED';
    jsonSchemaSource2.addField(jtc0);
    const jtcc0 = new Field();
    jtcc0.name = 'city';
    jtcc0.path = '/addressList<>/zip';
    jtcc0.type = 'STRING';
    jtcc0.isPrimitive = true;
    jtcc0.parentField = jtc0;
    jtc0.children.push(jtcc0);
    jsonSchemaSource2.addField(jtcc0);
    jsonSchemaSource2.initializeFromFields();
    cfg.targetDocs.push(jsonSchemaSource2);

    // Target XML doc
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

  test('deserialize & serialize mapping definition', (done) => {
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        const mappingJson = atlasMappingTestJson;
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);
        expect(cfg?.mappings?.mappings?.length).toEqual(
          Object.keys(mappingJson?.AtlasMapping?.mappings?.mapping).length
        );

        const serialized = MappingSerializer.serializeMappings(cfg);
        //console.log(JSON.stringify(serialized, null, 2));
        expect(
          Object.keys(serialized.AtlasMapping?.mappings?.mapping).length
        ).toEqual(cfg?.mappings?.mappings?.length);
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('deserialize & serialize conditional expression mapping definitions', (done) => {
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        cfg.mappings = new MappingDefinition();
        let fieldMapping = null;
        const mappingJson = atlasMappingExprPropJson;
        let expressionIndex = 0;

        // Find the expression input field group from the raw JSON.
        for (fieldMapping of mappingJson.AtlasMapping.mappings.mapping) {
          if (fieldMapping.inputFieldGroup) {
            const firstAction = fieldMapping?.inputFieldGroup?.actions[0];
            if (firstAction) {
              if (firstAction['@type'] === 'Expression') {
                break;
              }
            }
          }
          expressionIndex++;
        }
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);
        expect(cfg.mappings.mappings.length).toEqual(
          Object.keys(mappingJson.AtlasMapping?.mappings?.mapping).length
        );

        const mapping = cfg.mappings?.mappings[expressionIndex];
        expect(mapping).toBeDefined();
        const mfields = mapping.getMappedFields(true);
        let i = 0;
        if (!fieldMapping?.inputFieldGroup?.field) {
          fail();
        }
        for (const field of fieldMapping?.inputFieldGroup?.field) {
          // Constants have only a path - no name.
          if (!field.name) {
            expect(mfields[i].parsedData?.parsedPath).toEqual(field.path);
          } else {
            expect(mfields[i].parsedData?.parsedName).toEqual(field.name);
          }
          i++;
        }
        expect(fieldMapping.inputFieldGroup.field[0].docId).toContain(
          'DOC.Properties'
        );

        const serialized = MappingSerializer.serializeMappings(cfg);
        //console.log(JSON.stringify(serialized, null, 2));
        expect(
          Object.keys(serialized.AtlasMapping?.mappings?.mapping).length
        ).toEqual(cfg.mappings?.mappings?.length);
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('serialize many-to-one action', (done) => {
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        const mapping = new MappingModel();
        mapping.transition.mode = TransitionMode.MANY_TO_ONE;
        const actionDef = cfg.fieldActionService.getActionDefinitionForName(
          'Concatenate',
          Multiplicity.MANY_TO_ONE
        );
        if (!actionDef) {
          fail('Concatenate action defiition not found');
        }
        mapping.transition.transitionFieldAction = FieldAction.create(
          actionDef
        );
        const f = new Field();
        f.path = '/Text';
        const docDef = cfg.getDocForIdentifier('twitter4j.Status', true);
        if (!docDef) {
          fail('twitter4j.Status document not found');
        }
        f.docDef = docDef;
        mapping.addField(f, true);
        const json = MappingSerializer.serializeFieldMapping(
          cfg,
          mapping,
          'm1',
          true
        );
        expect(json.inputField).toBeFalsy();
        expect(json.inputFieldGroup.field.length).toEqual(1);
        expect(json.inputFieldGroup.field[0].actions).toBeFalsy();
        expect(json.inputFieldGroup.actions.length).toEqual(1);
        expect(json.inputFieldGroup.actions[0]['@type']).toEqual('Concatenate');
        const f2 = new Field();
        f2.path = '/User/Name';
        f2.docDef = f.docDef;
        mapping.addField(f, true);
        const json2 = MappingSerializer.serializeFieldMapping(
          cfg,
          mapping,
          'm1',
          true
        );
        expect(json2.inputField).toBeFalsy();
        expect(json2.inputFieldGroup.field.length).toEqual(2);
        expect(json2.inputFieldGroup.field[0].actions).toBeFalsy();
        expect(json2.inputFieldGroup.actions.length).toEqual(1);
        expect(json2.inputFieldGroup.actions[0]['@type']).toEqual(
          'Concatenate'
        );
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('serialize one-to-many action', (done) => {
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        const mapping = new MappingModel();
        mapping.transition.mode = TransitionMode.ONE_TO_MANY;
        const actionDef = cfg.fieldActionService.getActionDefinitionForName(
          'Repeat',
          Multiplicity.ONE_TO_MANY
        );
        if (!actionDef) {
          fail('Repeat action definition not found');
        }
        mapping.transition.transitionFieldAction = FieldAction.create(
          actionDef
        );
        const f = new Field();
        f.path = '/Text';
        const docDef = cfg.getDocForIdentifier('twitter4j.Status', true);
        if (!docDef) {
          fail('twitter4j.Status document definition not found');
        }
        f.docDef = docDef;
        mapping.addField(f, true);

        const json = MappingSerializer.serializeFieldMapping(
          cfg,
          mapping,
          'm1',
          true
        );
        expect(json.inputField[0].actions.length).toEqual(1);
        expect(json.inputField[0].actions[0]['@type']).toEqual('Repeat');
        done();
      })
      .catch((error) => {
        fail(error);
        done();
      });
  });

  test('serialize expression action', (done) => {
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        const mapping = new MappingModel();
        mapping.transition.mode = TransitionMode.ONE_TO_ONE;
        mapping.transition.enableExpression = true;
        mapping.transition.expression = new ExpressionModel(mapping, cfg);
        mapping.transition.expression.insertText('{0}');
        const f = new Field();
        f.path = '/Text';
        const docDef = cfg.getDocForIdentifier('twitter4j.Status', true);
        if (!docDef) {
          fail('twitter4j.Status document definition not found');
        }
        f.docDef = docDef;
        mapping.addField(f, true);
        const json = MappingSerializer.serializeFieldMapping(
          cfg,
          mapping,
          'm1',
          true
        );
        expect(json.inputField[0].actions).toBeUndefined();
        expect(json.expression).toBeDefined();
        expect(json.expression).toEqual('{0}');
        const f2 = new Field();
        f2.path = '/User/Name';
        f2.docDef = f.docDef;
        mapping.addField(f2, true);
        mapping.transition.mode = TransitionMode.MANY_TO_ONE;
        mapping.transition.expression = new ExpressionModel(mapping, cfg);
        mapping.transition.expression.insertText('{0} + {1}');
        const json2 = MappingSerializer.serializeFieldMapping(
          cfg,
          mapping,
          'm1',
          true
        );
        expect(json2.inputField).toBeFalsy();
        expect(json2.inputFieldGroup.field.length).toEqual(2);
        expect(json2.inputFieldGroup.field[0].actions).toBeFalsy();
        expect(json2.expression).toBeDefined();
        expect(json2.expression).toEqual('{0} + {1}');
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('serialize/ preview collection expression: repeat(count(city), const-str)', (done) => {
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        let fieldMapping = null;
        const mappingJson = atlasMappingExprPropJson;
        let expressionIndex = 0;
        let expressionStr = '';

        // Find the expression mapping repeat( count(city), const-str)
        for (fieldMapping of mappingJson.AtlasMapping.mappings.mapping) {
          if (fieldMapping.expression?.includes('repeat( count(')) {
            expressionStr = fieldMapping.expression.replace(
              /DOC.Constants/g,
              'DOC.Constants.'
            );
            break;
          }
          expressionIndex++;
        }
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);

        expect(cfg.mappings?.mappings.length).toEqual(
          Object.keys(mappingJson.AtlasMapping?.mappings?.mapping).length
        );

        const mapping = cfg.mappings?.mappings[expressionIndex];
        expect(mapping).toBeDefined();
        if (!mapping || !mapping.transition.enableExpression) {
          fail();
        }
        expect(
          mapping.transition.expression.toText().replace(/\.[0-9]*/g, '.')
        ).toEqual(expressionStr);

        const mfields = mapping.getMappedFields(true);
        let i = 0;
        if (!fieldMapping?.inputFieldGroup?.field) {
          fail();
        }
        let cityField: Field | null = null;
        for (const field of fieldMapping?.inputFieldGroup?.field) {
          if (field.name) {
            expect(mfields[i].parsedData?.parsedName).toEqual(field.name);
          }
          expect(mfields[i].parsedData?.parsedPath).toEqual(field.path);
          if (field.name === 'city') {
            cityField = mfields[i].field;
          }
          i++;
        }
        if (cityField === null) {
          fail("Unable to locate conditional expression field 'city'");
        }
        const serialized = MappingSerializer.serializeMappings(cfg);
        expect(
          Object.keys(serialized.AtlasMapping?.mappings?.mapping).length
        ).toEqual(cfg.mappings?.mappings?.length);

        // Verify preview mode.
        cityField.value = 'somestring';
        let sanitizedRequest = JSON.stringify(
          MappingSerializer.serializeMappings(cfg, false)
        ).replace(/\.[0-9]*/g, '.');
        expect(sanitizedRequest).toEqual(atlasMappingCollExprPreview);

        // Verify mapping.
        sanitizedRequest = JSON.stringify(
          MappingSerializer.serializeMappings(cfg) // ignoreValue defaults to true
        ).replace(/\.[0-9]*/g, '.');
        expect(sanitizedRequest).toEqual(atlasMappingCollExprMapping);
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('serialize/ preview reference collection expression', (done) => {
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        let fieldMapping = null;
        const mappingJson = atlasMappingExprPropJson;
        let expressionIndex = 0;
        let expressionStr = '';

        // Find the select/ filter expression mapping.
        for (fieldMapping of mappingJson.AtlasMapping.mappings.mapping) {
          if (fieldMapping.expression?.includes('select( filter(')) {
            expressionStr = fieldMapping.expression;
            break;
          }
          expressionIndex++;
        }
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);

        expect(cfg.mappings?.mappings.length).toEqual(
          Object.keys(mappingJson.AtlasMapping?.mappings?.mapping).length
        );
        const mapping = cfg.mappings?.mappings[expressionIndex];
        expect(mapping).toBeDefined();
        if (!mapping || !mapping.transition.enableExpression) {
          fail();
        }
        expect(
          mapping.transition.expression.toText().replace(/\.[0-9]* /g, '.')
        ).toEqual(expressionStr);

        const mfields = mapping.getMappedFields(true);
        let i = 0;
        if (!fieldMapping?.inputFieldGroup?.field) {
          fail();
        }

        let streetField: Field | null = null;
        for (const field of fieldMapping?.inputFieldGroup?.field) {
          if (field.name) {
            expect(mfields[i].parsedData?.parsedName).toEqual(field.name);
          }
          expect(mfields[i].parsedData?.parsedPath).toEqual(field.path);
          if (field.name === 'street') {
            streetField = mfields[i].field;
          }
          i++;
        }
        expect(streetField).toBeDefined();
        if (!streetField) {
          fail();
        }

        const serialized = MappingSerializer.serializeMappings(cfg);
        expect(
          Object.keys(serialized.AtlasMapping?.mappings?.mapping).length
        ).toEqual(cfg.mappings?.mappings?.length);

        // Verify preview mode.
        streetField.value = 'Bosto';
        let sanitizedRequest = JSON.stringify(
          MappingSerializer.serializeMappings(cfg, false)
        ).replace(/\.[0-9]*/g, '.');
        expect(sanitizedRequest).toEqual(atlasMappingCollRefExprPreview);

        // Verify mapping.
        sanitizedRequest = JSON.stringify(
          MappingSerializer.serializeMappings(cfg) // ignoreValue defaults to true
        ).replace(/\.[0-9]*/g, '.');
        expect(sanitizedRequest).toEqual(atlasMappingCollRefExprMapping);

        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('collection many-to-one deserialize/serialize', (done) => {
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        let mapping = null;
        const mappingJson = atlasMappingExprPropJson;
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);

        expect(cfg.mappings?.mappings).toBeDefined();
        if (!cfg.mappings || !cfg.mappings.mappings) {
          fail();
        }
        for (mapping of cfg.mappings.mappings) {
          if (mapping.transition?.mode === TransitionMode.MANY_TO_ONE) {
            break;
          }
        }
        expect(mapping).toBeDefined();

        const sanitizedRequest = JSON.stringify(
          MappingSerializer.serializeMappings(cfg)
        ).replace(/\.[0-9]*/g, '.');
        expect(sanitizedRequest).toEqual(atlasMappingCollRefExprMapping);

        done();
      })
      .catch((error) => {
        fail(error);
      });
  });
});
