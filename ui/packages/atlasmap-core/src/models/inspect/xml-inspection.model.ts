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
  NamespaceModel,
} from '../document-definition.model';
import {
  DocumentInspectionModel,
  DocumentInspectionRequestModel,
  DocumentInspectionRequestOptions,
} from './document-inspection.model';
import { EnumValue, Field } from '../field.model';
import { ErrorInfo, ErrorLevel, ErrorScope, ErrorType } from '../error.model';

export class XmlInspectionModel extends DocumentInspectionModel {
  request = new XmlInspectionRequestModel(this.cfg, this.doc);

  isOnlineInspectionCapable(): boolean {
    if (this.cfg.initCfg.baseXMLInspectionServiceUrl == null) {
      this.cfg.errorService.addError(
        new ErrorInfo({
          message: `XML inspection service is not configured. Document will not be loaded: ${this.doc.name}`,
          level: ErrorLevel.WARN,
          scope: ErrorScope.APPLICATION,
          type: ErrorType.INTERNAL,
          object: this.doc,
        })
      );
      return false;
    }
    return true;
  }

  parseResponse(responseJson: any): void {
    if (typeof responseJson.XmlInspectionResponse !== 'undefined') {
      this.extractXMLDocumentDefinitionFromInspectionResponse(
        responseJson,
        this.doc
      );
    } else if (typeof responseJson.XmlDocument !== 'undefined') {
      this.extractXMLDocumentDefinition(responseJson, this.doc);
    } else {
      throw new Error(`Unknown XML inspection result format: ${responseJson}`);
    }
  }

  private extractXMLDocumentDefinitionFromInspectionResponse(
    responseJson: any,
    docDef: DocumentDefinition
  ): void {
    const body: any = responseJson.XmlInspectionResponse;
    if (body.errorMessage) {
      docDef.errorOccurred = true;
      throw new Error(
        `Could not load XML document, error: ${body.errorMessage}`
      );
    }

    this.extractXMLDocumentDefinition(body, docDef);
  }

  private extractXMLDocumentDefinition(
    body: any,
    docDef: DocumentDefinition
  ): void {
    let xmlDocument: any;
    if (typeof body.xmlDocument !== 'undefined') {
      xmlDocument = body.xmlDocument;
    } else {
      xmlDocument = body.XmlDocument;
    }

    if (!docDef.description) {
      docDef.description = docDef.id;
    }
    if (!docDef.name) {
      docDef.name = docDef.id;
    }

    docDef.characterEncoding = xmlDocument.characterEncoding;
    docDef.locale = xmlDocument.locale;

    if (
      xmlDocument.xmlNamespaces &&
      xmlDocument.xmlNamespaces.xmlNamespace &&
      xmlDocument.xmlNamespaces.xmlNamespace.length
    ) {
      for (const serviceNS of xmlDocument.xmlNamespaces.xmlNamespace) {
        const ns: NamespaceModel = new NamespaceModel();
        ns.alias = serviceNS.alias;
        ns.uri = serviceNS.uri;
        ns.locationUri = serviceNS.locationUri;
        ns.isTarget = serviceNS.targetNamespace;
        docDef.namespaces.push(ns);
      }
    }

    for (const field of xmlDocument.fields.field) {
      if (!docDef.selectedRoot || this.isSelectedRootElement(field, docDef)) {
        this.parseXMLFieldFromDocument(field, null, docDef);
        break;
      }
    }
  }

  private isSelectedRootElement(
    field: any,
    docDef: DocumentDefinition
  ): boolean {
    return (
      docDef.selectedRoot &&
      field &&
      field.name &&
      docDef.selectedRoot ===
        (field.name.indexOf(':') !== -1 ? field.name.split(':')[1] : field.name)
    );
  }

  private parseXMLFieldFromDocument(
    field: any,
    parentField: Field | null,
    docDef: DocumentDefinition
  ): void {
    const parsedField = this.parseFieldFromDocument(field, parentField, docDef);
    if (parsedField == null) {
      return;
    }

    if (field.name.indexOf(':') !== -1) {
      parsedField.namespaceAlias = field.name.split(':')[0];
      parsedField.name = field.name.split(':')[1];
    }

    parsedField.isAttribute = parsedField.path.indexOf('@') !== -1;
    parsedField.enumeration = field.enumeration;

    if (
      parsedField.enumeration &&
      field.xmlEnumFields &&
      field.xmlEnumFields.xmlEnumField
    ) {
      for (const enumValue of field.xmlEnumFields.xmlEnumField) {
        const parsedEnumValue: EnumValue = new EnumValue();
        parsedEnumValue.name = enumValue.name;
        parsedEnumValue.ordinal = enumValue.ordinal;
        parsedField.enumValues.push(parsedEnumValue);
      }
    }
    if (
      field.xmlFields &&
      field.xmlFields.xmlField &&
      field.xmlFields.xmlField.length
    ) {
      for (const childField of field.xmlFields.xmlField) {
        this.parseXMLFieldFromDocument(childField, parsedField, docDef);
      }
    }
  }
}

export class XmlInspectionRequestModel extends DocumentInspectionRequestModel {
  url = this.cfg.initCfg.baseXMLInspectionServiceUrl + 'inspect';
  options = new XmlInspectionRequestOptions(this.cfg, this.doc);
}

export class XmlInspectionRequestOptions extends DocumentInspectionRequestOptions {
  json = {
    XmlInspectionRequest: {
      jsonType: 'io.atlasmap.xml.v2.XmlInspectionRequest',
      type: this.doc.inspectionType,
      xmlData: this.doc.inspectionSource,
    },
  };
}
