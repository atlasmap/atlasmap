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
  CollectionType,
  DocumentType,
  InspectionType,
} from '../contracts/common';
import {
  ConfigModel,
  DocumentInitializationModel,
} from '../models/config.model';

import { CommonUtil } from './common-util';
import { CsvInspectionModel } from '../models/inspect/csv-inspection.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { DocumentInspectionModel } from '../models/inspect/document-inspection.model';
import { JavaInspectionModel } from '../models/inspect/java-inspection.model';
import { JsonInspectionModel } from '../models/inspect/json-inspection.model';
import { XmlInspectionModel } from '../models/inspect/xml-inspection.model';

export class DocumentInspectionUtil {
  /**
   * Create one of the subclass of {@link DocumentInspectionModel} which
   * corresponds to the existing {@link DocumentDefinition} passed in as an argument.
   * @see fromNonJavaProperties()
   * @see fromJavaProperties()
   *
   * @param cfg
   * @param doc
   * @returns
   */
  static fromDocumentDefinition(
    cfg: ConfigModel,
    doc: DocumentDefinition
  ): DocumentInspectionModel {
    switch (doc.type) {
      case DocumentType.JAVA:
        return new JavaInspectionModel(cfg, doc);
      case DocumentType.JSON:
        return new JsonInspectionModel(cfg, doc);
      case DocumentType.XSD:
      case DocumentType.XML:
        return new XmlInspectionModel(cfg, doc);
      case DocumentType.CSV:
        return new CsvInspectionModel(cfg, doc);
      default:
        throw new Error(
          `Document type '${doc.type}' is not supported for inspection`
        );
    }
  }

  /**
   * Create {@link JavaInspectionModel} from arguments and add corresponding
   * {@link DocumentDefinition} into {@link ConfigModel}.
   * @see fromDocumentDefinition()
   * @see fromNonJavaProperties()
   * @todo https://github.com/atlasmap/atlasmap/issues/2919
   * Assign GUID to Java Document ID as well, and use simple class name for default Document name
   *
   * @param cfg
   * @param className
   * @param isSource
   * @param collectionType
   * @param collectionClassName
   * @returns
   */
  static fromJavaProperties(
    cfg: ConfigModel,
    className: string,
    isSource: boolean,
    collectionType = CollectionType.NONE,
    collectionClassName?: string
  ): JavaInspectionModel {
    const model: DocumentInitializationModel =
      new DocumentInitializationModel();
    model.id = className;
    const simpleName = className.split('.').pop();
    model.name = simpleName ? simpleName : '';
    model.type = DocumentType.JAVA;
    model.inspectionType = InspectionType.JAVA_CLASS;
    model.inspectionSource = className;
    model.inspectionParameters = { '': '' };
    model.isSource = isSource;
    model.collectionType = collectionType;
    model.collectionClassName = collectionClassName;
    model.description = 'Java document class ' + className;
    if (collectionType && collectionType !== CollectionType.NONE) {
      model.description += ' collection type: ' + collectionType;
      if (collectionClassName) {
        model.description += ' collection class name: ' + collectionClassName;
      }
    }
    DocumentInspectionUtil.removeDocumentIfAlreadyExists(
      cfg,
      model.id,
      isSource
    );
    const doc = cfg.addDocument(model);
    return new JavaInspectionModel(cfg, doc);
  }

  /**
   * Create one of the subclass of {@link DocumentInspectionModel}
   * other than Java from arguments and add corresponding
   * {@link DocumentDefinition} into {@link ConfigModel}..
   * @see fromDocumentDefinition()
   * @see fromJavaProperties()
   *
   * @param cfg
   * @param id
   * @param name
   * @param documentType
   * @param inspectionType
   * @param inspectionSource
   * @param isSource
   * @param inspectionParameters
   * @returns
   */
  static fromNonJavaProperties(
    cfg: ConfigModel,
    id: string,
    name: string,
    documentType: DocumentType,
    inspectionType: InspectionType,
    inspectionSource: string,
    isSource: boolean,
    inspectionParameters?: { [key: string]: string }
  ): DocumentInspectionModel {
    const model: DocumentInitializationModel =
      new DocumentInitializationModel();
    model.name = name;
    model.id = id;
    model.type = documentType;
    model.inspectionType = inspectionType;
    model.inspectionSource = inspectionSource;
    if (inspectionParameters) {
      model.inspectionParameters = inspectionParameters;
    } else {
      model.inspectionParameters = { '': '' };
    }
    model.isSource = isSource;
    model.description = isSource ? 'Source document ' : 'Target document ';
    model.description += name + ' type: ' + documentType;
    DocumentInspectionUtil.removeDocumentIfAlreadyExists(
      cfg,
      model.id,
      isSource
    );
    const doc = cfg.addDocument(model);
    return this.fromDocumentDefinition(cfg, doc);
  }

  private static removeDocumentIfAlreadyExists(
    cfg: ConfigModel,
    id: string,
    isSource: boolean
  ) {
    // Clear out the existing document if importing the same name.
    const existing = cfg.getDocForIdentifier(id, isSource);
    if (existing) {
      if (isSource) {
        CommonUtil.removeItemFromArray(existing, cfg.sourceDocs);
      } else {
        CommonUtil.removeItemFromArray(existing, cfg.targetDocs);
      }
    }
  }
}
