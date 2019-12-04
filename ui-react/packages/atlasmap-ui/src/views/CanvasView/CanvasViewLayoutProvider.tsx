import React, { createContext, FunctionComponent, useContext } from 'react';
import { useCanvas } from '../../canvas';
import { Coords } from './models';

interface ICanvasViewLayoutContext {
  boxHeight: number;
  sourceWidth: number;
  targetWidth: number;
  mappingWidth: number;
  initialSourceCoords: Coords;
  initialMappingCoords: Coords;
  initialTargetCoords: Coords;
}
const CanvasViewLayoutContext = createContext<ICanvasViewLayoutContext | undefined>(undefined);

export const CanvasViewLayoutProvider: FunctionComponent = ({
  children
}) => {
  const { height, width } = useCanvas();
  const gutter = 30;
  const boxHeight = height - gutter * 2;
  const sourceWidth = Math.max(250, (width / 6) * 2 - gutter * 2);
  const targetWidth = sourceWidth;
  const mappingWidth = Math.max(300, width / 6 - gutter);

  const initialSourceCoords = { x: gutter, y: gutter };
  const initialMappingCoords = {
    x: initialSourceCoords.x + sourceWidth + gutter * 3,
    y: gutter,
  };
  const initialTargetCoords = {
    x: initialMappingCoords.x + mappingWidth + gutter * 3,
    y: gutter,
  };

  return (
    <CanvasViewLayoutContext.Provider value={{
      boxHeight,
      sourceWidth,
      targetWidth,
      mappingWidth,
      initialSourceCoords,
      initialMappingCoords,
      initialTargetCoords,
    }}>
      {children}
    </CanvasViewLayoutContext.Provider>
  )
}

export function useCanvasViewLayoutContext() {
  const context = useContext(CanvasViewLayoutContext);
  if (!context) {
    throw new Error(
      `CanvasViewLayout compound components cannot be rendered outside the CanvasViewLayout component`,
    )
  }
  return context;
}