import React, { createContext, FunctionComponent, useCallback, useContext, useState } from 'react';
interface ICanvasViewOptionsContext {
  freeView: boolean;
  toggleFreeView: () => void;
  materializedMappings: boolean;
  toggleMaterializedMappings: () => void;
}
const CanvasViewOptionsContext = createContext<ICanvasViewOptionsContext | undefined>(undefined);

export const CanvasViewOptionsProvider: FunctionComponent = ({
  children
}) => {
  const [freeView, setFreeView] = useState(false);
  const [materializedMappings, setMaterializedMappings] = useState(true);

  const toggleFreeView = useCallback(() =>
      setFreeView(!freeView),
    [
      freeView,
      setFreeView,
    ]);

  const toggleMaterializedMappings = useCallback(
    () => setMaterializedMappings(!materializedMappings),
    [setMaterializedMappings, materializedMappings]
  );

  return (
    <CanvasViewOptionsContext.Provider value={{
      freeView,
      toggleFreeView,
      materializedMappings,
      toggleMaterializedMappings
    }}>
      {children}
    </CanvasViewOptionsContext.Provider>
  )
}

export function useCanvasViewOptionsContext() {
  const context = useContext(CanvasViewOptionsContext);
  if (!context) {
    throw new Error(
      `CanvasViewOptions compound components cannot be rendered outside the CanvasViewOptions component`,
    )
  }
  return context;
}