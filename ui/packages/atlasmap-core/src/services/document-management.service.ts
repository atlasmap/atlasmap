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
  FIELD_PATH_SEPARATOR,
  FieldType,
  InspectionType,
} from '../contracts/common';
import {
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
} from '../models/error.model';
import {
  HTTP_STATUS_NO_CONTENT,
  constantTypes,
  propertyTypes,
} from '../common/config.types';
import { Observable, Subscription } from 'rxjs';

import { CommonUtil } from '../utils/common-util';
import { ConfigModel } from '../models/config.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { DocumentInspectionModel } from '../models/inspect/document-inspection.model';
import { DocumentInspectionUtil } from '../utils/document-inspection-util';
import { Field } from '../models/field.model';
import { Guid } from '../utils';
import ky from 'ky';

/**
 * Manages Document object lifecycle. Import a Document source
 * such as JSON/XML schema/instance, request an inspection to the backend,
 * then enable it for mapping by consuming inspection result in UI.
 */
export class DocumentManagementService {
  cfg!: ConfigModel;

  private mappingUpdatedSubscription!: Subscription;
  private MAX_SEARCH_MATCH = 10000;

  constructor(private api: typeof ky) {}

  initialize(): void {
    this.mappingUpdatedSubscription =
      this.cfg.mappingService.mappingUpdated$.subscribe(() => {
        for (const d of this.cfg.getAllDocs()) {
          if (d.initialized) {
            d.updateFromMappings(this.cfg.mappings!); // TODO: check this non null operator
          }
        }
      });
  }

  uninitialize(): void {
    this.mappingUpdatedSubscription.unsubscribe();
  }

  /**
   * Request Document inspection for each {@link DocumentDefinition} object
   * stored in {@link ConfigModel} and populate it back with the inspection result.
   *
   * @returns
   */
  inspectDocuments(): Observable<DocumentDefinition> {
    return new Observable<DocumentDefinition>((observer) => {
      for (const docDef of this.cfg.getAllDocs()) {
        if (
          docDef === this.cfg.sourcePropertyDoc ||
          docDef === this.cfg.targetPropertyDoc ||
          docDef === this.cfg.constantDoc
        ) {
          docDef.initialized = true;
          continue;
        }

        const inspectionModel = DocumentInspectionUtil.fromDocumentDefinition(
          this.cfg,
          docDef
        );

        // TODO: check this non null operator
        this.inspectDocument(inspectionModel)
          .then(() => {
            observer.next(docDef);
          })
          .catch((error: any) => {
            observer.error(error);
          });
      }
    });
  }

  private inspectDocument(
    inspectionModel: DocumentInspectionModel
  ): Promise<DocumentDefinition> {
    return new Promise<DocumentDefinition>((resolve, reject) => {
      const docDef = inspectionModel.doc;
      if (docDef.inspectionResult) {
        const responseJson: any = JSON.parse(docDef.inspectionResult);
        inspectionModel.parseResponse(responseJson);
        docDef.initializeFromFields();
        docDef.initialized = true;
        resolve(docDef);
        return;
      }

      if (!inspectionModel.isOnlineInspectionCapable()) {
        docDef.initialized = true;
        docDef.errorOccurred = true;
        reject(docDef);
        return;
      }

      const request = inspectionModel.request;
      this.cfg.logger!.debug(
        `Document Inspection Request: ${JSON.stringify(request.options.json)}`
      );
      this.api
        .post(request.url, request.options)
        .json()
        .then((responseJson: any) => {
          this.cfg.logger!.debug(
            `Document Inspection Response: ${JSON.stringify(responseJson)}`
          );
          inspectionModel.parseResponse(responseJson);
          docDef.initializeFromFields();
          docDef.initialized = true;
          resolve(docDef);
        })
        .catch((error: any) => {
          this.cfg.errorService.addBackendError(
            `Failed to inspect Document: ${docDef.name}(${docDef.id})`,
            error
          );
          docDef.errorOccurred = true;
          reject(error);
        });
    });
  }

  getLibraryClassNames(): Promise<string[]> {
    return new Promise<string[]>((resolve, reject) => {
      if (typeof this.cfg.initCfg.baseMappingServiceUrl === 'undefined') {
        resolve([]);
        return;
      }
      const url: string =
        this.cfg.initCfg.baseMappingServiceUrl + 'library/list';
      this.cfg.logger!.debug('Library Class List Service Request: ' + url);
      this.api
        .get(url)
        .json()
        .then((body: any) => {
          this.cfg.logger!.debug(
            `Library Class List Service Response: ${JSON.stringify(body)}`
          );
          const classNames: string[] = body.ArrayList;
          resolve(classNames);
        })
        .catch((error: any) => {
          if (error.status !== HTTP_STATUS_NO_CONTENT) {
            this.cfg.errorService.addBackendError(
              'Error occurred while accessing the user uploaded JARs from the runtime service.',
              error
            );
            reject(error);
          } else {
            resolve([]);
          }
        });
    });
  }

  /**
   * Import user uploaded Document source such as JSON/XML schema/instance, assign
   * a unique Document ID with using GUID and delegate to {@link addNonJavaDocument}
   * to make it available  as a Document for mappings.
   * @see addNonJavaDocument
   *
   * @param selectedFile - user selected file
   * @param isSource - true is source panel, false is target
   * @param isSchema- user specified instance/ schema (true === schema)
   * @param inspectionParameters - CSV parameters
   *
   */
  importNonJavaDocument(
    selectedFile: File,
    isSource: boolean,
    isSchema: boolean,
    inspectionParameters?: { [key: string]: string }
  ): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
      let fileText = '';
      const reader = new FileReader();

      this.cfg.errorService.clearValidationErrors();

      const userFileComps = selectedFile.name.split('.');
      const userFile = userFileComps.slice(0, -1).join('.');
      const userFileSuffix: string =
        userFileComps[userFileComps.length - 1].toUpperCase();

      // Wait for the async read of the selected ascii document to be completed.
      try {
        fileText = await CommonUtil.readFile(selectedFile, reader);
      } catch (error) {
        this.cfg.errorService.addError(
          new ErrorInfo({
            message: 'Unable to import the specified schema document.',
            level: ErrorLevel.ERROR,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.USER,
            object: error,
          })
        );
        resolve(false);
        return;
      }

      let docType = undefined;
      try {
        docType = userFileSuffix as DocumentType;
        if (!docType) {
          throw new Error(docType);
        }
      } catch (error) {
        this.handleError(
          'Unrecognized document suffix (' + userFileSuffix + ')'
        );
        resolve(false);
        return;
      }
      this.addNonJavaDocument(
        fileText,
        userFile + '-' + Guid.newGuid(),
        userFile,
        docType,
        isSchema ? InspectionType.SCHEMA : InspectionType.INSTANCE,
        isSource,
        inspectionParameters
      ).then((value) => {
        if (!value) {
          resolve(false);
          return;
        }
        resolve(true);
      });
    });
  }

  /**
   * Import a Java class as a Document source and delegate to {@link addJavaDocument}
   * to make it available  as a Document for mappings.
   * JAR file(s) have to be imported and be available in the backend classpath
   * before doing this.
   * @see addJavaDocument
   * @todo https://github.com/atlasmap/atlasmap/issues/2919
   * A unique Document ID should be assigned with using GUID.
   *
   * @param className
   * @param isSource
   * @param collectionType
   * @param collectionClassName
   */
  importJavaDocument(
    className: string,
    isSource: boolean,
    collectionType = CollectionType.NONE,
    collectionClassName?: string
  ): Promise<boolean> {
    this.cfg.errorService.clearValidationErrors();
    return this.addJavaDocument(
      className,
      isSource,
      collectionType,
      collectionClassName
    );
  }
  /**
   * Add non-Java Document object into the {@link ConfigModel} store and
   * delegate to {@link doAddDocument}.
   * @see doAddDocument
   *
   * @param docBody
   * @param docId
   * @param docName
   * @param docType
   * @param inspectionType
   * @param isSource
   * @param parameters
   */
  addNonJavaDocument(
    docBody: any,
    docId: string,
    docName: string,
    docType: DocumentType,
    inspectionType: InspectionType,
    isSource: boolean,
    parameters?: { [key: string]: string }
  ): Promise<boolean> {
    const inspectionModel = DocumentInspectionUtil.fromNonJavaProperties(
      this.cfg,
      docId,
      docName,
      docType,
      inspectionType,
      docBody,
      isSource,
      parameters
    );
    return this.doAddDocument(inspectionModel);
  }

  /**
   * Add Java Document object into the {@link ConfigModel} store and
   * delegate to {@link doAddDocument}.
   * @see doAddDocument
   *
   * @param className
   * @param isSource
   * @param collectionType {@link CollectionType}
   * @param collectionClassName
   */
  addJavaDocument(
    className: string,
    isSource: boolean,
    collectionType: CollectionType = CollectionType.NONE,
    collectionClassName?: string
  ): Promise<boolean> {
    const inspectionModel = DocumentInspectionUtil.fromJavaProperties(
      this.cfg,
      className,
      isSource,
      collectionType,
      collectionClassName
    );
    return this.doAddDocument(inspectionModel);
  }

  /**
   * An utility method to look at the {@link DocumentType} passed in as an argument
   * and see if it's a Java or non-Java Document, then delegate to
   * {@link addJavaDocument} or {@link addNonJavaDocument}.
   *
   * @todo https://github.com/atlasmap/atlasmap/issues/2918
   * {@link CollectionType} and collectionClassName has to be persisted into digest
   * so it could be restored here
   */
  addDocument(
    docBody: any,
    docId: string,
    docName: string,
    docType: DocumentType,
    inspectionType: InspectionType,
    isSource: boolean,
    parameters?: { [key: string]: string }
  ): Promise<boolean> {
    if (docType === DocumentType.JAVA) {
      return this.addJavaDocument(docId, isSource);
    }
    return this.addNonJavaDocument(
      docBody,
      docId,
      docName,
      docType,
      inspectionType,
      isSource,
      parameters
    );
  }

  /**
   * Add or replace the {@link DocumentDefinition} object stored in {@link ConfigModel}
   * and delegate to {@link inspectDocument} to perform an inspection.
   * Then it gets available for mapping in the canvas.
   *
   * @param inspectionModel {@link DocumentInspectionModel}
   * @returns
   */
  private doAddDocument(
    inspectionModel: DocumentInspectionModel
  ): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
      let docdef = inspectionModel.doc;
      const isSource = docdef.isSource;

      this.inspectDocument(inspectionModel)
        .then(async (doc: DocumentDefinition) => {
          if (doc.fields.length === 0) {
            if (isSource) {
              CommonUtil.removeItemFromArray(docdef, this.cfg.sourceDocs);
            } else {
              CommonUtil.removeItemFromArray(docdef, this.cfg.targetDocs);
            }
          }
          docdef.updateFromMappings(this.cfg.mappings!);
          resolve(true);
        })
        .catch((error) => {
          this.cfg.logger?.error(
            `Failed to inspect Document ${docdef.name}(${docdef.id})`,
            error
          );
          docdef.errorOccurred = true;
          resolve(false);
        });
    });
  }

  private handleError(message: string, error?: any): void {
    this.cfg.errorService.addError(
      new ErrorInfo({
        message: message,
        level: ErrorLevel.ERROR,
        scope: ErrorScope.APPLICATION,
        type: ErrorType.INTERNAL,
        object: error,
      })
    );
  }

  /**
   * Filter Document fields that is shwon in a Source/Target Document tree.
   * @todo Consolidate with expression field search and Document Details field
   * search - https://github.com/atlasmap/atlasmap/issues/603
   * @param searchFilter
   * @param isSource
   */
  filterDocumentFields(searchFilter: string | undefined, isSource: boolean) {
    const cfg = ConfigModel.getConfig();

    let searchResultsExist = false;
    const searchIsEmpty: boolean =
      undefined === searchFilter || '' === searchFilter;
    const defaultVisibility: boolean = searchIsEmpty;
    for (const docDef of cfg.getDocs(isSource)) {
      docDef.visibleInCurrentDocumentSearch = defaultVisibility;
      for (const field of docDef.getAllFields()) {
        field.visibleInCurrentDocumentSearch = defaultVisibility;
      }
      if (!searchIsEmpty) {
        let searchFieldCount = 0;
        for (const field of docDef.getAllFields()) {
          // Skip this field if it's already determined to be visible.
          if (field.visibleInCurrentDocumentSearch && !field.collapsed) {
            continue;
          }
          field.visibleInCurrentDocumentSearch = field.name
            .toLowerCase()
            .includes(searchFilter!.toLowerCase());
          searchResultsExist =
            searchResultsExist || field.visibleInCurrentDocumentSearch;

          // The current field matches the user-specified filter.
          if (field.visibleInCurrentDocumentSearch) {
            docDef.visibleInCurrentDocumentSearch = true;
            let parentField = field.parentField;

            // Direct lineage is then visible.
            while (
              parentField != null &&
              !parentField.visibleInCurrentDocumentSearch
            ) {
              parentField.visibleInCurrentDocumentSearch = true;
              parentField.collapsed = false;
              parentField = parentField.parentField;
              searchFieldCount++;
            }

            // All fields below the matching field are also visible.
            try {
              this.markChildrenVisible(field);
            } catch (error) {
              cfg.errorService.addError(
                new ErrorInfo({
                  message: error.message,
                  level: ErrorLevel.INFO,
                  scope: ErrorScope.APPLICATION,
                  type: ErrorType.USER,
                })
              );
              break;
            }

            // The total number of matches is limited to allow the UI to perform.
            if (searchFieldCount++ >= this.MAX_SEARCH_MATCH) {
              cfg.errorService.addError(
                new ErrorInfo({
                  message:
                    'The maximum number of fields matching the specified search filter has beeen exceeded  ' +
                    'Try using a longer field filter.',
                  level: ErrorLevel.INFO,
                  scope: ErrorScope.APPLICATION,
                  type: ErrorType.USER,
                })
              );
              break;
            }
          }
        }
      }
    }
    cfg.mappingService.notifyLineRefresh();
  }

  private markChildrenVisible(field: Field): void {
    field.visibleInCurrentDocumentSearch = true;
    field.collapsed = false;
    // if (this.searchFieldCount++ >= this.maxSearchMatch) {
    //   throw new Error('The maximum number of fields matching the specified search filter has beeen exceeded  ' +
    //     'Try using a longer field filter.');
    // }
    for (const childField of field.children) {
      this.markChildrenVisible(childField);
    }
  }

  /**
   * Create a constant field.
   * @param constName
   * @param constValue
   * @param constType
   * @param addToActiveMapping
   */
  createConstant(
    constName: string,
    constValue: string,
    constType: string,
    addToActiveMapping?: boolean
  ): void {
    let field = this.cfg.constantDoc.getField(FIELD_PATH_SEPARATOR + constName);
    if (!field) {
      field = new Field();
    }
    field.name = constName;
    field.path = FIELD_PATH_SEPARATOR + constName;
    field.value = constValue;
    field.type = FieldType[constType as keyof typeof FieldType];
    field.docDef = this.cfg.constantDoc;
    field.userCreated = true;
    this.cfg.constantDoc.addField(field);
    if (addToActiveMapping) {
      this.cfg.mappingService.addFieldToActiveMapping(field);
    }
    this.cfg.mappingService.notifyMappingUpdated();
  }

  /**
   * Delete a constant field.
   * @param constName
   */
  deleteConstant(constName: string): void {
    const field = this.cfg.constantDoc.getField(
      FIELD_PATH_SEPARATOR + constName
    );
    if (!field) {
      return;
    }
    this.cfg.mappingService.removeFieldFromAllMappings(field);
    this.cfg.constantDoc.removeField(field);
    const activeMapping = this.cfg.mappings?.activeMapping;
    const expression = activeMapping?.transition?.expression;
    if (activeMapping && expression) {
      expression.updateFieldReference(activeMapping);
    }
    this.cfg.mappingService.notifyMappingUpdated();
  }

  /**
   * Edit a constant field. Look for an existing constant field with the specified name
   * and update the properties. If {@link origName} is specified and different from
   * {@link constName}, the name of the constant field will be changed.
   * @param constName
   * @param constValue
   * @param constType
   * @param origName
   */
  editConstant(
    constName: string,
    constValue: string,
    constType: string,
    origName?: string
  ): void {
    let constFieldName = origName ? origName : constName;
    let field = this.cfg.constantDoc.getField(
      FIELD_PATH_SEPARATOR + constFieldName
    );
    if (!field) {
      return;
    }
    if (constType.length > 0) {
      field.type = FieldType[constType as keyof typeof FieldType];
    }
    if (constValue.length > 0) {
      field.value = constValue;
    }
    if (origName && origName !== constName) {
      field.name = constName;
      field.path = FIELD_PATH_SEPARATOR + constName;
      this.cfg.constantDoc.updateField(field, FIELD_PATH_SEPARATOR + constName);
    }
    this.cfg.mappingService.notifyMappingUpdated();
  }

  /**
   * Get a {@link FieldType} of the constant field.
   * @param constName
   */
  getConstantType(constName: string): FieldType {
    const cfg = ConfigModel.getConfig();
    const field = cfg.constantDoc.getField(FIELD_PATH_SEPARATOR + constName);
    if (!field) {
      return FieldType.NONE;
    }
    return field.type;
  }

  /**
   * Get an index of the constant field type.
   * @param constName
   */
  getConstantTypeIndex(constName: string): number {
    const cfg = ConfigModel.getConfig();
    const field = cfg.constantDoc.getField(FIELD_PATH_SEPARATOR + constName);
    if (!field) {
      return 0;
    }
    for (let i = 0; i < constantTypes.length; i++) {
      if (constantTypes[i].includes(field.type)) {
        return i;
      }
    }
    return 0;
  }

  /**
   * Create a property field.
   * @param propName
   * @param propType
   * @param propScope
   * @param isSource
   * @param addToActiveMapping
   */
  createProperty(
    propName: string,
    propType: string,
    propScope: string,
    isSource: boolean,
    addToActiveMapping?: boolean
  ): void {
    const cfg = ConfigModel.getConfig();
    const path = this.getPropertyPath(propScope, propName);
    let field = isSource
      ? cfg.sourcePropertyDoc.getField(path)
      : cfg.targetPropertyDoc.getField(path);
    if (!field) {
      field = new Field();
    }
    field.name = propName;
    field.type = FieldType[propType as keyof typeof FieldType];
    field.scope = propScope;
    field.path = path;
    field.userCreated = true;

    if (isSource) {
      field.docDef = cfg.sourcePropertyDoc;
      cfg.sourcePropertyDoc.addField(field);
    } else {
      field.docDef = cfg.targetPropertyDoc;
      cfg.targetPropertyDoc.addField(field);
    }
    if (addToActiveMapping) {
      this.cfg.mappingService.addFieldToActiveMapping(field);
    }
    cfg.mappingService.notifyMappingUpdated();
  }

  /**
   * Delete a property field.
   * @param propName
   * @param propScope
   * @param isSource
   */
  deleteProperty(propName: string, propScope: string, isSource: boolean): void {
    const path = this.getPropertyPath(propScope, propName);
    const field = isSource
      ? this.cfg.sourcePropertyDoc.getField(path)
      : this.cfg.targetPropertyDoc.getField(path);
    if (!field) {
      return;
    }
    this.cfg.mappingService.removeFieldFromAllMappings(field);
    if (isSource) {
      this.cfg.sourcePropertyDoc.removeField(field);
    } else {
      this.cfg.targetPropertyDoc.removeField(field);
    }
    const activeMapping = this.cfg.mappings?.activeMapping;
    const expression = activeMapping?.transition?.expression;
    if (activeMapping && expression) {
      expression.updateFieldReference(activeMapping);
    }
    this.cfg.mappingService.notifyMappingUpdated();
  }

  /**
   * Edit a property field. When editing a property, the propName/propScope
   * is needed to fetch the existing field.  The newName and newScope may or
   * may not be specified.
   *
   * @param propName
   * @param propType
   * @param propScope
   * @param isSource
   * @param newName
   * @param newScope
   */
  editProperty(
    propName: string,
    propType: string,
    propScope: string,
    isSource: boolean,
    newName?: string,
    newScope?: string
  ): void {
    let oldPath = this.getPropertyPath(propScope, propName);
    let field = isSource
      ? this.cfg.sourcePropertyDoc.getField(oldPath)
      : this.cfg.targetPropertyDoc.getField(oldPath);
    if (!field) {
      return;
    }
    if (newName) {
      field.name = newName;
    }
    if (newScope) {
      field.scope = newScope;
    }
    field.type = FieldType[propType as keyof typeof FieldType];
    field.path = this.getPropertyPath(field.scope, field.name!);

    if (isSource) {
      this.cfg.sourcePropertyDoc.updateField(field, oldPath);
    } else {
      this.cfg.targetPropertyDoc.updateField(field, oldPath);
    }
    this.cfg.mappingService.notifyMappingUpdated();
  }

  /**
   * Get a {@link FieldType} of the property field.
   * @param propName
   * @param propScope
   * @param isSource
   */
  getPropertyType(
    propName: string,
    propScope: string,
    isSource: boolean
  ): FieldType {
    const field = isSource
      ? this.cfg.sourcePropertyDoc.getField(
          this.getPropertyPath(propScope, propName)
        )
      : this.cfg.targetPropertyDoc.getField(
          this.getPropertyPath(propScope, propName)
        );
    if (!field) {
      return FieldType.NONE;
    }
    return field.type;
  }

  /**
   * Get an index of the property field type.
   * @param propName
   * @param propScope
   * @param isSource
   */
  getPropertyTypeIndex(
    propName: string,
    propScope: string,
    isSource: boolean
  ): number {
    const field = isSource
      ? this.cfg.sourcePropertyDoc.getField(
          this.getPropertyPath(propScope, propName)
        )
      : this.cfg.targetPropertyDoc.getField(
          this.getPropertyPath(propScope, propName)
        );
    if (!field) {
      return 0;
    }
    for (let i = 0; i < propertyTypes.length; i++) {
      if (propertyTypes[i].includes(field.type)) {
        return i;
      }
    }
    return 0;
  }

  getPropertyPath(scope: string | undefined | null, name: string) {
    let answer = FIELD_PATH_SEPARATOR;
    if (scope && scope.length > 0) {
      answer += scope + FIELD_PATH_SEPARATOR;
    }
    answer += name;
    return answer;
  }
}
