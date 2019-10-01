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
import { Injectable } from '@angular/core';

import { ConfigModel } from '../models/config.model';
import { NGXLogger } from 'ngx-logger';
import { deflate } from 'pako';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { DataMapperUtil } from '../common/data-mapper-util';
import { map, timeout } from 'rxjs/operators';
import { DocumentManagementService } from './document-management.service';
import { InspectionType } from '../common/config.types';

/**
 * Handles file manipulation stored in the backend, including import/export via UI.
 */
@Injectable()
export class FileManagementService {
  _cfg: ConfigModel;

  private headers = new HttpHeaders(
    {'Content-Type': 'application/json; application/octet-stream',
     'Accept':       'application/json; application/octet-stream'});
  private jsonBuffer: string;

  constructor(private logger: NGXLogger, private http: HttpClient) {}

  get cfg() {
    return this._cfg;
  }

  set cfg(cfg: ConfigModel) {
    this._cfg = cfg;
    if (!this._cfg.logger) {
      this._cfg.logger = this.logger;
    }
  }

  findMappingFiles(filter: string): Observable<string[]> {
    return new Observable<string[]>((observer: any) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mappings' + (filter == null ? '' : '?filter=' + filter);
      this.cfg.logger.trace('Mapping List Request');
      this.http.get(url, { headers: this.headers }).toPromise().then((body: any) => {
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Mapping List Response: ${JSON.stringify(body)}`);
        }
        const entries: any[] = body.StringMap.stringMapEntry;
        const mappingFileNames: string[] = [];
        for (const entry of entries) {
          mappingFileNames.push(entry.name);
        }
        observer.next(mappingFileNames);
        observer.complete();
      }).catch((error: any) => {
        if (error.status !== DataMapperUtil.HTTP_STATUS_NO_CONTENT) {
          this.handleError('Error occurred while accessing the current mapping files from the runtime service.', error);
          observer.error(error);
        }
        observer.complete();
      });
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
  }

  /**
   * Retrieve the current user data mappings catalog from the server as a GZIP compressed byte array buffer.
   */
  getCurrentMappingCatalog(): Observable<Uint8Array> {
    const catalogName = 'adm-catalog-files.gz';
    return new Observable<Uint8Array>((observer: any) => {
      const baseURL: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/GZ/';
      const url: string = baseURL + catalogName;
      this.cfg.logger.trace('Mapping Catalog Request');
      const catHeaders = new HttpHeaders(
        { 'Content-Type':  'application/octet-stream',
          'Accept':        'application/octet-stream',
          'Response-Type': 'application/octet-stream'
        });
      this.http.get(url, { headers: catHeaders, responseType: 'arraybuffer' }).toPromise().then((body: any) => {
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Mapping Catalog Response: ${JSON.stringify(body)}`);
        }
        observer.next(body);
        observer.complete();
      }).catch((error: any) => {
        if (error.status !== DataMapperUtil.HTTP_STATUS_NO_CONTENT) {
          this.handleError('Error occurred while accessing the current mappings catalog from the runtime service.', error);
          observer.error(error);
        }
        observer.complete();
      });
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
  }

  getCurrentADMCatalog(): Observable<Uint8Array> {
    const atlasmapCatalogName = 'atlasmap-catalog.adm';
    return new Observable<Uint8Array>((observer: any) => {
      const baseURL: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/ZIP/';
      const url: string = baseURL + atlasmapCatalogName;
      this.cfg.logger.trace('Mapping Catalog Request');
      const catHeaders = new HttpHeaders(
        { 'Content-Type':  'application/octet-stream',
          'Accept':        'application/octet-stream',
          'Response-Type': 'application/octet-stream'
        });
      this.http.get(url, { headers: catHeaders, responseType: 'arraybuffer' }).toPromise().then((body: any) => {
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Mapping Catalog Response: ${JSON.stringify(body)}`);
        }
        observer.next(body);
        observer.complete();
      }).catch((error: any) => {
        if (error.status !== DataMapperUtil.HTTP_STATUS_NO_CONTENT) {
          this.handleError('Error occurred while accessing the ADM catalog from the runtime service.', error);
          observer.error(error);
        }
        observer.complete();
      });
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
  }

  /**
   * Establish an observable function to delete mapping files on the runtime.
   */
  resetMappings(): Observable<boolean> {
    return new Observable<boolean>((observer: any) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/RESET';
      this.cfg.logger.trace('Mapping Service Request - Reset');
      this.http.delete(url, { headers: this.headers }).toPromise().then((res: any) => {
          if (this.cfg.isTraceEnabled()) {
            this.cfg.logger.trace(`Mapping Service Response - Reset: ${JSON.stringify(res)}`);
          }
          observer.next(true);
          observer.complete();
          return res;
        })
        .catch((error: any) => {
          this.handleError('Error occurred while resetting mappings.', error); },
      );
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
  }

  /**
   * Establish an observable function to delete user-defined JAR library files on the runtime.
   */
  resetLibs(): Observable<boolean> {
    return new Observable<boolean>((observer: any) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/resetLibs';
      this.cfg.logger.trace('Mapping Service Request - Reset User-Defined Libraries');
      this.http.delete(url, { headers: this.headers }).toPromise().then((res: any) => {
          if (this.cfg.isTraceEnabled()) {
            this.cfg.logger.trace(`Mapping Service Response - Reset Libs: ${JSON.stringify(res)}`);
          }
          observer.next(true);
          observer.complete();
          return res;
        })
        .catch((error: any) => {
          this.handleError('Error occurred while resetting user-defined JAR libraries.', error); },
      );
    }).pipe(timeout(this.cfg.initCfg.admHttpTimeout));
  }

 /**
  * Commit the specified AtlasMapping JSON user mapping string to the runtime service.  The mappings
  * are kept separate so they can be updated with minimal overhead.
  *
  * @param buffer - JSON content
  */
  setMappingToService(jsonBuffer: string): Observable<boolean> {
    return new Observable<boolean>((observer: any) => {
      const url = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/JSON/' + this.getMappingId();
      this.cfg.logger.trace('Mapping Service Request');
      this.http.put(url, jsonBuffer, { headers: this.headers }).toPromise().then((res: any) => {
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Mapping Service Response: ${JSON.stringify(res)}`);
        }
        observer.next(true);
        observer.complete();
      })
      .catch((error: any) => {
        this.handleError('Error occurred while establishing mappings from an imported JSON.', error);
        observer.error(error);
        observer.complete();
      });
    });
  }

  /**
   * The user has either exported their mappings or imported new mappings.  Either way we're saving them on the server.
   *
   * @param compressedBuffer
   */
   setBinaryFileToService(compressedBuffer: any, url: string): Observable<boolean> {
     return new Observable<boolean>((observer: any) => {
       this.cfg.logger.trace('Set Compressed Mapping Service Request');
       this.http.put(url, compressedBuffer, { headers: this.headers }).toPromise().then((res: any) => {
          if (this.cfg.isTraceEnabled()) {
            this.cfg.logger.trace(`Set Compressed Mapping Service Response: ${JSON.stringify(res)}`);
          }
          observer.next(true);
          observer.complete();
       })
      .catch((error: any) => {
        this.handleError('Error occurred while saving mapping.', error);
        observer.error(error);
        observer.complete();
      });
    });
   }

  /**
   * Update the current mapping files and export the current mappings catalog (ADM).
   *
   * Establish the file content in JSON format (mappings + schema + instance-schema), compress
   * it (GZIP), update the runtime, then fetch the full ADM catalog ZIP file from the runtime
   * and export it.
   *
   * @param event
   */
  async exportMappingsCatalog(mappingsFileName: string): Promise<boolean> {
    return new Promise<boolean>(async(resolve, reject) => {
      let aggregateBuffer = '   {\n';
      let userExport = true;

      try {
        if (mappingsFileName === null || mappingsFileName.length === 0) {
          mappingsFileName = 'atlasmap-mapping.adm';
          userExport = false;
        }

        // Retrieve the JSON mappings buffer from the server.
        if (await this.getJsonBuf()) {
          aggregateBuffer += DocumentManagementService.generateExportMappings(this.jsonBuffer[0]);
        }

        let exportMeta = '   "exportMeta": [\n';
        let exportBlockData = '      "exportBlockData": [\n';
        let docCount = 0;

        // Establish two string arrays:
        //   exportMeta - meta-data describing the instance or schema documents.
        //   exportBlockData - the actual source of the instance/schema/mappings documents or the Java class name.
        for (const doc of this.cfg.getAllDocs()) {
          if (doc.inspectionSource !== null &&
               (doc.inspectionType === InspectionType.INSTANCE) || (doc.inspectionType === InspectionType.SCHEMA) ||
                 (doc.inspectionType === InspectionType.JAVA_CLASS) ) {
            if (docCount > 0) {
              exportMeta += ',\n';
              exportBlockData += ',\n';
            }
            exportMeta += DocumentManagementService.generateExportMetaStr(doc);
            exportBlockData += DocumentManagementService.generateExportBlockData(doc.inspectionSource);
            docCount++;
          }
        }
        exportMeta += '   ],\n';
        exportBlockData += '   ]\n';
        aggregateBuffer += exportMeta;
        aggregateBuffer += exportBlockData;
        aggregateBuffer += '   }\n';

        // Compress the JSON buffer - write out as binary.
        const binBuffer = DataMapperUtil.str2bytes(aggregateBuffer);
        try {
          const compress = deflate(binBuffer, {gzip: true});
          let fileContent: Blob = new Blob([compress], {type: 'application/octet-stream'});

          // Save the model mappings to the runtime.
          this.setBinaryFileToService(fileContent,
            this.cfg.initCfg.baseMappingServiceUrl + 'mapping/GZ/' + this.getMappingId()).toPromise()
            .then(async(result: boolean) => {

            // Fetch the full ADM catalog file from the runtime (ZIP) and export it to to the local
            // downloads area.
            if (userExport) {

              this.getCurrentADMCatalog().subscribe( async(value: Uint8Array) => {

                // If value is null then no compressed mappings catalog is available on the server.
                if (value !== null) {
                  fileContent = new Blob([value], {type: 'application/octet-stream'});
                  if (!await DataMapperUtil.writeFile(fileContent, mappingsFileName)) {
                    this.cfg.errorService.mappingError('Unable to save the current data mappings.', null);
                  }
                }
                resolve(true);
              });
            }
            resolve(true);
          }).catch((error: any) => {
            if (error.status === 0) {
              this.cfg.errorService.mappingError(
                'Fatal network error: Unable to connect to the AtlasMap design runtime service.', error);
            } else {
              this.cfg.errorService.mappingError(
                'Unable to update the catalog mappings file to the AtlasMap design runtime service.  ' +
                  error.status + ' ' + error.statusText, error);
            }
          });
        } catch (error1) {
          this.cfg.errorService.mappingError('Unable to compress the current data mappings.\n', error1);
          return;
        }
      } catch (error) {
        this.cfg.errorService.mappingError('Unable to export the current data mappings.', error);
        return;
      }
    });
  }

  /**
   * Perform a binary read of the specified catalog (.ADM) file and push it to the runtime.  The ADM file is
   * in (ZIP) file format.  Once pushed, we can retrieve from runtime the extracted compressed (GZIP) mappings
   * file catalog as well as the mappings JSON file.  These files exist separately for performance reasons.
   *
   * Once the runtime has its ADM catalog, catalog files and mappings file set then restart the DM.
   *
   * @param mappingsFileName - ADM master ZIP catalog
   */
  async importADMCatalog(mappingsFileName: string): Promise<boolean> {
    return new Promise<boolean>(async(resolve, reject) => {
      let fileBin = null;
      const reader = new FileReader();

      // Turn the imported ADM file into a binary octet stream.
      try {
        fileBin = await DataMapperUtil.readBinaryFile(mappingsFileName, reader);
      } catch (error) {
        this.cfg.errorService.mappingError('Unable to import the specified catalog file \'' + mappingsFileName + '\'', error);
        return;
      }
      const fileContent: Blob = new Blob([fileBin], {type: 'application/octet-stream'});

      // Push the binary stream to the runtime.
      this.setBinaryFileToService(fileContent, this.cfg.initCfg.baseMappingServiceUrl +
        'mapping/ZIP/' + this.getMappingId()).toPromise().then( async(result: boolean) => {

        try {
          this.cfg.mappings = null;
          this.cfg.clearDocs();
          await this.cfg.initializationService.initialize();
        } catch (error) {
          this.cfg.errorService.mappingError('Unable to import the catalog file: \n' + mappingsFileName +
            '\n' + error.message, error);
          return;
        }
      }).catch((error: any) => {
        if (error.status === 0) {
          this.cfg.errorService.mappingError(
            'Fatal network error: Unable to connect to the AtlasMap design runtime service.', error);
        } else {
          this.cfg.errorService.mappingError(
            'Unable to send the ADM file to the runtime service.  ' + error.status + ' ' + error.statusText, error);
        }
      });
      resolve(true);
    });
  }

  /**
   * Asynchronously retrieve the current user-defined AtlasMap mappings from the runtime server as an JSON buffer.
   */
  private async getJsonBuf(): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      if (this.cfg.mappings === null) {
        resolve(false);
      }
      this.cfg.mappingFiles[0] = this.cfg.mappings.name;
      this.getCurrentMappingJson().toPromise().then((result: string) => {
        this.jsonBuffer = result;
        resolve(true);
      }).catch((error: any) => {
        if (error.status === 0) {
          this.cfg.errorService.mappingError(
            'Fatal network error: Unable to connect to the AtlasMap design runtime service.', error);
        } else {
          this.cfg.errorService.mappingError(
            'Unable to access current mapping definitions: ' + error.status + ' ' + error.statusText, error);
        }
        resolve(false);
      });
    });
  }

  /**
   * Retrieve the current user AtlasMap data mappings from the server as an JSON buffer.
   */
  private getCurrentMappingJson(): Observable<string> {
    const mappingFileNames: string[] = this.cfg.mappingFiles;
    return new Observable<string>((observer: any) => {
      const baseURL: string = this.cfg.initCfg.baseMappingServiceUrl + 'mapping/JSON/';
      const operations: Observable<any>[] = [];
      for (const mappingName of mappingFileNames) {
        const url: string = baseURL + mappingName;
        this.cfg.logger.trace('Mapping Service Request');
        const jsonHeaders = new HttpHeaders(
          { 'Content-Type':  'application/json',
            'Accept':        'application/json',
            'Response-Type': 'application/json'
          });
        const operation = this.http.get(url, { headers: jsonHeaders, responseType: 'text' }).pipe(map((res: any) => res ));
        operations.push(operation);
      }

      forkJoin(operations).toPromise().then((data: string[]) => {
        if (!data) {
          observer.next('no data');
          observer.complete();
          return;
        }
        if (this.cfg.isTraceEnabled()) {
          this.cfg.logger.trace(`Mapping Service Response: ${JSON.stringify(data)}`);
        }
        observer.next(data);
        observer.complete();
      }).catch((error: any) => {
        observer.error(error);
        observer.complete();
      });
    });
  }

  private getMappingId(): string {
    return (this.cfg.mappingFiles.length > 0) ? this.cfg.mappingFiles[0] : '0';
  }

  private handleError(message: string, error: any): void {
    this.cfg.errorService.mappingError(message, error);
    this.cfg.initCfg.initialized = true;
  }

}
