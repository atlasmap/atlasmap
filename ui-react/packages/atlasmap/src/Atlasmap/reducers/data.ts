import { IAtlasmapDocument, IAtlasmapMapping } from "../../Views";

export interface DataAction {
  type: "reset" | "loading" | "update" | "error";
  payload?: DataActionPayload;
}

export interface DataActionPayload {
  pending?: boolean;
  error?: boolean;
  sources?: IAtlasmapDocument[];
  targets?: IAtlasmapDocument[];
  properties?: IAtlasmapDocument | null;
  constants?: IAtlasmapDocument | null;
  mappings?: IAtlasmapMapping[];
  selectedMapping?: IAtlasmapMapping | null;
  sourcesFilter?: string;
  targetsFilter?: string;
}

export interface IDataState {
  pending: boolean;
  error: boolean;
  sources: IAtlasmapDocument[];
  targets: IAtlasmapDocument[];
  properties: IAtlasmapDocument | null;
  constants: IAtlasmapDocument | null;
  mappings: IAtlasmapMapping[];
  selectedMapping: IAtlasmapMapping | null;
}

export function initDataState(): IDataState {
  return {
    pending: false,
    error: false,
    properties: null,
    constants: null,
    sources: [],
    targets: [],
    mappings: [],
    selectedMapping: null,
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
