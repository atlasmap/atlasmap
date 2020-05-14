/* tslint:disable:no-unused-variable */

import { NamespaceModel, DocumentDefinition } from '../../src/models/document-definition.model';
import { DocumentType } from '../../src/common/config.types';

describe('DocumentDefinitionModel', () => {

  test('initialize NamespaceModel', () => {
    const nsModel = new NamespaceModel;
    nsModel.uri = 'http://atlasmap.io/xml/test/testNamespace';
    nsModel.alias = 'ns1';
    expect(nsModel.getPrettyLabel()).toBe('ns1 [http://atlasmap.io/xml/test/testNamespace]');
  });

  test('initialize DocumentDefinition', () => {
    const docDef = new DocumentDefinition;
    docDef.name = 'TestDoc';
    docDef.type = DocumentType.JAVA;
    expect(docDef.getName(true)).toEqual('TestDoc (JAVA)');
  });

});
