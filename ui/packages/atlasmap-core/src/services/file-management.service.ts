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
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
} from '../models/error.model';
import {
  IAtlasMappingContainer,
  IStringMapContainer,
} from '../contracts/mapping';
import { gzip, inflate } from 'pako';

import { ADMDigest } from '../contracts/adm-digest';
import { CommonUtil } from '../utils/common-util';
import { ConfigModel } from '../models/config.model';
import { HTTP_STATUS_NO_CONTENT } from '../common/config.types';
import { MappingDigestUtil } from '../utils/mapping-digest-util';
import ky from 'ky';
import log from 'loglevel';

export enum FileName {
  DIGEST = 'Mapping digest file',
  ADM = 'ADM archive file',
  JAR = 'Jar file',
}

export enum FileType {
  DIGEST = 'GZ',
  ADM = 'ZIP',
  JAR = 'JAR',
}

/**
 * Handles file manipulation stored in the backend, including import/export via UI.
 */
export class FileManagementService {
  _cfg!: ConfigModel;

  constructor(private api: typeof ky) {}

  get cfg() {
    return this._cfg;
  }

  set cfg(cfg: ConfigModel) {
    this._cfg = cfg;
    if (!this._cfg.logger) {
      this._cfg.logger = log.getLogger('file-management');
    }
  }

  findMappingFiles(filter: string): Promise<string[]> {
    return new Promise<string[]>((resolve, reject) => {
      const url =
        this.cfg.initCfg.baseMappingServiceUrl +
        'mappings' +
        (filter == null ? '' : '?filter=' + filter);
      this.cfg.logger!.debug('Mapping List Request');
      this.api
        .get(url)
        .json<IStringMapContainer>()
        .then((body) => {
          this.cfg.logger!.debug(
            `Mapping List Response: ${JSON.stringify(body)}`
          );
          const entries = body.StringMap.stringMapEntry;
          resolve(entries.map((item) => item.name));
        })
        .catch((error: any) => {
          if (error.status !== HTTP_STATUS_NO_CONTENT) {
            this.cfg.errorService.addBackendError(
              'Error occurred while accessing the current mapping files from the runtime service.',
              error
            );
            reject(error);
          }
        });
    });
  }

  /**
   * Retrieve the current user data mappings digest file from the server as a GZIP compressed byte array buffer.
   */
  getCurrentMappingDigest(): Promise<ADMDigest | null> {
    return new Promise<ADMDigest | null>((resolve, reject) => {
      this.getCurrentFile(FileName.DIGEST, FileType.DIGEST)
        .then((gzipped) => {
          if (!gzipped) {
            resolve(null);
            return;
          }
          const gunzipped = inflate(gzipped);
          const stringified = new Uint8Array(gunzipped).reduce(
            (data, byte) => data + String.fromCharCode(byte),
            ''
          );
          const admDigest = CommonUtil.objectize(stringified);
          resolve(admDigest);
        })
        .catch((error) => {
          reject(error);
        });
    });
  }

  getCurrentADMArchive(): Promise<Uint8Array | null> {
    return this.getCurrentFile(FileName.ADM, FileType.ADM);
  }

  private getCurrentFile(
    fileName: string,
    fileType: string
  ): Promise<Uint8Array | null> {
    return new Promise<Uint8Array | null>((resolve, reject) => {
      const url = `${this.cfg.initCfg.baseMappingServiceUrl}mapping/${fileType}/`;
      this.cfg.logger!.debug(`Get Current ${fileName} Request: ${url}`);
      const headers = {
        'Content-Type': 'application/octet-stream',
        Accept: 'application/octet-stream',
        'Response-Type': 'application/octet-stream',
      };
      this.api
        .get(url, { headers })
        .arrayBuffer()
        .then((body: ArrayBuffer) => {
          this.cfg.logger!.debug(
            `Get Current ${fileName} Response: ${JSON.stringify(body)}`
          );
          if (body.byteLength) {
            resolve(new Uint8Array(body));
          } else {
            resolve(null);
          }
        })
        .catch((error: any) => {
          if (error.status !== HTTP_STATUS_NO_CONTENT) {
            this.cfg.errorService.addBackendError(
              `Error occurred while accessing the ${fileName} from the runtime service.`,
              error
            );
            reject(error);
          }
        });
    });
  }

  /**
   * Delete mapping files on the runtime.
   */
  resetMappings(): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/RESET';
      this.cfg.logger!.debug('Reset Mappings Request');
      this.api
        .delete(url)
        .arrayBuffer()
        .then((res: any) => {
          this.cfg.logger!.debug(
            `Reset Mappings Response: ${JSON.stringify(res)}`
          );
          resolve(true);
          return res;
        })
        .catch((error: any) => {
          this.cfg.errorService.addBackendError(
            'Error occurred while resetting mappings.',
            error
          );
          resolve(false);
        });
    });
  }

  /**
   * Delete user-defined JAR library files on the runtime.
   */
  resetLibs(): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/resetLibs';
      this.cfg.logger!.debug('Reset Libs Request');
      this.api
        .delete(url)
        .arrayBuffer()
        .then((res: any) => {
          this.cfg.logger!.debug(`Reset Libs Response: ${JSON.stringify(res)}`);
          resolve(true);
          return res;
        })
        .catch((error: any) => {
          this.cfg.errorService.addBackendError(
            'Error occurred while resetting user-defined JAR libraries.',
            error
          );
          resolve(false);
        });
    });
  }

  /**
   * Clear error service and delete jar libraries, documents and mappings.
   */
  resetAll(): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.cfg.errorService.resetAll();
      this.cfg.fileService
        .resetMappings()
        .then(async () => {
          this.cfg.mappings = null;
          this.cfg.clearDocs();
          this.cfg.fileService
            .resetLibs()
            .then((value) => {
              resolve(value);
            })
            .catch((error) => {
              this.cfg.errorService.addBackendError(
                `Failed to remove jar files: ${error.message}`,
                error
              );
              resolve(false);
            });
        })
        .catch((error) => {
          this.cfg.errorService.addBackendError(
            `Failed to remove mappings: ${error.message}`,
            error
          );
          resolve(false);
        });
    });
  }

  /**
   * Commit the specified AtlasMapping object to the runtime service.  The mappings
   * are kept separate so they can be updated with minimal overhead.
   *
   * @param buffer - The stringified AtlasMapping JSON
   */
  setMappingToService(
    atlasMappingContainer: IAtlasMappingContainer
  ): Promise<boolean> {
    const jsonBuffer = JSON.stringify(atlasMappingContainer);
    return this.setMappingStringToService(jsonBuffer);
  }

  /**
   * Commit the specified AtlasMapping JSON user mapping string to the runtime service.  The mappings
   * are kept separate so they can be updated with minimal overhead.
   *
   * @param buffer - The stringified AtlasMapping JSON
   */
  setMappingStringToService(jsonBuffer: string): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/JSON';
      const headers = {
        'Content-Type': 'application/json',
        Accept: 'application/json',
        'Response-Type': 'application/json',
      };
      this.cfg.logger!.debug(
        `Set Mapping Request (set mapping): ${jsonBuffer}`
      );
      this.api
        .put(url, { headers, body: jsonBuffer })
        .arrayBuffer()
        .then((res) => {
          this.cfg.logger!.debug(
            `Set Mapping Response: ${JSON.stringify(res)}`
          );
          resolve(true);
        })
        .catch((error: any) => {
          this.cfg.errorService.addBackendError(
            `Unable to update the mappings file to the AtlasMap design runtime service. ${error.status} ${error.statusText}`,
            error
          );
          resolve(false);
        });
    });
  }

  setMappingDigestToService(mappingDigest: ADMDigest): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      // Compress the JSON buffer - write out as binary.
      const strBuffer = JSON.stringify(mappingDigest);
      const binBuffer = CommonUtil.str2bytes(strBuffer);
      let compressedBuffer: Uint8Array;
      try {
        compressedBuffer = gzip(binBuffer);
      } catch (error1) {
        this.cfg.errorService.addError(
          new ErrorInfo({
            message: 'Unable to compress the current data mappings.',
            level: ErrorLevel.ERROR,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.INTERNAL,
            object: error1,
          })
        );
        resolve(false);
        return;
      }
      // Update .../target/mappings/adm-catalog-files.gz
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/GZ/0';
      const fileContent: Blob = new Blob([compressedBuffer], {
        type: 'application/octet-stream',
      });
      this.setBinaryFileToService(fileContent, url, FileName.DIGEST).then(
        (value) => {
          resolve(value);
        }
      );
    });
  }

  private setADMArchiveFileToService(
    compressedBuffer: BlobPart
  ): Promise<boolean> {
    const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/ZIP/';
    const fileContent: Blob = new Blob([compressedBuffer], {
      type: 'application/octet-stream',
    });
    return this.setBinaryFileToService(fileContent, url, FileName.ADM);
  }

  /**
   * The user has either exported their mappings or imported new mappings.
   * Either way we're saving them on the server.
   *
   * @param compressedBuffer
   */
  private setBinaryFileToService(
    compressedBuffer: BlobPart,
    url: string,
    fileName: FileName
  ): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.cfg.logger!.debug(`Set ${fileName} Request`);
      this.api
        .put(url, { body: compressedBuffer })
        .arrayBuffer()
        .then((res) => {
          this.cfg.logger!.debug(
            `Set ${fileName} Response: ${JSON.stringify(res)}`
          );
          resolve(true);
        })
        .catch((error: any) => {
          this.cfg.errorService.addBackendError(
            `Unable to update the ${fileName} to the AtlasMap design runtime service.
              ${error.status} ${error.statusText}`,
            error
          );
          resolve(false);
        });
    });
  }

  /**
   * Push a user-defined Java archive file (JAR binary buffer) to the runtime.
   *
   * @param binaryBuffer - binary JAR file
   */
  importJarFile(binaryBuffer: BlobPart): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'library';
      const fileContent: Blob = new Blob([binaryBuffer], {
        type: 'application/octet-stream',
      });
      const jarUpdated = await this.setBinaryFileToService(
        fileContent,
        url,
        FileName.JAR
      );
      if (jarUpdated && this.cfg.mappingService) {
        this.cfg.mappingService.notifyMappingUpdated();
        await this.cfg.fieldActionService.fetchFieldActions();
        resolve(true);
      } else {
        resolve(false);
      }
    });
  }

  /**
   * Generate mapping digest file from current state and push it to the runtime.
   *
   * @returns {@link Promise}
   */
  updateDigestFile(): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
      try {
        let mappingJson = undefined;
        // Retrieve the JSON mappings buffer from the server.
        if (this.cfg.mappings) {
          mappingJson = await this.getCurrentMappingJson();
        }
        const mappingDigest = MappingDigestUtil.generateMappingDigest(
          this.cfg,
          mappingJson
        );

        // Save mapping digest file to the runtime.
        this.setMappingDigestToService(mappingDigest).then((value) => {
          resolve(value);
        });
      } catch (error) {
        this.cfg.errorService.addError(
          new ErrorInfo({
            message: 'Unable to update mapping digest file.',
            level: ErrorLevel.ERROR,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.INTERNAL,
            object: error,
          })
        );
        resolve(false);
        return;
      }
    });
  }

  /**
   * Update the current mapping files and export the ADM archive file with current mappings.
   *
   * Establish the mapping digest file content in JSON format (mappings + schema + instance-schema),
   * compress it (GZIP), update the runtime, then fetch the full ADM archive ZIP file from the runtime
   * and export it.
   *
   * @param event
   */
  exportADMArchive(mappingsFileName: string): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.updateDigestFile().then(() => {
        // Fetch the full ADM archive file from the runtime (ZIP) and export it to to the local
        // downloads area.
        this.getCurrentADMArchive().then(async (value: Uint8Array | null) => {
          // If value is null then no compressed mappings digest file is available on the server.
          if (value === null) {
            resolve(false);
            return;
          }
          // Tack on a .adm suffix if one wasn't already specified.
          if (mappingsFileName.split('.').pop() !== 'adm') {
            mappingsFileName = mappingsFileName.concat('.adm');
          }
          const fileContent = new Blob([value], {
            type: 'application/octet-stream',
          });
          CommonUtil.writeFile(fileContent, mappingsFileName)
            .then((value2) => {
              resolve(value2);
            })
            .catch((error) => {
              this.cfg.errorService.addError(
                new ErrorInfo({
                  message: 'Unable to save the current data mappings.',
                  level: ErrorLevel.ERROR,
                  scope: ErrorScope.APPLICATION,
                  type: ErrorType.INTERNAL,
                  object: error,
                })
              );
              resolve(false);
            });
        });
      });
    });
  }

  /**
   * Clean up all existing mappings, documents, libraries and import specified ADM archive file,
   * push it to the runtime and reflect back in UI. The ADM file is in (ZIP) file format.
   * Once pushed, we can retrieve from runtime the extracted compressed (GZIP) mappings
   * digest file as well as the mappings JSON file.  These files exist separately for performance reasons.
   *
   * Once the runtime has its ADM archive file, digest file and mappings file set then restart the DM.
   *
   * @param mappingsFileName - ADM archive file
   */
  importADMArchive(admFile: File): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.resetAll().then(() => {
        const reader = new FileReader();

        // Turn the imported ADM file into a binary octet stream.
        CommonUtil.readBinaryFile(admFile, reader)
          .then((fileBin) => {
            // Push the binary stream to the runtime.
            this.setADMArchiveFileToService(fileBin).then((value) => {
              resolve(value);
            });
          })
          .catch((error) => {
            this.cfg.errorService.addError(
              new ErrorInfo({
                message: `Unable to import the specified ADM file '${admFile.name}'`,
                level: ErrorLevel.ERROR,
                scope: ErrorScope.APPLICATION,
                type: ErrorType.INTERNAL,
                object: error,
              })
            );
            resolve(false);
          });
      });
    });
  }

  /**
   * Retrieve the current user AtlasMap data mappings from the server as a JSON object.
   */
  getCurrentMappingJson(): Promise<IAtlasMappingContainer> {
    return new Promise<any>((resolve, reject) => {
      if (this.cfg.mappings === null) {
        reject();
        return;
      }
      this.cfg.mappingFiles[0] = this.cfg.mappings.name!;
      const baseURL: string =
        this.cfg.initCfg.baseMappingServiceUrl + 'mapping/JSON/';
      this.cfg.logger!.debug('Get Current Mapping Request');
      this.api
        .get(baseURL)
        .json<IAtlasMappingContainer>()
        .then((body) => {
          this.cfg.logger!.debug(
            `Get Current Mapping Response: ${JSON.stringify(body)}`
          );
          resolve(body);
        })
        .catch((error: any) => {
          if (error.status !== HTTP_STATUS_NO_CONTENT) {
            this.cfg.errorService.addBackendError(
              'Error occurred while accessing the current mappings from the backend service.',
              error
            );
            reject(error);
          } else {
            resolve(undefined);
          }
        });
    });
  }
}
