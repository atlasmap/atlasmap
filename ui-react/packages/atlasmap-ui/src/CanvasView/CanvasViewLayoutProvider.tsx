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
  showMappingColumn: boolean;
}
const CanvasViewLayoutContext = createContext<
  ICanvasViewLayoutContext | undefined
>(undefined);

export interface ICanvasViewLayoutProviderProps {
  showMappingColumn?: boolean;
}

function withoutMappingsSizes(
  width: number,
  height: number,
  gutter = 30,
  minBoxWidth = 280,
  maxBoxWidth = 350
) {
  const numberOfGutters = 3;
  const availableWidth = width - gutter * numberOfGutters;
  const columnWidth = availableWidth / 2;

  const boxHeight = height - gutter * 2;
  const sourceWidth = Math.min(maxBoxWidth, Math.max(minBoxWidth, columnWidth));
  const targetWidth = sourceWidth;
  const mappingWidth = 0;

  const initialSourceCoords = { x: gutter, y: gutter };
  const initialMappingCoords = {
    x: -9999,
    y: -9999,
  };
  const initialTargetCoords = {
    x: Math.max(
      width - gutter - targetWidth,
      initialSourceCoords.x + sourceWidth + gutter
    ),
    y: gutter,
  };

  const overflows =
    Math.max(
      initialSourceCoords.x + sourceWidth,
      initialMappingCoords.x + mappingWidth,
      initialTargetCoords.x + targetWidth
    ) >
    width - gutter;

  return {
    boxHeight,
    sourceWidth,
    targetWidth,
    mappingWidth,
    initialSourceCoords,
    initialMappingCoords,
    initialTargetCoords,
    overflows,
  };
}

function withMappingsSizes(
  width: number,
  height: number,
  gutter = 30,
  minBoxWidth = 280,
  maxBoxWidth = 350
) {
  const numberOfGutters = 4;
  const columns = 12;
  let availableWidth = width - gutter * numberOfGutters;
  const columnWidth = availableWidth / columns;

  const boxHeight = height - gutter * 2;
  const mappingWidth = Math.min(
    maxBoxWidth,
    Math.max(minBoxWidth, columnWidth * 2)
  );

  availableWidth = width - mappingWidth - gutter * numberOfGutters - 2;

  const sourceWidth = Math.min(
    maxBoxWidth,
    Math.max(minBoxWidth, availableWidth / 2)
  );
  const targetWidth = sourceWidth;

  const initialSourceCoords = { x: gutter, y: gutter };
  const initialMappingCoords = {
    x: Math.max(
      width / 2 - mappingWidth / 2,
      initialSourceCoords.x + sourceWidth + gutter
    ),
    y: gutter,
  };
  const initialTargetCoords = {
    x: Math.max(
      width - gutter - targetWidth,
      initialMappingCoords.x + mappingWidth + gutter
    ),
    y: gutter,
  };

  const overflows =
    Math.max(
      initialSourceCoords.x + sourceWidth,
      initialMappingCoords.x + mappingWidth,
      initialTargetCoords.x + targetWidth
    ) >
    width - gutter;

  return {
    boxHeight,
    sourceWidth,
    targetWidth,
    mappingWidth,
    initialSourceCoords,
    initialMappingCoords,
    initialTargetCoords,
    overflows,
  };
}

export const CanvasViewLayoutProvider: FunctionComponent<ICanvasViewLayoutProviderProps> = ({
  showMappingColumn = true,
  children,
}) => {
  const { height, width } = useCanvas();

  const withMappingSizesObj = withMappingsSizes(width, height);

  showMappingColumn = showMappingColumn || !withMappingSizesObj.overflows;

  const {
    boxHeight,
    sourceWidth,
    targetWidth,
    mappingWidth,
    initialSourceCoords,
    initialMappingCoords,
    initialTargetCoords,
  } = showMappingColumn
    ? withMappingSizesObj
    : withoutMappingsSizes(width, height);

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
        showMappingColumn,
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
