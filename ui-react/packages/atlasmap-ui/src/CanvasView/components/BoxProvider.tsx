import React, { createContext, FunctionComponent, useContext } from 'react';

interface IBoxContext {
  getScrollableAreaRef: () => HTMLDivElement | null;
}

const BoxContext = createContext<IBoxContext | null>(null);

export const BoxProvider: FunctionComponent<IBoxContext> = ({
  children,
  ...props
}) => {
  return <BoxContext.Provider value={props}>{children}</BoxContext.Provider>;
};

export function useBoxContext() {
  const context = useContext(BoxContext);
  if (!context) {
    throw new Error('useBoxContext can be used only inside a Box component');
  }
  return context;
}
