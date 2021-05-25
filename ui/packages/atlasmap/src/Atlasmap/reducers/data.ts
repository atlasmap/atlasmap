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
import { IAtlasmapDocument, IAtlasmapMapping } from '../../Views';

import { IAtlasmapField } from './../../Views/models';

export interface DataAction {
  type: 'reset' | 'loading' | 'update' | 'error';
  payload?: DataActionPayload;
}

export interface DataActionPayload {
  pending?: boolean;
  error?: boolean;
  sources?: IAtlasmapDocument[];
  targets?: IAtlasmapDocument[];
  constants?: IAtlasmapDocument | null;
  mappings?: IAtlasmapMapping[];
  selectedMapping?: IAtlasmapMapping | null;
  sourcesFilter?: string;
  sourceProperties?: IAtlasmapDocument | null;
  targetsFilter?: string;
  targetProperties?: IAtlasmapDocument | null;
  flatSources?: IAtlasmapField[];
  flatTargets?: IAtlasmapField[];
}

export interface IDataState {
  pending: boolean;
  error: boolean;
  sources: IAtlasmapDocument[];
  targets: IAtlasmapDocument[];
  sourceProperties: IAtlasmapDocument | null;
  targetProperties: IAtlasmapDocument | null;
  constants: IAtlasmapDocument | null;
  mappings: IAtlasmapMapping[];
  selectedMapping: IAtlasmapMapping | null;
  flatSources: IAtlasmapField[];
  flatTargets: IAtlasmapField[];
}

export function initDataState(): IDataState {
  return {
    pending: false,
    error: false,
    sourceProperties: null,
    targetProperties: null,
    constants: null,
    sources: [],
    targets: [],
    mappings: [],
    selectedMapping: null,
    flatSources: [],
    flatTargets: [],
  };
}

export function dataReducer(state: IDataState, action: DataAction): IDataState {
  switch (action.type) {
    case 'reset':
      return initDataState();
    case 'loading':
      return {
        ...state,
        pending: true,
        error: false,
      };
    case 'update':
      return {
        ...state,
        ...action.payload,
      };
    case 'error':
      return {
        ...initDataState(),
      };
    default:
      return state;
  }
}
