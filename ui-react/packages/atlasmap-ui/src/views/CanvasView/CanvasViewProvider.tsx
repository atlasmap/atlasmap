import React, { createContext, FunctionComponent, useCallback, useContext, useState } from 'react';
import { useGesture } from 'react-use-gesture';
import { Coords } from './models';

interface ICanvasViewContext {
  zoom: number;
  updateZoom: (tick: number) => void;
  resetZoom: () => void;
  pan: Coords;
  setPan: (pan: Coords) => void;
  resetPan: () => void;
  isPanning: boolean;
  freeView: boolean;
  toggleFreeView: () => void;
  materializedMappings: boolean;
  toggleMaterializedMappings: () => void;
  bindCanvas: ReturnType<typeof useGesture>;
}
const CanvasViewContext = createContext<ICanvasViewContext | undefined>(undefined);

export interface ICanvasViewProviderProps {

}
export const CanvasViewProvider: FunctionComponent<ICanvasViewProviderProps> = ({
  children
}) => {
  const [freeView, setFreeView] = useState(false);
  const [materializedMappings, setMaterializedMappings] = useState(true);

  const [{ x: panX, y: panY }, setPan] = useState<Coords>({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);

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

  const updateZoom = useCallback(
    (tick: number) => {
      setZoom(currentZoom => Math.max(0.2, Math.min(2, currentZoom + tick)));
    },
    [setZoom]
  );

  const resetZoom = useCallback(() =>
    setZoom(1),
    [setZoom]
  );

  const resetPan = useCallback(() =>
      setPan({ x: 0, y: 0 }),
    [setPan]
  );

  const [isPanning, setIsPanning] = useState(false);
  const bindCanvas = useGesture(
    {
      onDrag: ({ movement: [x, y], first, last, memo = [panX, panY] }) => {
        if (freeView) {
          if (first) setIsPanning(true);
          if (last) setIsPanning(false);
          setPan({ x: x + memo[0], y: y + memo[1] });
        }
        return memo;
      },
      onWheel: ({ delta }) => {
        if (freeView) {
          updateZoom(delta[1] * -0.001);
        }
      },
    },
    { dragDelay: true }
  );

  return (
    <CanvasViewContext.Provider value={{
      zoom,
      updateZoom,
      resetZoom,
      pan: { x: panX, y: panY },
      setPan,
      resetPan,
      isPanning,
      bindCanvas,
      freeView,
      toggleFreeView,
      materializedMappings,
      toggleMaterializedMappings,
    }}>
      {children}
    </CanvasViewContext.Provider>
  )
}

export function useCanvasViewContext() {
  const context = useContext(CanvasViewContext);
  if (!context) {
    throw new Error(
      `CanvasView compound components cannot be rendered outside the CanvasView component`,
    )
  }
  return context;
}