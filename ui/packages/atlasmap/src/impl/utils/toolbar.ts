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
import * as constants from '../../atlasmap.json';
import {
  ConfigModel,
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
} from '@atlasmap/core';
import { getDocDef, getDocDefByName, removeDocumentRef } from './document';

/**
 * Return true if the specified file object exists as a source or target
 * document, false otherwise.
 *
 * @param file - file object
 * @param isSource - source or target panel
 */
export function documentExists(file: File, isSource: boolean): boolean {
  const docDef = getDocDefByName(file.name, ConfigModel.getConfig(), isSource);
  return docDef ? true : false;
}

/**
 * Delete the specified source or target document.
 *
 * @param fileId - document ID to delete
 * @param isSource - source or target panel
 */
export async function deleteAtlasFile(fileId: string, isSource: boolean) {
  const cfg = ConfigModel.getConfig();
  const docDef = getDocDef(fileId, cfg, isSource);
  await removeDocumentRef(docDef, cfg);
  cfg.initializationService.updateInitComplete();
}

/**
 * The user has requested their current mappings be exported.  Use the mapping management
 * service to establish the file content and to push it down to the server.
 *
 * @param fileName - user-specified ADM archive file name
 */
export function exportADMArchiveFile(fileName: string) {
  const cfg = ConfigModel.getConfig();
  return cfg.fileService.exportADMArchive(fileName);
}

/**
 * Import an ADM archive file or a user JAR file.
 *
 * @param selectedFile
 * @param userFileSuffix
 * @param cfg
 */
export function importADMArchiveFile(selectedFile: File, cfg: ConfigModel) {
  cfg.initializationService.initializeWithADMArchiveFile(selectedFile);
}

/**
 * Import a user-defined JAR file.
 *
 * @param selectedFile - user selected JAR file
 * @param cfg - configuration model
 */
export function importJarFile(selectedFile: File, cfg: ConfigModel) {
  return new Promise<boolean>((resolve) => {
    cfg.fileService.importJarFile(selectedFile).then(() => {
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
}

/**
 * Remove all documents and imported JARs from the server.
 */
export function resetAtlasmap() {
  const cfg = ConfigModel.getConfig();
  cfg.initializationService.resetAtlasMap();
}

export function getRuntimeVersion(): Promise<string> {
  const cfg = ConfigModel.getConfig();
  return cfg.initializationService.getRuntimeVersion();
}

export function getUIVersion(): string {
  return constants.version;
}

export function toggleMappingPreview(enabled: boolean) {
  const cfg = ConfigModel.getConfig();
  return cfg.previewService.toggleMappingPreview(enabled);
}

export function toggleShowMappedFields(enabled: boolean) {
  const cfg = ConfigModel.getConfig();

  cfg.showMappedFields = enabled;
  cfg.initializationService.systemInitializedSource.next();

  return enabled;
}

export function toggleShowUnmappedFields(enabled: boolean) {
  const cfg = ConfigModel.getConfig();

  cfg.showUnmappedFields = enabled;
  cfg.initializationService.systemInitializedSource.next();

  return enabled;
}
