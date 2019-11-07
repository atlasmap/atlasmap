import React, { useMemo } from 'react';
import { createContext, FunctionComponent, useContext } from 'react';
import { scaleLinear } from 'd3-scale';

export interface ICanvasContext {
  width: number;
  height: number;
  zoom: number;
}
const CanvasContext = createContext<ICanvasContext | null>(null);

export const CanvasProvider: FunctionComponent<ICanvasContext> = ({children, ...value }) => (
  <CanvasContext.Provider value={value}>
    {children}
  </CanvasContext.Provider>
);

export function useCanvasDomain() {
  const context = useContext(CanvasContext);
  if (!context) {
    throw new Error('A CanvasProvider wrapper is required to use this hook.');
  }
  const { width, height, zoom } = context;

  const xDomain = useMemo(
    () =>
      scaleLinear()
        .range([0, width])
        .domain([0, width /* * zoom*/]),
    [width, zoom]
  );
  const yDomain = useMemo(
    () =>
      scaleLinear()
        .range([height, 0])
        .domain([height /* * zoom*/, 0]),
    [width, zoom]
  );

  return { xDomain, yDomain };
}