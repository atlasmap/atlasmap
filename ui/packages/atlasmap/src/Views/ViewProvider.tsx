import { createContext } from "react";

interface IViewContext {
  usingTransformationApproach: boolean;
  initializeActiveMappingExpression: () => void;
}

export const ViewContext = createContext<IViewContext | undefined>(undefined);
