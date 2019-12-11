import React, { createContext, FunctionComponent, useCallback, useContext, useState } from 'react';
interface ICanvasViewOptionsContext {
  freeView: boolean;
  toggleFreeView: () => void;
}
const CanvasViewOptionsContext = createContext<ICanvasViewOptionsContext | undefined>(undefined);

export const CanvasViewOptionsProvider: FunctionComponent = ({
  children
}) => {
  const [freeView, setFreeView] = useState(false);
  const toggleFreeView = useCallback(() =>
      setFreeView(!freeView),
    [
      freeView,
      setFreeView,
    ]);

  return (
    <CanvasViewOptionsContext.Provider value={{
      freeView,
      toggleFreeView
    }}>
      {children}
    </CanvasViewOptionsContext.Provider>
  )
};

export function useCanvasViewOptionsContext() {
  const context = useContext(CanvasViewOptionsContext);
  if (!context) {
    throw new Error(
      `CanvasViewOptions compound components cannot be rendered outside the CanvasViewOptions component`,
    )
  }
  return context;
}