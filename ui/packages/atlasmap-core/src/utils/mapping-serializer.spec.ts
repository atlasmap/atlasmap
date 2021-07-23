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
import {
  DocumentDefinition,
  NamespaceModel,
} from '../models/document-definition.model';
import {
  DocumentType,
  FieldStatus,
  FieldType,
  InspectionType,
} from '../contracts/common';
import { IAtlasMappingContainer, IMapping } from '../contracts/mapping';

import { CsvInspectionModel } from '../models/inspect/csv-inspection.model';
import { DocumentInspectionUtil } from './document-inspection-util';
import { ExpressionModel } from '../models/expression.model';
import { Field } from '../models/field.model';
import { FieldAction } from '../models/field-action.model';
import { InitializationService } from '../services';
import { LookupTable } from '../models/lookup-table.model';
import { LookupTableUtil } from './lookup-table-util';
import { MappingDefinition } from '../models/mapping-definition.model';
import { MappingModel } from '../models/mapping.model';
import { MappingSerializer } from '../utils/mapping-serializer';
import { MappingUtil } from '../utils/mapping-util';
import { Multiplicity } from '../contracts/field-action';
import { TestUtils } from '../../test/test-util';
import { TransitionMode } from '../models/transition.model';

import atlasMappingCSV from '../../../../test-resources/mapping/atlasmapping-csv.json';
import atlasMappingCollExprMapping from '../../../../test-resources/mapping/atlasmapping-coll-expr-mapping.json';
import atlasMappingCollExprPreview from '../../../../test-resources/mapping/atlasmapping-coll-expr-preview.json';
import atlasMappingCollRefExprPreview from '../../../../test-resources/mapping/atlasmapping-coll-ref-expr-preview.json';
import atlasMappingEnumLookupTableMapping from '../../../../test-resources/mapping/atlasmapping-enum-lookup-table.json';
import atlasMappingExprPropJson from '../../../../test-resources/mapping/atlasmapping-expr-prop.json';
import atlasMappingSplitCollapseJson from '../../../../test-resources/mapping/atlasmapping-split-collapse.json';
import atlasMappingSplitJson from '../../../../test-resources/mapping/atlasmapping-split.json';
import atlasMappingTestJson from '../../../../test-resources/mapping/atlasmapping-test.json';
import atlasmapFieldActionJson from '../../../../test-resources/fieldActions/atlasmap-field-action.json';

import ky from 'ky';

function createTwitter4jSourceDoc() {
  const twitter = new DocumentDefinition();
  twitter.type = DocumentType.JAVA;
  twitter.name = 'twitter4j.Status';
  twitter.isSource = true;
  twitter.id = 'twitter4j.Status';
  twitter.uri = 'atlas:java?className=twitter4j.Status';
  twitter.description = 'random desc';
  const user = new Field();
  user.name = 'User';
  user.path = '/User';
  user.type = FieldType.COMPLEX;
  twitter.addField(user);
  const userName = new Field();
  userName.name = 'Name';
  userName.path = '/User/Name';
  userName.type = FieldType.STRING;
  userName.parentField = user;
  twitter.addField(userName);
  const userScreenName = new Field();
  userScreenName.name = 'ScreenName';
  userScreenName.path = '/User/ScreenName';
  userScreenName.type = FieldType.STRING;
  userScreenName.parentField = user;
  twitter.addField(userScreenName);
  const text = new Field();
  text.name = 'Text';
  text.path = '/Text';
  text.type = FieldType.STRING;
  twitter.addField(text);
  twitter.initializeFromFields();
  return twitter;
}

function createSomeJsonSourceSourceDoc() {
  const jsonSource = new DocumentDefinition();
  jsonSource.type = DocumentType.JSON;
  jsonSource.name = 'SomeJsonSource';
  jsonSource.isSource = true;
  jsonSource.id = 'SomeJsonSource';
  jsonSource.uri = 'atlas:json:SomeJsonSource';
  jsonSource.description = 'random desc';
  const js0 = new Field();
  js0.name = 'js0';
  js0.docDef = jsonSource;
  js0.path = '/js0';
  js0.type = FieldType.STRING;
  jsonSource.addField(js0);
  const js1 = new Field();
  js1.name = 'js1';
  js1.docDef = jsonSource;
  js1.path = '/js1';
  js1.type = FieldType.STRING;
  jsonSource.addField(js1);
  jsonSource.initializeFromFields();
  return jsonSource;
}

function createPrimitivesField() {
  const primitives = new Field();
  primitives.name = 'primitives';
  primitives.path = '/primitives';
  primitives.type = FieldType.COMPLEX;
  primitives.documentField.status = FieldStatus.SUPPORTED;
  return primitives;
}

function createJSONSchemaSourceSourceDoc() {
  const jsonSchemaSource = new DocumentDefinition();
  jsonSchemaSource.type = DocumentType.JSON;
  jsonSchemaSource.name = 'JSONSchemaSource';
  jsonSchemaSource.isSource = true;
  jsonSchemaSource.id = 'JSONSchemaSource';
  jsonSchemaSource.uri = 'atlas:json:JSONSchemaSource';
  jsonSchemaSource.description = 'random desc';
  const jc0 = new Field();
  jc0.name = 'addressList';
  jc0.docDef = jsonSchemaSource;
  jc0.path = '/addressList<>';
  jc0.type = FieldType.COMPLEX;
  jc0.isCollection = true;
  jc0.documentField.status = FieldStatus.SUPPORTED;
  jsonSchemaSource.addField(jc0);
  const jcc0 = new Field();
  jcc0.name = 'city';
  jcc0.docDef = jsonSchemaSource;
  jcc0.path = '/addressList<>/city';
  jcc0.type = FieldType.STRING;
  jcc0.isPrimitive = true;
  jcc0.parentField = jc0;
  jc0.children.push(jcc0);
  jsonSchemaSource.addField(jcc0);
  const jcc1 = new Field();
  jcc1.name = 'state';
  jcc1.docDef = jsonSchemaSource;
  jcc1.path = '/addressList<>/state';
  jcc1.type = FieldType.STRING;
  jcc1.isPrimitive = true;
  jcc1.parentField = jc0;
  jc0.children.push(jcc1);
  jsonSchemaSource.addField(jcc1);
  const jcc2 = new Field();
  jcc2.name = 'street';
  jcc2.docDef = jsonSchemaSource;
  jcc2.path = '/addressList<>/street';
  jcc2.type = FieldType.STRING;
  jcc2.isPrimitive = true;
  jcc2.parentField = jc0;
  jc0.children.push(jcc2);
  jsonSchemaSource.addField(jcc2);
  const jcc3 = new Field();
  jcc3.name = 'zip';
  jcc3.docDef = jsonSchemaSource;
  jcc3.path = '/addressList<>/zip';
  jcc3.type = FieldType.STRING;
  jcc3.isPrimitive = true;
  jcc3.parentField = jc0;
  jc0.children.push(jcc3);
  jsonSchemaSource.addField(jcc3);
  const primitives = createPrimitivesField();
  jsonSchemaSource.addField(primitives);
  const stringPrimitive = new Field();
  stringPrimitive.name = 'stringPrimitive';
  stringPrimitive.docDef = jsonSchemaSource;
  stringPrimitive.path = '/primitives/stringPrimitive';
  stringPrimitive.type = FieldType.STRING;
  stringPrimitive.isPrimitive = true;
  stringPrimitive.parentField = primitives;
  primitives.children.push(stringPrimitive);
  jsonSchemaSource.addField(stringPrimitive);
  jsonSchemaSource.initializeFromFields();
  return jsonSchemaSource;
}

function createSomeXmlSourceSourceDoc() {
  const xmlSource = new DocumentDefinition();
  xmlSource.type = DocumentType.XML;
  xmlSource.name = 'SomeXmlSource';
  xmlSource.isSource = true;
  xmlSource.id = 'SomeXmlSource';
  xmlSource.uri = 'atlas:xml:SomeXmlSource';
  xmlSource.description = 'random desc';
  const xs0 = new Field();
  xs0.name = 'xs0';
  xs0.docDef = xmlSource;
  xs0.path = '/xs0';
  xs0.type = FieldType.STRING;
  xmlSource.addField(xs0);
  const xs1 = new Field();
  xs1.name = 'xs1';
  xs1.docDef = xmlSource;
  xs1.path = '/xs1';
  xs1.type = FieldType.STRING;
  xmlSource.addField(xs1);
  xmlSource.initializeFromFields();
  return xmlSource;
}

function createSalesforceContactTargetDoc() {
  const contact = new DocumentDefinition();
  contact.type = DocumentType.JAVA;
  contact.name = 'salesforce.Contact';
  contact.isSource = false;
  contact.id = 'salesforce.Contact';
  contact.uri = 'atlas:java?className=org.apache.camel.salesforce.dto.Contact';
  contact.description = 'random desc';
  const desc = new Field();
  desc.name = 'Description';
  desc.docDef = contact;
  desc.path = '/Description';
  desc.type = FieldType.STRING;
  contact.addField(desc);
  const title = new Field();
  title.name = 'Title';
  title.docDef = contact;
  title.path = '/Title';
  title.type = FieldType.STRING;
  contact.addField(title);
  const firstName = new Field();
  firstName.name = 'FirstName';
  firstName.docDef = contact;
  firstName.path = '/FirstName';
  firstName.type = FieldType.STRING;
  contact.addField(firstName);
  const lastName = new Field();
  lastName.name = 'LastName';
  lastName.docDef = contact;
  lastName.path = '/LastName';
  lastName.type = FieldType.STRING;
  contact.addField(lastName);
  contact.initializeFromFields();
  return contact;
}

function createSomeJsonTargetTargetDoc() {
  const jsonTarget = new DocumentDefinition();
  jsonTarget.type = DocumentType.JSON;
  jsonTarget.name = 'SomeJsonTarget';
  jsonTarget.isSource = false;
  jsonTarget.id = 'SomeJsonTarget';
  jsonTarget.uri = 'atlas:json:SomeJsonTarget';
  jsonTarget.description = 'random desc';
  const jt0 = new Field();
  jt0.name = 'jt0';
  jt0.docDef = jsonTarget;
  jt0.path = '/jt0';
  jt0.type = FieldType.STRING;
  jsonTarget.addField(jt0);
  const jt1 = new Field();
  jt1.name = 'jt1';
  jt1.docDef = jsonTarget;
  jt1.path = '/jt1';
  jt1.type = FieldType.STRING;
  jsonTarget.addField(jt1);
  jsonTarget.initializeFromFields();
  return jsonTarget;
}

function createJSONSchemaSourceTargetDoc() {
  const jsonSchemaSource = new DocumentDefinition();
  jsonSchemaSource.type = DocumentType.JSON;
  jsonSchemaSource.name = 'JSONSchemaSource';
  jsonSchemaSource.isSource = false;
  jsonSchemaSource.id = 'JSONSchemaSource';
  jsonSchemaSource.uri = 'atlas:json:JSONSchemaSource';
  jsonSchemaSource.description = 'random desc';
  const jtc0 = new Field();
  jtc0.name = 'addressList';
  jtc0.docDef = jsonSchemaSource;
  jtc0.path = '/addressList<>';
  jtc0.type = FieldType.COMPLEX;
  jtc0.isCollection = true;
  jtc0.documentField.status = FieldStatus.SUPPORTED;
  jsonSchemaSource.addField(jtc0);
  const jtcc0 = new Field();
  jtcc0.name = 'city';
  jtcc0.docDef = jsonSchemaSource;
  jtcc0.path = '/addressList<>/city';
  jtcc0.type = FieldType.STRING;
  jtcc0.isPrimitive = true;
  jtcc0.parentField = jtc0;
  jtc0.children.push(jtcc0);
  jsonSchemaSource.addField(jtcc0);
  const jtcc1 = new Field();
  jtcc1.name = 'zip';
  jtcc1.docDef = jsonSchemaSource;
  jtcc1.path = '/addressList<>/zip';
  jtcc1.type = FieldType.STRING;
  jtcc1.isPrimitive = true;
  jtcc1.parentField = jtc0;
  jtc0.children.push(jtcc1);
  jsonSchemaSource.addField(jtcc1);
  const tprimitives = new Field();
  tprimitives.name = 'primitives';
  tprimitives.docDef = jsonSchemaSource;
  tprimitives.path = '/primitives';
  tprimitives.type = FieldType.COMPLEX;
  tprimitives.documentField.status = FieldStatus.SUPPORTED;
  jsonSchemaSource.addField(tprimitives);
  const tstringPrimitive = new Field();
  tstringPrimitive.name = 'stringPrimitive';
  tstringPrimitive.docDef = jsonSchemaSource;
  tstringPrimitive.path = '/primitives/stringPrimitive';
  tstringPrimitive.type = FieldType.STRING;
  tstringPrimitive.isPrimitive = true;
  tstringPrimitive.parentField = createPrimitivesField();
  tprimitives.children.push(tstringPrimitive);
  jsonSchemaSource.addField(tstringPrimitive);
  jsonSchemaSource.initializeFromFields();
  return jsonSchemaSource;
}

function createXMLInstanceSourceTargetDoc() {
  const xmlTarget = new DocumentDefinition();
  xmlTarget.type = DocumentType.XML;
  xmlTarget.name = 'XMLInstanceSource';
  xmlTarget.isSource = false;
  xmlTarget.id = 'XMLInstanceSource';
  xmlTarget.uri = 'atlas:xml:XMLInstanceSource';
  xmlTarget.description = 'random desc';
  const ns: NamespaceModel = new NamespaceModel();
  ns.alias = 'xsi';
  ns.uri = 'http://www.w3.org/2001/XMLSchema-instance';
  ns.locationUri = 'http://www.w3.org/2001/XMLSchema-instance';
  ns.isTarget = true;
  xmlTarget.namespaces.push(ns);
  const xt0 = new Field();
  xt0.name = 'xt0';
  xt0.path = '/xt0';
  xt0.type = FieldType.STRING;
  xmlTarget.addField(xt0);
  const xt1 = new Field();
  xt1.name = 'xt1';
  xt1.path = '/xt1';
  xt1.type = FieldType.STRING;
  xmlTarget.addField(xt1);
  const xt2 = new Field();
  xt2.name = 'xt2';
  xt2.path = '/xt2';
  xt2.type = FieldType.STRING;
  xmlTarget.addField(xt2);
  xmlTarget.initializeFromFields();
  return xmlTarget;
}

function createAllDocs(cfg: ConfigModel) {
  // Source Java doc
  cfg.sourceDocs.push(createTwitter4jSourceDoc());
  // Source JSON doc
  cfg.sourceDocs.push(createSomeJsonSourceSourceDoc());
  // Source JSON doc - collection
  cfg.sourceDocs.push(createJSONSchemaSourceSourceDoc());
  // Source XML doc
  cfg.sourceDocs.push(createSomeXmlSourceSourceDoc());
  // Target Java doc
  cfg.targetDocs.push(createSalesforceContactTargetDoc());
  // Target JSON doc
  cfg.targetDocs.push(createSomeJsonTargetTargetDoc());
  // Target JSON doc - collection
  cfg.targetDocs.push(createJSONSchemaSourceTargetDoc());
  // Target XML Namespace doc
  cfg.targetDocs.push(createXMLInstanceSourceTargetDoc());
}

describe('MappingSerializer', () => {
  let cfg: ConfigModel;
  let tName = '';
  beforeEach(() => {
    const init = new InitializationService(ky);
    init.initialize();
    cfg = init.cfg;
    cfg.mappings = new MappingDefinition();
  });

  tName = 'deserialize & serialize mapping definition';
  test(tName, (done) => {
    createAllDocs(cfg);
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        const mappingJson = atlasMappingTestJson as IAtlasMappingContainer;
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);
        expect(cfg?.mappings?.mappings?.length).toEqual(
          Object.keys(
            mappingJson?.AtlasMapping?.mappings?.mapping as IMapping[]
          ).length
        );

        const serialized = MappingSerializer.serializeMappings(cfg);
        //console.log(JSON.stringify(serialized, null, 2));
        expect(
          Object.keys(serialized.AtlasMapping?.mappings?.mapping as IMapping[])
            .length
        ).toEqual(cfg?.mappings?.mappings?.length);
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  tName = 'deserialize & serialize conditional expression mapping definitions';
  test(tName, (done) => {
    createAllDocs(cfg);
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        cfg.mappings = new MappingDefinition();
        let fieldMapping = null;
        const mappingJson = atlasMappingExprPropJson as IAtlasMappingContainer;
        let expressionIndex = 0;

        // Find the expression input field group from the raw JSON.
        for (fieldMapping of mappingJson?.AtlasMapping?.mappings
          ?.mapping as IMapping[]) {
          if (
            fieldMapping.expression &&
            fieldMapping.expression.startsWith('if (')
          ) {
            break;
          }
          expressionIndex++;
        }
        // console.log(JSON.stringify(mappingJson, null, 2));
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);
        expect(cfg.mappings.mappings.length).toEqual(
          Object.keys(mappingJson.AtlasMapping?.mappings?.mapping as IMapping[])
            .length
        );

        const mapping = cfg.mappings?.mappings[expressionIndex];
        expect(mapping).toBeDefined();
        const mfields = mapping.getMappedFields(true);
        let i = 0;
        if (!mfields || !fieldMapping?.inputFieldGroup?.field) {
          fail();
        }
        for (const field of fieldMapping.inputFieldGroup.field) {
          expect(mfields[i]?.mappingField?.path).toEqual(field.path);
          i++;
        }
        expect(fieldMapping.inputFieldGroup.field[0].docId).toContain(
          'DOC.Properties'
        );

        const serialized = MappingSerializer.serializeMappings(cfg);
        //console.log(JSON.stringify(serialized, null, 2));
        expect(
          Object.keys(serialized.AtlasMapping?.mappings?.mapping as IMapping[])
            .length
        ).toEqual(cfg.mappings?.mappings?.length);
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  tName = 'serialize many-to-one action';
  test(tName, (done) => {
    createAllDocs(cfg);
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
        mapping.transition.transitionFieldAction =
          FieldAction.create(actionDef);
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
        expect(json.inputFieldGroup!.field!.length).toEqual(1);
        expect(json.inputFieldGroup!.field![0].actions).toBeFalsy();
        expect(json.inputFieldGroup!.actions!.length).toEqual(1);
        expect(json.inputFieldGroup!.actions![0]['@type']).toEqual(
          'Concatenate'
        );
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
        expect(json2.inputFieldGroup!.field!.length).toEqual(2);
        expect(json2.inputFieldGroup!.field![0].actions).toBeFalsy();
        expect(json2.inputFieldGroup!.actions!.length).toEqual(1);
        expect(json2.inputFieldGroup!.actions![0]['@type']).toEqual(
          'Concatenate'
        );
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  tName = 'serialize one-to-many action';
  test(tName, (done) => {
    createAllDocs(cfg);
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
        mapping.transition.transitionFieldAction =
          FieldAction.create(actionDef);
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
        expect(json.inputField![0].actions!.length).toEqual(1);
        expect(json.inputField![0].actions![0]['@type']).toEqual('Repeat');
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  tName = 'serialize expression action';
  test(tName, (done) => {
    createAllDocs(cfg);
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
        expect(json.inputField![0].actions).toBeUndefined();
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
        expect(json2.inputFieldGroup!.field!.length).toEqual(2);
        expect(json2.inputFieldGroup!.field![0].actions).toBeFalsy();
        expect(json2.expression).toBeDefined();
        expect(json2.expression).toEqual('{0} + {1}');
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  tName =
    'serialize/ preview collection expression: repeat(count(city), const-str)';
  test(tName, (done) => {
    createAllDocs(cfg);
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        let fieldMapping = null;
        const mappingJson = atlasMappingExprPropJson as IAtlasMappingContainer;
        let expressionIndex = 0;
        let expressionStr = '';

        // Find the expression mapping repeat( count(city), const-str)
        for (fieldMapping of mappingJson!.AtlasMapping!.mappings!
          .mapping as IMapping[]) {
          if (fieldMapping?.expression?.includes('repeat( count(')) {
            expressionStr = fieldMapping.expression;
            break;
          }
          expressionIndex++;
        }
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);
        expect(cfg.mappings?.mappings.length).toEqual(
          Object.keys(mappingJson.AtlasMapping?.mappings?.mapping as IMapping[])
            .length
        );

        const mapping = cfg.mappings?.mappings[expressionIndex];
        expect(mapping).toBeDefined();
        if (!mapping || !mapping.transition.enableExpression) {
          fail();
        }
        expect(mapping.transition.expression.toText()).toEqual(expressionStr);

        const mfields = mapping.getMappedFields(true);
        let i = 0;
        if (!fieldMapping?.inputFieldGroup?.field) {
          fail();
        }
        let testField: Field | null = null;
        for (const field of fieldMapping?.inputFieldGroup?.field) {
          if (field.path) {
            expect(mfields[i].mappingField?.path).toEqual(field.path);
          }
          if (field.path?.indexOf('street') !== -1) {
            testField = mfields[i].field;
          }
          i++;
        }
        if (testField === null) {
          fail('Unable to locate conditional expression field.');
        }
        const serialized = MappingSerializer.serializeMappings(cfg);
        expect(
          Object.keys(serialized.AtlasMapping?.mappings?.mapping as IMapping[])
            .length
        ).toEqual(cfg.mappings?.mappings?.length);

        // Verify mapping.
        expect(
          TestUtils.isEqualJSON(
            tName,
            atlasMappingCollExprMapping,
            serialized // ignoreValue defaults to true
          )
        ).toBe(true);

        // Verify preview mode.
        testField.value = 'somestring';
        // const sanitizedRequest = JSON.stringify(
        // MappingSerializer.serializeMappings(cfg, false)
        // ).replace(/\.[0-9]*/g, '.');
        expect(
          TestUtils.isEqualJSON(
            tName,
            atlasMappingCollExprPreview,
            MappingSerializer.serializeMappings(cfg) // ignoreValue defaults to true
          )
        ).toBe(true);

        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  tName = 'serialize/ preview reference collection expression';
  test(tName, (done) => {
    createAllDocs(cfg);
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        let fieldMapping = null;
        const mappingJson = atlasMappingExprPropJson as IAtlasMappingContainer;
        let expressionIndex = 0;
        let expressionStr = '';

        // Find the select/ filter expression mapping.
        for (fieldMapping of mappingJson?.AtlasMapping?.mappings
          ?.mapping as IMapping[]) {
          if (fieldMapping.expression?.includes('select( filter(')) {
            expressionStr = fieldMapping.expression;
            break;
          }
          expressionIndex++;
        }
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);

        expect(cfg.mappings?.mappings.length).toEqual(
          Object.keys(mappingJson.AtlasMapping?.mappings?.mapping as IMapping[])
            .length
        );
        const mapping = cfg.mappings?.mappings[expressionIndex];
        expect(mapping).toBeDefined();
        if (!mapping || !mapping.transition.enableExpression) {
          fail();
        }
        expect(mapping.transition.expression.toText()).toEqual(expressionStr);

        const mfields = mapping.getMappedFields(true);
        let i = 0;
        if (!fieldMapping?.inputFieldGroup?.field) {
          fail();
        }

        let testField: Field | null = null;
        for (const field of fieldMapping?.inputFieldGroup?.field) {
          if (
            field.jsonType === 'io.atlasmap.v2.FieldGroup' &&
            field.path === '/addressList<>'
          ) {
            testField = Field.getField(
              mfields[i].mappingField?.path!,
              cfg.getDocForIdentifier(mfields[i].mappingField!.docId!, true)
                ?.allFields!
            );
          }
          i++;
        }
        expect(testField).toBeDefined();

        const serialized = MappingSerializer.serializeMappings(cfg);
        expect(
          Object.keys(serialized.AtlasMapping?.mappings?.mapping as IMapping[])
            .length
        ).toEqual(cfg.mappings?.mappings?.length);

        // Verify preview mode.
        testField!.value = 'Bosto';
        expect(
          TestUtils.isEqualJSON(
            tName,
            atlasMappingCollRefExprPreview,
            MappingSerializer.serializeMappings(cfg, false)
          )
        ).toBe(true);

        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  tName = 'collection many-to-one deserialize/serialize';
  test(tName, (done) => {
    createAllDocs(cfg);
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        let mapping = null;
        const mappingJson = atlasMappingExprPropJson as IAtlasMappingContainer;
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
        expect(
          TestUtils.isEqualJSON(
            tName,
            atlasMappingCollExprMapping,
            MappingSerializer.serializeMappings(cfg) // ignoreValue defaults to true
          )
        ).toBe(true);

        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  tName = 'collection one-to-many deserialize/serialize';
  test(tName, (done) => {
    cfg.sourceDocs.push(createJSONSchemaSourceSourceDoc());
    cfg.targetDocs.push(createJSONSchemaSourceTargetDoc());
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        const mappingJson = atlasMappingSplitJson as IAtlasMappingContainer;
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);
        cfg.mappingService.updateMappingsTransition();

        expect(cfg.mappings?.mappings).toBeDefined();
        expect(cfg.mappings?.mappings.length).toBe(1);
        const mapping = cfg.mappings?.mappings[0];
        expect(mapping).toBeDefined();
        expect(
          TestUtils.isEqualJSON(
            tName,
            atlasMappingSplitJson,
            MappingSerializer.serializeMappings(cfg)
          )
        ).toBe(true);

        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  tName = 'collection one-to-many collapse deserialize/serialize';
  test(tName, (done) => {
    cfg.sourceDocs.push(createJSONSchemaSourceSourceDoc());
    cfg.targetDocs.push(createJSONSchemaSourceTargetDoc());
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        const mappingJson =
          atlasMappingSplitCollapseJson as IAtlasMappingContainer;
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);
        cfg.mappingService.updateMappingsTransition();

        expect(cfg.mappings?.mappings).toBeDefined();
        expect(cfg.mappings?.mappings.length).toBe(1);
        const mapping = cfg.mappings?.mappings[0];
        expect(mapping).toBeDefined();
        expect(
          TestUtils.isEqualJSON(
            tName,
            atlasMappingSplitCollapseJson,
            MappingSerializer.serializeMappings(cfg)
          )
        ).toBe(true);

        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  tName = 'remove a field node from a conditional expression';
  test(tName, (done) => {
    createAllDocs(cfg);
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        cfg.mappings = new MappingDefinition();
        let fieldMapping = null;
        const mappingJson = atlasMappingExprPropJson as IAtlasMappingContainer;
        let expressionIndex = 0;

        // Find the expression input field group from the raw JSON.
        for (fieldMapping of mappingJson?.AtlasMapping?.mappings
          ?.mapping as IMapping[]) {
          if (
            fieldMapping.expression &&
            fieldMapping.expression.startsWith('select( filter(')
          ) {
            break;
          }
          expressionIndex++;
        }
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);
        const mapping = cfg.mappings?.mappings[expressionIndex];
        expect(mapping).toBeDefined();

        let cityField = mapping?.transition.expression.nodes.find(
          (n) => n.toText() === `\${/city}`
        );
        expect(cityField).toBeDefined();

        // Remove the 'city' field from the expression.
        mapping?.transition.expression.removeToken(cityField?.getUuid());
        cityField = mapping?.transition.expression.nodes.find(
          (n) => n.toText() === `\${/city}`
        );
        expect(cityField).toBeUndefined();

        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  tName = 'map enumeration values through a lookup table';
  test(tName, (done) => {
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        cfg.mappings = new MappingDefinition();
        let fieldMapping = null;
        const mappingJson =
          atlasMappingEnumLookupTableMapping as IAtlasMappingContainer;

        // Find the enum lookup table mapping from the raw JSON.
        for (fieldMapping of mappingJson?.AtlasMapping?.mappings
          ?.mapping as IMapping[]) {
          if (
            fieldMapping?.lookupTableName &&
            fieldMapping.lookupTableName.length > 0
          ) {
            break;
          }
        }
        expect(fieldMapping).toBeDefined();

        // Isolate the mock documents using the document initialization model.
        const docInitSource = new DocumentInitializationModel();
        docInitSource.description =
          'Java document class io.atlasmap.java.test.TargetTestClass';
        docInitSource.id = 'io.atlasmap.java.test.TargetTestClass';
        docInitSource.isSource = true;
        docInitSource.name = 'io.atlasmap.java.test.TargetTestClass';
        docInitSource.type = DocumentType.JAVA;
        docInitSource.inspectionSource =
          'io.atlasmap.java.test.TargetTestClass';
        const docEnumSrc = cfg.addDocument(docInitSource);

        // Establish the enum fields.
        const ss = new Field();
        ss.name = 'statesShort';
        ss.path = '/statesShort';
        ss.scope = '';
        ss.type = FieldType.COMPLEX;
        ss.documentField.status = FieldStatus.SUPPORTED;
        docEnumSrc.addField(ss);

        const docInitTarget = new DocumentInitializationModel();
        docInitTarget.description =
          'Java document class io.atlasmap.java.test.TargetTestClass';
        docInitTarget.id = 'io.atlasmap.java.test.TargetTestClass';
        docInitTarget.isSource = false;
        docInitTarget.name = 'io.atlasmap.java.test.TargetTestClass';
        docInitTarget.type = DocumentType.JAVA;
        docInitTarget.inspectionSource =
          'io.atlasmap.java.test.TargetTestClass';
        const docEnumtgt = cfg.addDocument(docInitTarget);

        const sl = new Field();
        sl.name = 'statesLong';
        sl.path = '/statesLong';
        sl.scope = '';
        sl.type = FieldType.COMPLEX;
        sl.documentField.status = FieldStatus.SUPPORTED;
        docEnumtgt.addField(sl);

        // Deserialize then serialize.
        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);
        expect(
          TestUtils.isEqualJSON(
            tName,
            atlasMappingEnumLookupTableMapping,
            MappingSerializer.serializeMappings(cfg)
          )
        ).toBe(true);

        expect(cfg.mappings.mappings[0].transition.mode).toBe(
          TransitionMode.ENUM
        );

        // Test the lookup table utilities
        LookupTableUtil.populateMappingLookupTable(
          cfg.mappings,
          cfg.mappings.mappings[0]
        );
        LookupTableUtil.updateLookupTables(cfg.mappings);
        const sourceField: Field = cfg.mappings.mappings[0].getFields(true)[0];
        sourceField.enumValues = [
          { name: 'AZ', ordinal: 0 },
          { name: 'FL', ordinal: 2 },
          { name: 'TX', ordinal: 4 },
        ];
        const targetField: Field = cfg.mappings.mappings[0].getFields(false)[0];
        targetField.enumValues = [
          { name: 'Arizona', ordinal: 0 },
          { name: 'Florida', ordinal: 2 },
          { name: 'Texas', ordinal: 4 },
        ];
        const tables: LookupTable[] = cfg.mappings!.getTables();
        const lookupTable = tables[0];
        expect(tables.length).toBeGreaterThan(0);
        const enumValues = LookupTableUtil.getEnumerationValues(
          cfg,
          cfg.mappings.mappings[0]
        );
        expect(enumValues).toBeDefined();

        // Inspect the mapping lookup table.
        expect(lookupTable.lookupEntry.length).toBe(3);
        let i = 0;
        for (const eVal of enumValues) {
          expect(eVal.sourceEnumValue).toEqual(sourceField.enumValues[i].name);
          expect(eVal.selectedTargetEnumValue).toEqual(
            targetField.enumValues[i].name
          );
          i++;
        }
        for (const entry of lookupTable.lookupEntry) {
          expect(entry.sourceType).toEqual('STRING');
          expect(entry.targetType).toEqual('STRING');
          expect(entry.sourceValue.length).toBe(2);
        }
        expect(tables[0].lookupEntry[0].targetValue).toEqual('Arizona');
        expect(tables[0].lookupEntry[1].targetValue).toEqual('Florida');
        expect(tables[0].lookupEntry[2].targetValue).toEqual('Texas');
        const flEntry = lookupTable.getEntryForSource('FL', false);
        expect(flEntry).toBeDefined();
        expect(flEntry?.targetValue).toEqual('Florida');

        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  tName = 'process a CSV mapping';
  test(tName, (done) => {
    cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    return cfg.fieldActionService
      .fetchFieldActions()
      .then(() => {
        cfg.mappings = new MappingDefinition();
        let fieldMapping = null;
        const mappingJson = atlasMappingCSV as IAtlasMappingContainer;

        // Isolate the mock documents using the document initialization model.
        const docModelSource = DocumentInspectionUtil.fromNonJavaProperties(
          cfg,
          'source',
          'CsvDataSource',
          DocumentType.CSV,
          InspectionType.SCHEMA,
          'io.atlasmap.csv.v2.CsvDataSource',
          true
        );
        cfg.initCfg.baseCSVInspectionServiceUrl = 'dummy';

        // Establish the CSV source fields.
        const sf1 = new Field();
        sf1.name = '<>';
        sf1.path = '/<>';
        sf1.type = FieldType.COMPLEX;
        docModelSource.doc.addField(sf1);
        const sf2 = new Field();
        sf2.name = 'last_name';
        sf2.path = '/<>/last_name';
        sf2.scope = '';
        sf2.type = FieldType.STRING;
        sf2.documentField.status = FieldStatus.SUPPORTED;
        sf2.parentField = sf1;
        docModelSource.doc.addField(sf2);

        const docModelTarget = DocumentInspectionUtil.fromNonJavaProperties(
          cfg,
          'target-csv',
          'CsvDataSource',
          DocumentType.CSV,
          InspectionType.SCHEMA,
          'io.atlasmap.csv.v2.CsvDataSource',
          false
        );
        expect(docModelTarget).toBeDefined();

        // Establish the CSV target fields.
        const st1 = new Field();
        st1.name = '<>';
        st1.path = '/<>';
        st1.type = FieldType.COMPLEX;
        docModelSource.doc.addField(st1);
        const st2 = new Field();
        st2.name = 'last';
        st2.path = '/<>/last';
        st2.scope = '';
        st2.type = FieldType.STRING;
        st2.documentField.status = FieldStatus.SUPPORTED;
        st2.parentField = st1;
        docModelTarget.doc.addField(st2);

        const docInspModel = DocumentInspectionUtil.fromDocumentDefinition(
          cfg,
          docModelTarget.doc
        );
        expect(docInspModel).toBeDefined();

        const csvInspModel = new CsvInspectionModel(cfg, docModelTarget.doc);
        expect(csvInspModel.isOnlineInspectionCapable()).toBe(true);

        // Find a CSV field mapping.
        for (fieldMapping of mappingJson?.AtlasMapping?.mappings
          ?.mapping as IMapping[]) {
          if (fieldMapping.inputField) {
            for (const field of fieldMapping.inputField) {
              if (field.name === 'last_name') {
                break;
              }
            }
          }
        }
        expect(fieldMapping).toBeDefined();

        MappingSerializer.deserializeMappingServiceJSON(mappingJson, cfg);
        MappingUtil.updateMappingsFromDocuments(cfg);
        expect(
          TestUtils.isEqualJSON(
            tName,
            atlasMappingCSV,
            MappingSerializer.serializeMappings(cfg)
          )
        ).toBe(true);

        done();
      })
      .catch((error) => {
        fail(error);
      });
  });
});
