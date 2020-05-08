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

import ky from 'ky';
import {Observable, Subscription} from 'rxjs';

import {CollectionType, DocumentType, InspectionType} from '../common/config.types';
import {ConfigModel} from '../models/config.model';
import {EnumValue, Field} from '../models/field.model';
import {DocumentDefinition, NamespaceModel} from '../models/document-definition.model';

import {DataMapperUtil} from '../common/data-mapper-util';
import {ErrorInfo, ErrorLevel, ErrorScope, ErrorType} from '../models/error.model';

export class DocumentManagementService {
  cfg!: ConfigModel;

  private mappingUpdatedSubscription!: Subscription;

  private headers = {'Content-Type': 'application/json'};

  /**
   * Use the JSON utility to translate the specified buffer into a JSON buffer - then replace any
   * non-ascii character encodings with unicode escape sequences.
   *
   * @param buffer
   */
  static sanitizeJSON(buffer: string): string {
    let jsonBuffer = buffer;
    jsonBuffer = JSON.stringify(buffer);
    jsonBuffer = jsonBuffer.replace(/[\u007F-\uFFFF]/g, function(chr) {
      return '\\u' + ('0000' + chr.charCodeAt(0).toString(16)).substr(-4);
    });
    return jsonBuffer;
  }

  /**
   * Restrict JSON parsing to the document management service.
   *
   * @param buffer
   */
  static getMappingsInfo(buffer: any): any {
    if (typeof buffer === 'string') {
      return JSON.parse(<string>buffer);
    } else {
      return buffer;
    }
  }

  /**
   * Capture the specified user mappings into a general catalog JSON buffer (exportMappings).
   * @param buffer
   */
  static generateExportMappings(buffer: string): string {

    if (buffer === null || buffer.length === 0) {
      return '';
    }
    const metaStr = `   "exportMappings":
    {
       \"value\": ` + this.sanitizeJSON(buffer) + `
    },\n`;

    return metaStr;
  }

  /**
   * Capture the specified user JSON or XML document buffer into a general catalog JSON buffer.
   *
   * @param buffer
   */
  static generateExportBlockData(buffer: string): string {

    if (buffer === null || buffer.length === 0) {
        return '';
    }
    const metaStr = `
          {
             \"value\": ` + this.sanitizeJSON(buffer) + `
          }`;
    return metaStr;
  }

  /**
   * Capture the specified user document definition meta data into a general catalog JSON buffer.
   * @param docDef
   */
  static generateExportMetaStr(docDef: DocumentDefinition): string {
    const metaStr = `
       {
          \"name\": \"` + docDef.name + `\",
          \"documentType\": \"` + docDef.type + `\",
          \"inspectionType\": \"` + docDef.inspectionType + `\",
          \"isSource\": \"` + docDef.isSource + `\"
       }`;
    return metaStr;
  }

  constructor(private api: typeof ky) {}

  initialize(): void {
    this.mappingUpdatedSubscription
      = this.cfg.mappingService.mappingUpdated$.subscribe(() => {
      for (const d of this.cfg.getAllDocs()) {
        if (d.initialized) {
          d.updateFromMappings(this.cfg.mappings!); // TODO: check this non null operator
        }
      }
    });
  }

  ngOnDestroy() {
    this.mappingUpdatedSubscription.unsubscribe();
  }

  fetchClassPath(): Observable<string> {
    return new Observable<string>((observer: any) => {
      const requestBody = {
        'MavenClasspathRequest': {
          'jsonType': ConfigModel.javaServicesPackagePrefix + '.MavenClasspathRequest',
          'pomXmlData': this.cfg.initCfg.pomPayload,
          'executeTimeout': this.cfg.initCfg.classPathFetchTimeoutInMilliseconds,
        },
      };
      const url: string = this.cfg.initCfg.baseJavaInspectionServiceUrl + 'mavenclasspath';
      this.cfg.logger!.debug(`Classpath Service Request: ${JSON.stringify(requestBody)}`);
      this.api.post(url, { json: requestBody, headers: this.headers }).json()
        .then((body: any) => {
          this.cfg.logger!.debug(`Classpath Service Response: ${JSON.stringify(body)}`);
          const classPath: string = body.MavenClasspathResponse.classpath;
          observer.next(classPath);
          observer.complete();
        })
        .catch((error: any) => {
          observer.error(error);
          observer.complete();
        },
      );
    });
  }

  fetchDocument(docDef: DocumentDefinition, classPath: string): Observable<DocumentDefinition> {
    return new Observable<DocumentDefinition>((observer: any) => {
      if (docDef.inspectionResult) {
        const responseJson: any = JSON.parse(docDef.inspectionResult);
        this.parseDocumentResponse(responseJson, docDef);
        observer.next(docDef);
        observer.complete();
        return;
      }

      const payload: any = this.createDocumentFetchRequest(docDef, classPath);
      let options: any = {json: payload, headers: this.headers};
      let url: string = this.cfg.initCfg.baseJavaInspectionServiceUrl + 'class';
      if ((docDef.type === DocumentType.XML) || (docDef.type === DocumentType.XSD)) {
        url = this.cfg.initCfg.baseXMLInspectionServiceUrl + 'inspect';
      } else if (docDef.type === DocumentType.JSON) {
        url = this.cfg.initCfg.baseJSONInspectionServiceUrl + 'inspect';
      } else if (docDef.type === DocumentType.CSV) {
        url = this.cfg.initCfg.baseCSVInspectionServiceUrl + 'inspect';
        options = {body: payload, headers: this.headers, searchParams: {firstRecordAsHeader: true}};
      }

      this.cfg.logger!.debug(`Document Service Request: ${JSON.stringify(payload)}`);
      this.api.post(url, options).json()
        .then((responseJson: any) => {
          this.cfg.logger!.debug(`Document Service Response: ${JSON.stringify(responseJson)}`);
          this.parseDocumentResponse(responseJson, docDef);
          observer.next(docDef);
          observer.complete();
        })
        .catch((error: any) => {
          observer.error(error);
          docDef.errorOccurred = true;
          observer.next(docDef);
          observer.complete();
        },
      );
    });
  }

  /**
   * Push a user-defined Java archive file (binary buffer) to the runtime.
   *
   * @param binaryBuffer
   */
  setLibraryToService(binaryBuffer: any, callback: (success: boolean, res: any) => void): void {
    const url = this.cfg.initCfg.baseMappingServiceUrl + 'library';
    this.cfg.logger!.debug('Set Library Service Request');
    const fileContent: Blob = new Blob([binaryBuffer], {type: 'application/octet-stream'});
    this.api.put(url, { body: fileContent }).blob().then((res: any) => {
      callback(true, res);
      this.cfg.logger!.debug(`Set Library Service Response: ${JSON.stringify(res)}`);
    })
    .catch((error: any) => {
      callback(false, error);
      this.handleError('Error occurred while uploading a JAR file to the server.', error); },
    );
  }

  /**
   * Read the selected file and parse it with the format defined by the specified inspection type.  Call the
   * initialization service to update the sources/ targets in both the runtime and the UI.  The runtime will
   * parse/ validate the file.
   *
   * @param selectedFile
   * @param inspectionType
   * @param isSource
   */
  async processDocument(selectedFile: any, inspectionType: InspectionType, isSource: boolean): Promise<boolean> {
    return new Promise<boolean>( async(resolve) => {
      let fileBin = null;
      let fileText = '';
      const reader = new FileReader();

      this.cfg.errorService.clearValidationErrors();

      const userFileComps = selectedFile.name.split('.');
      const userFile = userFileComps[0];
      const userFileSuffix: string = userFileComps[userFileComps.length - 1].toUpperCase();

      if (userFileSuffix === DocumentType.JAVA_ARCHIVE) {

        // Wait for the async read of the selected binary document to be completed.
        try {
          fileBin = await DataMapperUtil.readBinaryFile(selectedFile, reader);
        } catch (error) {
          this.cfg.errorService.addError(new ErrorInfo({message: 'Unable to import the specified schema document.',
            level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.USER, object: error}));
          resolve(false);
          return;
        }
        if (inspectionType === InspectionType.UNKNOWN) {
          inspectionType = InspectionType.JAVA_CLASS;
        }
      } else {
        // Wait for the async read of the selected ascii document to be completed.
        try {
          fileText = await DataMapperUtil.readFile(selectedFile, reader);
        } catch (error) {
          this.cfg.errorService.addError(new ErrorInfo({message: 'Unable to import the specified schema document.',
            level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.USER, object: error}));
          resolve(false);
          return;
        }

        // Derive the format if not already defined.
        if (inspectionType === InspectionType.UNKNOWN) {
          if ((userFileSuffix === DocumentType.XSD) ||
              (fileText.search('SchemaSet') > -1) ||
              (fileText.search('\\$schema') > -1)) {
            inspectionType = InspectionType.SCHEMA;
          } else {
            inspectionType = InspectionType.INSTANCE;
          }
        }
      }

      switch (userFileSuffix) {

      case DocumentType.JSON:
        await this.cfg.initializationService.initializeUserDoc(fileText, userFile, DocumentType.JSON,
          inspectionType, isSource);
        break;

      case DocumentType.JAVA_ARCHIVE:
        await this.cfg.initializationService.initializeUserDoc(fileBin, userFile, DocumentType.JAVA_ARCHIVE,
          inspectionType, isSource);
        this.cfg.errorService.addError(new ErrorInfo({
          message: `${selectedFile.name} import complete.  Select the plus icon on the Sources/Targets panel to enable specific classes.`,
          level: ErrorLevel.INFO, scope: ErrorScope.APPLICATION, type: ErrorType.USER}));
        resolve(true);
        return;

      case 'java':
        await this.cfg.initializationService.initializeUserDoc(fileText, userFile, DocumentType.JAVA,
          inspectionType, isSource);
        break;

      case DocumentType.CSV:
        this.cfg.initializationService.initializeUserDoc(fileText, userFile, DocumentType.CSV,
            inspectionType, isSource);
        break;

      case DocumentType.XML:
      case DocumentType.XSD:
        await this.cfg.initializationService.initializeUserDoc(fileText, userFile, userFileSuffix,
          inspectionType, isSource);
        break;

      default:
        this.handleError('Unrecognized document suffix (' + userFileSuffix + ')', null);
      }

      this.cfg.errorService.addError(new ErrorInfo({message: `${selectedFile.name} ${userFileSuffix} import complete.`,
        level: ErrorLevel.INFO, scope: ErrorScope.APPLICATION, type: ErrorType.USER}));
      resolve(true);
    });
  }

  private createDocumentFetchRequest(docDef: DocumentDefinition, classPath: string): any {
    if ((docDef.type === DocumentType.XML) || (docDef.type === DocumentType.XSD)) {
      return {
        'XmlInspectionRequest': {
          'jsonType': 'io.atlasmap.xml.v2.XmlInspectionRequest',
          'type': docDef.inspectionType,
          'xmlData': docDef.inspectionSource,
        },
      };
    }
    if (docDef.type === DocumentType.JSON) {
      return {
        'JsonInspectionRequest': {
          'jsonType': 'io.atlasmap.json.v2.JsonInspectionRequest',
          'type': docDef.inspectionType,
          'jsonData': docDef.inspectionSource,
        },
      };
    }
    if (docDef.type === DocumentType.CSV) {
      return docDef.inspectionSource;
    }
    const className: string = docDef.inspectionSource;
    const payload: any = {
      'ClassInspectionRequest': {
        'jsonType': ConfigModel.javaServicesPackagePrefix + '.ClassInspectionRequest',
        'classpath': classPath,
        'className': className,
        'disablePrivateOnlyFields': this.cfg.initCfg.disablePrivateOnlyFields,
        'disableProtectedOnlyFields': this.cfg.initCfg.disableProtectedOnlyFields,
        'disablePublicOnlyFields': this.cfg.initCfg.disablePublicOnlyFields,
        'disablePublicGetterSetterFields': this.cfg.initCfg.disablePublicGetterSetterFields,
      },
    };
    if (docDef.initModel.collectionType && docDef.initModel.collectionType as CollectionType !== CollectionType.NONE) {
      payload['ClassInspectionRequest']['collectionType'] = docDef.initModel.collectionType;
      if (docDef.initModel.collectionClassName) {
        payload['ClassInspectionRequest']['collectionClassName'] = docDef.initModel.collectionClassName;
      }
    }
    if (this.cfg.initCfg.fieldNameBlacklist && this.cfg.initCfg.fieldNameBlacklist.length) {
      payload['ClassInspectionRequest']['fieldNameBlacklist'] = { 'string': this.cfg.initCfg.fieldNameBlacklist };
    }
    if (this.cfg.initCfg.classNameBlacklist && this.cfg.initCfg.classNameBlacklist.length) {
      payload['ClassInspectionRequest']['classNameBlacklist'] = { 'string': this.cfg.initCfg.classNameBlacklist };
    }
    return payload;
  }

  parseDocumentResponse(responseJson: any, docDef: DocumentDefinition): void {
    if (docDef.type === DocumentType.JAVA) {
      if (typeof responseJson.ClassInspectionResponse !== 'undefined') {
        this.extractJavaDocumentDefinitionFromInspectionResponse(responseJson, docDef);
      } else if ((typeof responseJson.javaClass !== 'undefined')
        || (typeof responseJson.JavaClass !== 'undefined')) {
        this.extractJavaDocumentDefinition(responseJson, docDef);
      } else {
        this.handleError('Unknown Java inspection result format', responseJson);
      }
    } else if (docDef.type === DocumentType.JSON) {
      if (typeof responseJson.JsonInspectionResponse !== 'undefined') {
        this.extractJSONDocumentDefinitionFromInspectionResponse(responseJson, docDef);
      } else if ((typeof responseJson.jsonDocument !== 'undefined')
        || (typeof responseJson.JsonDocument !== 'undefined')) {
        this.extractJSONDocumentDefinition(responseJson, docDef);
      } else {
        this.handleError('Unknown JSON inspection result format', responseJson);
      }
    } else if (docDef.type === DocumentType.CSV) {
      if (typeof responseJson.CsvInspectionResponse !== 'undefined') {
        this.extractCSVDocumentDefinitionFromInspectionResponse(responseJson, docDef);
      } else if ((typeof responseJson.csvDocument !== 'undefined')
          || (typeof responseJson.csvDocument !== 'undefined')) {
        this.extractCSVDocumentDefinition(responseJson, docDef);
      } else {
        this.handleError('Unknown CSV inspection result format', responseJson);
      }
    } else {
      if (typeof responseJson.XmlInspectionResponse !== 'undefined') {
        this.extractXMLDocumentDefinitionFromInspectionResponse(responseJson, docDef);
      } else if ((typeof responseJson.xmlDocument !== 'undefined')
        || (typeof responseJson.XmlDocument !== 'undefined')) {
        this.extractXMLDocumentDefinition(responseJson, docDef);
      } else {
        this.handleError('Unknown XML inspection result format', responseJson);
      }
    }
    docDef.initializeFromFields();
  }

  private extractCSVDocumentDefinitionFromInspectionResponse(responseJson: any, docDef: DocumentDefinition): void {
    const body: any = responseJson.CsvInspectionResponse;
    if (body.errorMessage) {
      this.handleError('Could not load JSON document, error: ' + body.errorMessage, null);
      docDef.errorOccurred = true;
      return;
    }

    this.extractCSVDocumentDefinition(body, docDef);
  }

  private extractCSVDocumentDefinition(body: any, docDef: DocumentDefinition): void {
    let csvDocument: any;
    if (typeof body.csvDocument !== 'undefined') {
      csvDocument = body.csvDocument;
    } else {
      csvDocument = body.CsvDocument;
    }

    if (!docDef.description) {
      docDef.description = docDef.id;
    }
    if (!docDef.name) {
      docDef.name = docDef.description;
    }

    docDef.characterEncoding = csvDocument.characterEncoding;
    docDef.locale = csvDocument.locale;

    for (const field of csvDocument.fields.field) {
      this.parseCSVFieldFromDocument(field, null, docDef);
    }
  }

  private extractJSONDocumentDefinitionFromInspectionResponse(responseJson: any, docDef: DocumentDefinition): void {
    const body: any = responseJson.JsonInspectionResponse;
    if (body.errorMessage) {
      this.handleError('Could not load JSON document, error: ' + body.errorMessage, null);
      docDef.errorOccurred = true;
      return;
    }

    this.extractJSONDocumentDefinition(body, docDef);
  }

  private extractJSONDocumentDefinition(body: any, docDef: DocumentDefinition): void {
    let jsonDocument: any;
    if (typeof body.jsonDocument !== 'undefined') {
      jsonDocument = body.jsonDocument;
    } else {
      jsonDocument = body.JsonDocument;
    }

    if (!docDef.description) {
      docDef.description = docDef.id;
    }
    if (!docDef.name) {
      docDef.name = docDef.description;
    }

    docDef.characterEncoding = jsonDocument.characterEncoding;
    docDef.locale = jsonDocument.locale;

    for (const field of jsonDocument.fields.field) {
      this.parseJSONFieldFromDocument(field, null, docDef);
    }
  }

  private extractXMLDocumentDefinitionFromInspectionResponse(responseJson: any, docDef: DocumentDefinition): void {
    const body: any = responseJson.XmlInspectionResponse;
    if (body.errorMessage) {
      this.handleError('Could not load XML document, error: ' + body.errorMessage, null);
      docDef.errorOccurred = true;
      return;
    }

    this.extractXMLDocumentDefinition(body, docDef);
  }

  extractXMLDocumentDefinition(body: any, docDef: DocumentDefinition): void {
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
      docDef.name = docDef.description;
    }

    docDef.characterEncoding = xmlDocument.characterEncoding;
    docDef.locale = xmlDocument.locale;

    if (xmlDocument.xmlNamespaces && xmlDocument.xmlNamespaces.xmlNamespace
      && xmlDocument.xmlNamespaces.xmlNamespace.length) {
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

  private isSelectedRootElement(field: any, docDef: DocumentDefinition): boolean {
    return docDef.selectedRoot && field && field.name
      && docDef.selectedRoot === (field.name.indexOf(':') !== -1 ? field.name.split(':')[1] : field.name);
  }

  private extractJavaDocumentDefinitionFromInspectionResponse(responseJson: any, docDef: DocumentDefinition): void {
    const body: any = responseJson.ClassInspectionResponse;

    if (body.errorMessage) {
      this.handleError('Could not load Java document, error: ' + body.errorMessage, null);
      docDef.errorOccurred = true;
      return;
    }
    this.extractJavaDocumentDefinition(body, docDef);
  }

  private extractJavaDocumentDefinition(body: any, docDef: DocumentDefinition): void {
    const docIdentifier: string = docDef.id;
    const javaClass = body.JavaClass ? body.JavaClass : body.javaClass;
    if (!javaClass || javaClass.status === 'NOT_FOUND') {
      this.handleError('Could not load JAVA document. Document is not found: ' + docIdentifier, null);
      docDef.errorOccurred = true;
      return;
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
    if (javaClass.uri) {
      docDef.uri = javaClass.uri;
    }

    docDef.characterEncoding = javaClass.characterEncoding;
    docDef.locale = javaClass.locale;

    let rootField = null;
    if (javaClass.collectionType && javaClass.collectionType !== CollectionType.NONE.valueOf()) {
      this.parseJavaFieldFromDocument(javaClass, null, docDef);
      rootField = docDef.fields[0];
    }
    for (const field of javaClass.javaFields.javaField) {
      this.parseJavaFieldFromDocument(field, rootField, docDef);
    }
  }

  private parseCSVFieldFromDocument(field: any, parentField: Field | null, docDef: DocumentDefinition): void {
    const parsedField = this.parseFieldFromDocument(field, parentField, docDef);
    if (parsedField == null) {
      return;
    }

    if (field.csvFields && field.csvFields.csvField && field.csvFields.csvField.length) {
      for (const childField of field.csvFields.csvField) {
        this.parseCSVFieldFromDocument(childField, parsedField, docDef);
      }
    }
  }

  private parseJSONFieldFromDocument(field: any, parentField: Field | null, docDef: DocumentDefinition): void {
    const parsedField = this.parseFieldFromDocument(field, parentField, docDef);
    if (parsedField == null) {
      return;
    }

    if (field.jsonFields && field.jsonFields.jsonField && field.jsonFields.jsonField.length) {
      for (const childField of field.jsonFields.jsonField) {
        this.parseJSONFieldFromDocument(childField, parsedField, docDef);
      }
    }
  }

  private parseFieldFromDocument(field: any, parentField: Field | null, docDef: DocumentDefinition): Field | null {
    if (field != null && field.status === 'NOT_FOUND') {
      this.cfg.errorService.addError(new ErrorInfo({
        message: `Ignoring unknown field: ${field.name} (${field.className}), parent class: ${docDef.name}`,
        level: ErrorLevel.WARN, scope: ErrorScope.APPLICATION, type: ErrorType.USER}));
      return null;
    } else if (field != null && field.status === 'BLACK_LIST') {
      return null;
    }

    const parsedField: Field = new Field();
    parsedField.name = field.name;
    parsedField.type = field.fieldType;
    parsedField.path = field.path;
    parsedField.isPrimitive = field.fieldType !== 'COMPLEX';
    parsedField.serviceObject = field;
    parsedField.column = field.column;

    if ('LIST' === field.collectionType || 'ARRAY' === field.collectionType) {
      parsedField.isCollection = true;
      if ('ARRAY' === field.collectionType) {
        parsedField.isArray = true;
      }
    }

    if (parentField != null) {
      parentField.children.push(parsedField);
    } else {
      docDef.fields.push(parsedField);
    }

    return parsedField;
  }

  private parseXMLFieldFromDocument(field: any, parentField: Field | null, docDef: DocumentDefinition): void {
    const parsedField = this.parseFieldFromDocument(field, parentField, docDef);
    if (parsedField == null) {
      return;
    }

    if (field.name.indexOf(':') !== -1) {
      parsedField.namespaceAlias = field.name.split(':')[0];
      parsedField.name = field.name.split(':')[1];
    }

    parsedField.isAttribute = (parsedField.path.indexOf('@') !== -1);

    if (field.xmlFields && field.xmlFields.xmlField && field.xmlFields.xmlField.length) {
      for (const childField of field.xmlFields.xmlField) {
        this.parseXMLFieldFromDocument(childField, parsedField, docDef);
      }
    }
  }

  private parseJavaFieldFromDocument(field: any, parentField: Field | null, docDef: DocumentDefinition): void {
    const parsedField = this.parseFieldFromDocument(field, parentField, docDef);
    if (parsedField == null) {
      return;
    }

    // java fields have a special primitive property, so override the "!= COMPLEX" math from parseFieldFromDocument()
    parsedField.isPrimitive = field.primitive;
    parsedField.classIdentifier = field.className;
    parsedField.enumeration = field.enumeration;

    if (parsedField.enumeration && field.javaEnumFields && field.javaEnumFields.javaEnumField) {
      for (const enumValue of field.javaEnumFields.javaEnumField) {
        const parsedEnumValue: EnumValue = new EnumValue();
        parsedEnumValue.name = enumValue.name;
        parsedEnumValue.ordinal = enumValue.ordinal;
        parsedField.enumValues.push(parsedEnumValue);
      }
    }

    if (field.javaFields && field.javaFields.javaField && field.javaFields.javaField.length) {
      for (const childField of field.javaFields.javaField) {
        this.parseJavaFieldFromDocument(childField, parsedField, docDef);
      }
    }
  }

  private handleError(message: string, error: any): void {
    this.cfg.errorService.addError(new ErrorInfo({message: message, level: ErrorLevel.ERROR,
      scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
  }
}
