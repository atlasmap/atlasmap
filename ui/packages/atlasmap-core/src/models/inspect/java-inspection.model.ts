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

import { CollectionType } from '../../common/config.types';
import { ConfigModel } from '../config.model';
import { DocumentDefinition } from '../document-definition.model';

export class JavaInspectionModel extends DocumentInspectionModel {
  request = new JavaInspectionRequestModel(this.cfg, this.doc);

  isOnlineInspectionCapable(): boolean {
    if (this.cfg.initCfg.baseJavaInspectionServiceUrl == null) {
      this.cfg.errorService.addError(
        new ErrorInfo({
          message: `Java inspection service is not configured. Document will not be loaded: ${this.doc.name}`,
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
    if (typeof responseJson.ClassInspectionResponse !== 'undefined') {
      this.extractJavaDocumentDefinitionFromInspectionResponse(
        responseJson,
        this.doc
      );
    } else if (typeof responseJson.JavaClass !== 'undefined') {
      this.extractJavaDocumentDefinition(responseJson, this.doc);
    } else {
      throw new Error(`Unknown Java inspection result format: ${responseJson}`);
    }
  }

  private extractJavaDocumentDefinitionFromInspectionResponse(
    responseJson: any,
    docDef: DocumentDefinition
  ): void {
    const body: any = responseJson.ClassInspectionResponse;

    if (body.errorMessage) {
      docDef.errorOccurred = true;
      throw new Error(
        `Could not load Java document, error: ${body.errorMessage}`
      );
    }
    this.extractJavaDocumentDefinition(body, docDef);
  }

  private extractJavaDocumentDefinition(
    body: any,
    docDef: DocumentDefinition
  ): void {
    const docIdentifier: string = docDef.id;
    const javaClass = body.JavaClass ? body.JavaClass : body.javaClass;
    if (!javaClass || javaClass.status === 'NOT_FOUND') {
      docDef.errorOccurred = true;
      throw new Error(
        `Could not load JAVA document. Document is not found: ${docIdentifier}`
      );
    }

    if (!docDef.description) {
      docDef.description = javaClass.className;
    }
    if (!docDef.name) {
      docDef.name = javaClass.className;
      // Make doc name the class name rather than fully qualified name
      if (docDef.name && docDef.name.indexOf('.') !== -1) {
        docDef.name = docDef.name.substr(docDef.name.lastIndexOf('.') + 1);
      }
    }
    if (javaClass.uri && (!docDef.uri || docDef.uri.length === 0)) {
      docDef.uri = javaClass.uri;
    }

    docDef.characterEncoding = javaClass.characterEncoding;
    docDef.locale = javaClass.locale;

    let rootField = null;
    if (
      javaClass.collectionType &&
      javaClass.collectionType !== CollectionType.NONE.valueOf()
    ) {
      this.parseJavaFieldFromDocument(javaClass, null, docDef);
      rootField = docDef.fields[0];
    }
    for (const field of javaClass.javaFields.javaField) {
      this.parseJavaFieldFromDocument(field, rootField, docDef);
    }
  }

  private parseJavaFieldFromDocument(
    field: any,
    parentField: Field | null,
    docDef: DocumentDefinition
  ): void {
    const parsedField = this.parseFieldFromDocument(field, parentField, docDef);
    if (parsedField == null) {
      return;
    }

    // java fields have a special primitive property, so override the "!= COMPLEX" math from parseFieldFromDocument()
    parsedField.isPrimitive = field.primitive;
    parsedField.classIdentifier = field.className;
    parsedField.enumeration = field.enumeration;

    if (
      parsedField.enumeration &&
      field.javaEnumFields &&
      field.javaEnumFields.javaEnumField
    ) {
      for (const enumValue of field.javaEnumFields.javaEnumField) {
        const parsedEnumValue: EnumValue = new EnumValue();
        parsedEnumValue.name = enumValue.name;
        parsedEnumValue.ordinal = enumValue.ordinal;
        parsedField.enumValues.push(parsedEnumValue);
      }
    }

    if (
      field.javaFields &&
      field.javaFields.javaField &&
      field.javaFields.javaField.length
    ) {
      for (const childField of field.javaFields.javaField) {
        this.parseJavaFieldFromDocument(childField, parsedField, docDef);
      }
    }
  }
}

export class JavaInspectionRequestModel extends DocumentInspectionRequestModel {
  url = this.cfg.initCfg.baseJavaInspectionServiceUrl + 'class';
  options = new JavaInspectionRequestOptions(this.cfg, this.doc);
}

export class JavaInspectionRequestOptions extends DocumentInspectionRequestOptions {
  constructor(protected cfg: ConfigModel, protected doc: DocumentDefinition) {
    super(cfg, doc);
    this.json = {
      ClassInspectionRequest: {
        jsonType:
          ConfigModel.javaServicesPackagePrefix + '.ClassInspectionRequest',
        className: this.doc.inspectionSource,
        disablePrivateOnlyFields: this.cfg.initCfg.disablePrivateOnlyFields,
        disableProtectedOnlyFields: this.cfg.initCfg.disableProtectedOnlyFields,
        disablePublicOnlyFields: this.cfg.initCfg.disablePublicOnlyFields,
        disablePublicGetterSetterFields:
          this.cfg.initCfg.disablePublicGetterSetterFields,
      },
    };
    if (
      this.doc.initModel.collectionType &&
      (this.doc.initModel.collectionType as CollectionType) !==
        CollectionType.NONE
    ) {
      this.json['ClassInspectionRequest']['collectionType'] =
        this.doc.initModel.collectionType;
      if (this.doc.initModel.collectionClassName) {
        this.json['ClassInspectionRequest']['collectionClassName'] =
          this.doc.initModel.collectionClassName;
      }
    }
    if (
      this.cfg.initCfg.fieldNameExclusions &&
      this.cfg.initCfg.fieldNameExclusions.length
    ) {
      this.json['ClassInspectionRequest']['fieldNameExclusions'] = {
        string: this.cfg.initCfg.fieldNameExclusions,
      };
    }
    if (
      this.cfg.initCfg.classNameExclusions &&
      this.cfg.initCfg.classNameExclusions.length
    ) {
      this.json['ClassInspectionRequest']['classNameExclusions'] = {
        string: this.cfg.initCfg.classNameExclusions,
      };
    }
  }
}
