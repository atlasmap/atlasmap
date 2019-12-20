import React, { createContext, FunctionComponent, useContext } from "react";

interface IDocumentContext {
  getScrollableAreaRef: () => HTMLDivElement | null;
}

const DocumentContext = createContext<IDocumentContext | null>(null);

export const DocumentProvider: FunctionComponent<IDocumentContext> = ({ children, ...props }) => {
  return (
    <DocumentContext.Provider value={props}>
      {children}
    </DocumentContext.Provider>
  )
} ;

export function useDocumentContext() {
  const context = useContext(DocumentContext);
  if (!context) {
    throw new Error('useDocumentContext can be used only inside a Document component');
  }
  return context;
}