import React, { createContext, FunctionComponent, useContext } from 'react';
import { Coords, useCanvas } from '../Canvas';

interface ICanvasViewLayoutContext {
  boxHeight: number;
  sourceWidth: number;
  targetWidth: number;
  mappingWidth: number;
  initialSourceCoords: Coords;
  initialMappingCoords: Coords;
  initialTargetCoords: Coords;
  isMappingColumnVisible: boolean;
}
const CanvasViewLayoutContext = createContext<
  ICanvasViewLayoutContext | undefined
>(undefined);

export interface ICanvasViewLayoutProviderProps {
  isMappingColumnVisible?: boolean;
}

export const CanvasViewLayoutProvider: FunctionComponent<
  ICanvasViewLayoutProviderProps
> = ({ isMappingColumnVisible = true, children }) => {
  const { height, width } = useCanvas();
  const minBoxWidth = 280;
  const gutter = 30;
  const boxHeight = height - gutter * 2;
  const sourceWidth = Math.max(minBoxWidth, (width / 6) * 2 - gutter * 2);
  const targetWidth = sourceWidth;
  const mappingWidth = Math.max(minBoxWidth, width / 6 - gutter);

  const initialSourceCoords = { x: gutter, y: gutter };
  const initialMappingCoords = {
    x: initialSourceCoords.x + sourceWidth + gutter * 3,
    y: gutter,
  };
  const initialTargetCoords = isMappingColumnVisible
    ? {
        x: initialMappingCoords.x + mappingWidth + gutter * 3,
        y: gutter,
      }
    : {
        x: initialMappingCoords.x + gutter,
        y: gutter,
      };

  return (
    <CanvasViewLayoutContext.Provider
      value={{
        boxHeight,
        sourceWidth,
        targetWidth,
        mappingWidth,
        initialSourceCoords,
        initialMappingCoords,
        initialTargetCoords,
        isMappingColumnVisible,
      }}
    >
      {children}
    </CanvasViewLayoutContext.Provider>
  );
};

export function useCanvasViewLayoutContext() {
  const context = useContext(CanvasViewLayoutContext);
  if (!context) {
    throw new Error(
      `CanvasViewLayout compound components cannot be rendered outside the CanvasViewLayout component`
    );
  }
  return context;
}
