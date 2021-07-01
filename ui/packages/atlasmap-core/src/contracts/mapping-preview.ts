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
import { IMapping } from './mapping';
import { MODEL_PACKAGE_PREFIX } from './common';

/**
 * The mapping Data model contracts between frontend and backend.
 */

export const PROCESS_MAPPING_REQUEST_JSON_TYPE =
  MODEL_PACKAGE_PREFIX + '.ProcessMappingRequest';

/**
 * The root object that carries {@link IProcessMappingRequest}
 * when it's sent to backend.
 */
export interface IProcessMappingRequestContainer {
  ProcessMappingRequest: IProcessMappingRequest;
}

/**
 * The serialized process mapping request object.
 */
export interface IProcessMappingRequest {
  jsonType: string;
  mapping: IMapping;
}

/**
 * The root object that carries {@link IProcessMappingResponse}
 * when it's received from backend.
 */
export interface IProcessMappingResponseContainer {
  ProcessMappingResponse: IProcessMappingResponse;
}

/**
 * The serialized process mapping response object.
 */
export interface IProcessMappingResponse {
  jsonType: string;
  mapping: IMapping;
  audits: IAudits;
}

/**
 * The container of serialized {@link IAudit}.
 */
export interface IAudits {
  audit: IAudit[];
}

/**
 * The serialized audit object which is logged during processing a mapping.
 */
export interface IAudit {
  message?: string;
  docId?: string;
  docName?: string;
  path?: string;
  value?: string;
  status: AuditStatus;
}

export enum AuditStatus {
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR',
}
