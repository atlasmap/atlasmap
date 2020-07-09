import { createContext } from "react";

interface IViewContext {
  usingTransformationApproach: boolean;
}

export const ViewContext = createContext<IViewContext | null>(null);
