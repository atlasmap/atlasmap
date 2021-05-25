/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import { Coords, useCanvas } from '../../UI';
import React, { FunctionComponent, createContext, useContext } from 'react';

interface ILayoutContext {
  boxHeight: number;
  sourceWidth: number;
  targetWidth: number;
  mappingWidth: number;
  initialSourceCoords: Coords;
  initialMappingCoords: Coords;
  initialTargetCoords: Coords;
  showMappingColumn: boolean;
}
const LayoutContext = createContext<ILayoutContext | undefined>(undefined);

export interface ILayoutProviderProps {
  showMappingColumn?: boolean;
}

function withoutMappingsSizes(
  width: number,
  height: number,
  gutter = 30,
  minBoxWidth = 280,
  maxBoxWidth = 350,
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
      initialSourceCoords.x + sourceWidth + gutter,
    ),
    y: gutter,
  };

  const overflows =
    Math.max(
      initialSourceCoords.x + sourceWidth,
      initialMappingCoords.x + mappingWidth,
      initialTargetCoords.x + targetWidth,
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
  maxBoxWidth = 350,
) {
  const numberOfGutters = 4;
  const columns = 12;
  let availableWidth = width - gutter * numberOfGutters;
  const columnWidth = availableWidth / columns;

  const boxHeight = height - gutter * 2;
  const mappingWidth = Math.min(
    maxBoxWidth,
    Math.max(minBoxWidth, columnWidth * 2),
  );

  availableWidth = width - mappingWidth - gutter * numberOfGutters - 2;

  const sourceWidth = Math.min(
    maxBoxWidth,
    Math.max(minBoxWidth, availableWidth / 2),
  );
  const targetWidth = sourceWidth;

  const initialSourceCoords = { x: gutter, y: gutter };
  const initialMappingCoords = {
    x: Math.max(
      width / 2 - mappingWidth / 2,
      initialSourceCoords.x + sourceWidth + gutter,
    ),
    y: gutter,
  };
  const initialTargetCoords = {
    x: Math.max(
      width - gutter - targetWidth,
      initialMappingCoords.x + mappingWidth + gutter,
    ),
    y: gutter,
  };

  const overflows =
    Math.max(
      initialSourceCoords.x + sourceWidth,
      initialMappingCoords.x + mappingWidth,
      initialTargetCoords.x + targetWidth,
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

export const LayoutProvider: FunctionComponent<ILayoutProviderProps> = ({
  showMappingColumn = true,
  children,
}) => {
  const {
    dimensions: { height, width },
  } = useCanvas();

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
    <LayoutContext.Provider
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
    </LayoutContext.Provider>
  );
};

export function useLayoutContext() {
  const context = useContext(LayoutContext);
  if (!context) {
    throw new Error(
      `Layout compound components cannot be rendered outside the Layout component`,
    );
  }
  return context;
}
