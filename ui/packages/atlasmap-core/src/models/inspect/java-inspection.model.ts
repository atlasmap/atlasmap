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
import { CollectionType, FieldType } from '../../contracts/common';
import {
  DocumentInspectionModel,
  DocumentInspectionRequestModel,
  DocumentInspectionRequestOptions,
} from './document-inspection.model';
import { EnumValue, Field } from '../field.model';
import { ErrorInfo, ErrorLevel, ErrorScope, ErrorType } from '../error.model';
import {
  IClassInspectionRequestContainer,
  IClassInspectionResponseContainer,
  IJavaClass,
  IJavaClassContainer,
  IJavaField,
  JAVA_INSPECTION_REQUEST_JSON_TYPE,
} from '../../contracts/documents/java';

import { ConfigModel } from '../config.model';
import { DocumentDefinition } from '../document-definition.model';

/**
 * Encapsulates Java class inspection context.
 */
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
    let javaClass: IJavaClass;
    if (responseJson.ClassInspectionResponse) {
      if (responseJson.errorMessage) {
        this.doc.errorOccurred = true;
        throw new Error(
          `Could not load JSON document, error: ${responseJson.errorMessage}`
        );
      }
      javaClass = (responseJson as IClassInspectionResponseContainer)
        .ClassInspectionResponse.javaClass;
    } else {
      javaClass = (responseJson as IJavaClassContainer).JavaClass;
    }
    const docIdentifier: string = this.doc.id;
    if (!javaClass || javaClass.status === 'NOT_FOUND') {
      this.doc.errorOccurred = true;
      throw new Error(
        `Could not load JAVA document. Document is not found: ${docIdentifier}`
      );
    }

    if (!this.doc.description) {
      this.doc.description = javaClass.className;
    }
    if (!this.doc.name) {
      this.doc.name = javaClass.className!;
      // Make doc name the class name rather than fully qualified name
      if (this.doc.name && this.doc.name.indexOf('.') !== -1) {
        this.doc.name = this.doc.name.substr(
          this.doc.name.lastIndexOf('.') + 1
        );
      }
    }
    if (javaClass.uri && (!this.doc.uri || this.doc.uri.length === 0)) {
      this.doc.uri = javaClass.uri;
    }

    let rootField = null;
    if (
      javaClass.collectionType &&
      javaClass.collectionType !== CollectionType.NONE.valueOf()
    ) {
      this.parseJavaFieldFromDocument(javaClass, null);
      rootField = this.doc.fields[0];
    }
    for (const field of javaClass.javaFields.javaField) {
      this.parseJavaFieldFromDocument(field, rootField);
    }
  }

  private parseJavaFieldFromDocument(
    field: IJavaField,
    parentField: Field | null
  ): void {
    const parsedField = this.parseFieldFromDocument(field, parentField);
    if (parsedField == null) {
      return;
    }

    if (field.className) {
      parsedField.classIdentifier = field.className;
    }

    if (field.fieldType !== FieldType.COMPLEX) {
      return;
    }

    const javaClass = field as IJavaClass;
    parsedField.enumeration = javaClass.enumeration;
    if (javaClass.enumeration && javaClass.javaEnumFields?.javaEnumField) {
      for (const enumValue of javaClass.javaEnumFields.javaEnumField) {
        const parsedEnumValue: EnumValue = new EnumValue();
        parsedEnumValue.name = enumValue.name;
        parsedEnumValue.ordinal = enumValue.ordinal;
        parsedField.enumValues.push(parsedEnumValue);
      }
    }

    if (javaClass.javaFields?.javaField?.length) {
      for (const childField of javaClass.javaFields.javaField) {
        this.parseJavaFieldFromDocument(childField, parsedField);
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
    const request: IClassInspectionRequestContainer = {
      ClassInspectionRequest: {
        jsonType: JAVA_INSPECTION_REQUEST_JSON_TYPE,
        className: this.doc.inspectionSource,
        disablePrivateOnlyFields: this.cfg.initCfg.disablePrivateOnlyFields,
        disableProtectedOnlyFields: this.cfg.initCfg.disableProtectedOnlyFields,
        disablePublicOnlyFields: this.cfg.initCfg.disablePublicOnlyFields,
        disablePublicGetterSetterFields:
          this.cfg.initCfg.disablePublicGetterSetterFields,
      },
    };
    this.json = request;
    if (
      this.doc.initModel.collectionType &&
      (this.doc.initModel.collectionType as CollectionType) !==
        CollectionType.NONE
    ) {
      request.ClassInspectionRequest.collectionType =
        this.doc.initModel.collectionType;
      if (this.doc.initModel.collectionClassName) {
        request.ClassInspectionRequest.collectionClassName =
          this.doc.initModel.collectionClassName;
      }
    }
    if (
      this.cfg.initCfg.fieldNameExclusions &&
      this.cfg.initCfg.fieldNameExclusions.length
    ) {
      request.ClassInspectionRequest.fieldNameExclusions = {
        string: this.cfg.initCfg.fieldNameExclusions,
      };
    }
    if (
      this.cfg.initCfg.classNameExclusions &&
      this.cfg.initCfg.classNameExclusions.length
    ) {
      request.ClassInspectionRequest.classNameExclusions = {
        string: this.cfg.initCfg.classNameExclusions,
      };
    }
  }
}
