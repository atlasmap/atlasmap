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
  CommonUtil,
  ConfigModel,
  DocumentDefinition,
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
  NamespaceModel,
} from '@atlasmap/core';

/**
 * Modify the document name of the document specified by the document ID.
 *
 * @param docId
 * @param newDocName
 * @param isSource
 */
export async function changeDocumentName(
  docId: string,
  newDocName: string,
  isSource: boolean,
) {
  const cfg = ConfigModel.getConfig();
  const docDef = getDocDef(docId, cfg, isSource);
  docDef.name = newDocName;
  await cfg.mappingService.notifyMappingUpdated();
  await cfg.fileService.updateDigestFile();
}

/**
 * Create a new namespace for the supplied XML document.
 *
 * @param docName
 * @param alias
 * @param uri
 * @param locationUri
 */
export function createNamespace(
  docName: string,
  alias: string,
  uri: string,
  locationUri: string,
  isTarget: boolean,
) {
  const cfg = ConfigModel.getConfig();
  const docDef = getDocDefByName(docName, cfg, true);
  const namespace: NamespaceModel = {
    alias: alias,
    uri: uri,
    locationUri: locationUri,
    createdByUser: true,
    isTarget: isTarget,
    getPrettyLabel: () => alias + ' [' + uri + ']',
    copy: () => Object.assign({}, namespace),
    copyFrom: (n: NamespaceModel) => Object.assign(namespace, n),
  };
  docDef.namespaces.push(namespace);
  cfg.mappingService.notifyMappingUpdated();
}

export function editNamespace(
  docName: string,
  initAlias: string,
  alias: string,
  uri: string,
  locationUri: string,
  isTarget: boolean,
) {
  const cfg = ConfigModel.getConfig();
  const docDef = getDocDefByName(docName, cfg, true);
  const namespace = docDef.getNamespaceForAlias(initAlias);
  namespace.alias = alias;
  namespace.uri = uri;
  namespace.locationUri = locationUri;
  namespace.isTarget = isTarget;
  cfg.mappingService.notifyMappingUpdated();
}

export function deleteNamespace(docName: string, alias: string) {
  const cfg = ConfigModel.getConfig();
  const docDef = getDocDefByName(docName, cfg, true);
  docDef.namespaces = docDef.namespaces.filter(
    (namespace: { alias: string }) => namespace.alias !== alias,
  );
  cfg.mappingService.notifyMappingUpdated();
}

/**
 * Remove a document from the UI and backend service.
 *
 * @param docDef
 * @param cfg
 */
export async function removeDocumentRef(
  docDef: DocumentDefinition,
  cfg: ConfigModel,
): Promise<boolean> {
  return new Promise<boolean>(async (resolve) => {
    cfg.mappingService.removeDocumentReferenceFromAllMappings(docDef.id);
    if (docDef.isSource) {
      CommonUtil.removeItemFromArray(docDef, cfg.sourceDocs);
    } else {
      CommonUtil.removeItemFromArray(docDef, cfg.targetDocs);
    }
    await cfg.mappingService.notifyMappingUpdated();
    await cfg.fileService.updateDigestFile();
    resolve(true);
  });
}

/**
 * Return the document definition associated with the specified document ID.
 *
 * @param docId - document ID
 * @param cfg
 * @param isSource
 */
export function getDocDef(
  docId: string,
  cfg: ConfigModel,
  isSource: boolean,
): DocumentDefinition {
  for (const docDef of cfg.getDocs(isSource)) {
    if (docDef.id.match(docId)) {
      return docDef;
    }
  }
  return null as unknown as DocumentDefinition;
}

/**
 * Return the document definition associated with the specified document name.
 *
 * @param docName - document name
 * @param cfg
 * @param isSource
 */
export function getDocDefByName(
  docName: string,
  cfg: ConfigModel,
  isSource: boolean,
): DocumentDefinition {
  for (const docDef of cfg.getDocs(isSource)) {
    const candidateDocName =
      docDef.getName(false) + '.' + docDef.type.toLowerCase();
    if (candidateDocName.match(docName)) {
      return docDef;
    }
  }
  return null as unknown as DocumentDefinition;
}

/**
 * Determine the user-defined class names associated with previously
 * imported JARs.
 */
export async function getCustomClassNameOptions(): Promise<string[]> {
  return new Promise<string[]>(async (resolve, reject) => {
    const cfg = ConfigModel.getConfig();
    cfg.documentService
      .getLibraryClassNames()
      .then((classNames: string[]) => {
        resolve(classNames);
      })
      .catch(() => {
        reject();
      });
  });
}

/**
 * Import a CSV, instance or schema document into either the Source panel or Target
 * panel (CSV, JSON, XML, XSD).
 *
 * @param selectedFile
 * @param cfg
 * @param isSource
 * @param isSchema - user-specified instance/ schema (true === schema)
 * @param inspectionParameters - CSV parameters
 */
export async function importInstanceSchema(
  selectedFile: File,
  cfg: ConfigModel,
  isSource: boolean,
  isSchema: boolean,
  inspectionParameters?: { [key: string]: string },
) {
  return new Promise<boolean>(async (resolve) => {
    cfg.initCfg.initialized = false;
    cfg.initializationService.updateLoadingStatus(
      'Importing Document ' + selectedFile.name,
    );
    cfg.documentService
      .importNonJavaDocument(
        selectedFile,
        isSource,
        isSchema,
        inspectionParameters,
      )
      .then(() => {
        cfg.fileService.updateDigestFile().finally(() => {
          cfg.initializationService.updateStatus();
          cfg.errorService.addError(
            new ErrorInfo({
              message: `${selectedFile.name} import complete.`,
              level: ErrorLevel.INFO,
              scope: ErrorScope.APPLICATION,
              type: ErrorType.USER,
            }),
          );
          resolve(true);
        });
      });
  });
}

/**
 * Enable the specified class name and collection type in the targetted
 * panel for use in Java document loading. The user must have previously
 * imported a JAR file containing the class.
 *
 * @param selectedClass
 * @param selectedCollection
 * @param isSource
 */
export function enableCustomClass(
  cfg: ConfigModel,
  selectedClass: string,
  selectedCollection: string,
  isSource: boolean,
) {
  return new Promise<boolean>((resolve) => {
    cfg.initCfg.initialized = false;
    cfg.initializationService.updateLoadingStatus(
      'Importing Document ' + selectedClass,
    );
    cfg.documentService
      .importJavaDocument(
        selectedClass,
        isSource,
        selectedCollection as CollectionType,
      )
      .then(() => {
        cfg.fileService.updateDigestFile().finally(() => {
          cfg.initializationService.updateStatus();
          resolve(true);
        });
      });
  });
}

export function getPropertyScopeOptions(isSource: boolean): {
  value: string;
  label: string;
}[] {
  const cfg = ConfigModel.getConfig();
  let scopeOptions: {
    value: string;
    label: string;
  }[] = [
    {
      label: 'Current Message Header',
      value: 'current',
    },
    {
      label: 'Camel Exchange Property',
      value: 'camelExchangeProperty',
    },
  ];
  const propertyDocOptions: DocumentDefinition[] = isSource
    ? cfg.sourceDocs
    : cfg.targetDocs;
  for (let propertyDocOption of propertyDocOptions) {
    scopeOptions.push({
      value: propertyDocOption.id,
      label: propertyDocOption.name,
    });
  }
  return scopeOptions;
}

/**
 * Return CSV document inspection parameters.
 *
 * @param docId
 * @param isSource
 * @returns
 */
export function getDocCSVParams(
  docId: string,
  isSource: boolean,
): { [key: string]: string } {
  const doc = getDocDef(docId, ConfigModel.getConfig(), isSource);
  return doc?.inspectionParameters;
}

/**
 * Set CSV document inspection parameters.
 *
 * @param docId
 * @param isSource
 * @param parameters
 */
export async function setDocCSVParams(
  docId: string,
  isSource: boolean,
  parameters: { [key: string]: string },
) {
  const cfg = ConfigModel.getConfig();
  const docDef = getDocDef(docId, cfg, isSource);
  docDef.inspectionParameters = parameters;
  await cfg.mappingService.notifyMappingUpdated();
  await cfg.fileService.updateDigestFile();
}
