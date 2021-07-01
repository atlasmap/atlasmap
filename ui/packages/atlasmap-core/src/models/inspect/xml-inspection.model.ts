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
  DocumentInspectionModel,
  DocumentInspectionRequestModel,
  DocumentInspectionRequestOptions,
} from './document-inspection.model';
import { EnumValue, Field } from '../field.model';
import { ErrorInfo, ErrorLevel, ErrorScope, ErrorType } from '../error.model';
import { FieldType, IField } from '../../contracts/common';
import {
  IXmlComplexType,
  IXmlDocument,
  IXmlDocumentContainer,
  IXmlField,
  IXmlInspectionResponse,
  IXmlInspectionResponseContainer,
  XML_INSPECTION_REQUEST_JSON_TYPE,
} from '../../contracts/documents/xml';
import { NamespaceModel } from '../document-definition.model';

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
        (responseJson as IXmlInspectionResponseContainer).XmlInspectionResponse
      );
    } else if (typeof responseJson.XmlDocument !== 'undefined') {
      this.extractXMLDocumentDefinition(
        (responseJson as IXmlDocumentContainer).XmlDocument
      );
    } else {
      throw new Error(`Unknown XML inspection result format: ${responseJson}`);
    }
  }

  private extractXMLDocumentDefinitionFromInspectionResponse(
    body: IXmlInspectionResponse
  ): void {
    if (body.errorMessage) {
      this.doc.errorOccurred = true;
      throw new Error(
        `Could not load XML document, error: ${body.errorMessage}`
      );
    }

    this.extractXMLDocumentDefinition(body.xmlDocument);
  }

  private extractXMLDocumentDefinition(xmlDocument: IXmlDocument): void {
    if (!this.doc.description) {
      this.doc.description = this.doc.id;
    }
    if (!this.doc.name) {
      this.doc.name = this.doc.id;
    }

    if (xmlDocument?.xmlNamespaces?.xmlNamespace?.length) {
      for (const serviceNS of xmlDocument.xmlNamespaces.xmlNamespace) {
        const ns: NamespaceModel = new NamespaceModel();
        ns.alias = serviceNS.alias;
        ns.uri = serviceNS.uri;
        ns.locationUri = serviceNS.locationUri;
        ns.isTarget = serviceNS.targetNamespace;
        this.doc.namespaces.push(ns);
      }
    }

    for (const field of xmlDocument.fields.field) {
      if (!this.doc.selectedRoot || this.isSelectedRootElement(field)) {
        this.parseXMLFieldFromDocument(field, null);
        break;
      }
    }
  }

  private isSelectedRootElement(field: IField): boolean {
    if (!this.doc.selectedRoot && !field?.name) {
      return false;
    }
    return (
      this.doc.selectedRoot ===
      (field.name!.indexOf(':') !== -1 ? field.name!.split(':')[1] : field.name)
    );
  }

  private parseXMLFieldFromDocument(
    field: IXmlField,
    parentField: Field | null
  ): void {
    const parsedField = this.parseFieldFromDocument(field, parentField);
    if (parsedField == null) {
      return;
    }

    if (field.name!.indexOf(':') !== -1) {
      parsedField.namespaceAlias = field.name!.split(':')[0];
      parsedField.name = field.name!.split(':')[1];
    }

    parsedField.isAttribute = parsedField.path.indexOf('@') !== -1;
    if (field.fieldType !== FieldType.COMPLEX) {
      return;
    }
    const complex = field as IXmlComplexType;

    parsedField.enumeration = complex.enumeration;

    if (parsedField.enumeration && complex.xmlEnumFields?.xmlEnumField) {
      for (const enumValue of complex.xmlEnumFields.xmlEnumField) {
        const parsedEnumValue: EnumValue = new EnumValue();
        parsedEnumValue.name = enumValue.name!;
        parsedEnumValue.ordinal = enumValue.ordinal;
        parsedField.enumValues.push(parsedEnumValue);
      }
    }
    if (complex.xmlFields?.xmlField.length) {
      for (const childField of complex.xmlFields.xmlField) {
        this.parseXMLFieldFromDocument(childField, parsedField);
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
      jsonType: XML_INSPECTION_REQUEST_JSON_TYPE,
      type: this.doc.inspectionType,
      xmlData: this.doc.inspectionSource,
    },
  };
}
