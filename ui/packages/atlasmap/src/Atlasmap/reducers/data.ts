import { IAtlasmapDocument, IAtlasmapMapping } from "../../Views";

import { IAtlasmapField } from "./../../Views/models";

export interface DataAction {
  type: "reset" | "loading" | "update" | "error";
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
    case "reset":
      return initDataState();
    case "loading":
      return {
        ...state,
        pending: true,
        error: false,
      };
    case "update":
      return {
        ...state,
        ...action.payload,
      };
    case "error":
      return {
        ...initDataState(),
      };
    default:
      return state;
  }
}
