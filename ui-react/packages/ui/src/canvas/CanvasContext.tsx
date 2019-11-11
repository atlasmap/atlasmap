import React, { useMemo } from 'react';
import { createContext, FunctionComponent, useContext } from 'react';
import { scaleLinear } from 'd3-scale';

export interface ICanvasContext {
  width: number;
  height: number;
  zoom: number;
  offsetTop: number;
  offsetLeft: number;
}
const CanvasContext = createContext<ICanvasContext | null>(null);

export const CanvasProvider: FunctionComponent<ICanvasContext> = ({children, ...value }) => (
  <CanvasContext.Provider value={value}>
    {children}
  </CanvasContext.Provider>
);

export function useCanvasInfo() {
  const context = useContext(CanvasContext);
  if (!context) {
    throw new Error('A CanvasProvider wrapper is required to use this hook.');
  }
  const { width, height, zoom, offsetLeft, offsetTop } = context;

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

  return { width, height, zoom, xDomain, yDomain, offsetLeft, offsetTop };
}