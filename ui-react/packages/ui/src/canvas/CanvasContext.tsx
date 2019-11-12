import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { createContext, FunctionComponent, useContext } from 'react';
import { scaleLinear } from 'd3-scale';

type RedrawCallback = () => unknown;
type RedrawCallbacks = Array<RedrawCallback>;

export interface ICanvasContext {
  width: number;
  height: number;
  zoom: number;
  offsetTop: number;
  offsetLeft: number;
  lastUpdate: number;
  redraw: () => void;
  redrawCallbacks: RedrawCallbacks;
  addRedrawListener: (callback: RedrawCallback) => void;
  removeRedrawListener: (callback: RedrawCallback) => void;
}
const CanvasContext = createContext<ICanvasContext | null>(null);

export interface ICanvasProviderProps {
  width: number;
  height: number;
  zoom: number;
  offsetTop: number;
  offsetLeft: number;
}
export const CanvasProvider: FunctionComponent<ICanvasProviderProps> = ({
  children,
  width,
  height,
  zoom,
  offsetTop,
  offsetLeft,
}) => {
  const [lastUpdate, setLastUpdate] = useState(Date.now());
  const redraw = useCallback(() => {
    setLastUpdate(Date.now());
  }, [setLastUpdate]);
  const redrawCallbacks = useRef<RedrawCallbacks>([]);
  const addRedrawListener = useCallback(
    (cb: RedrawCallback) => {
      redrawCallbacks.current = [...redrawCallbacks.current, cb];
    },
    [redrawCallbacks]
  );
  const removeRedrawListener = useCallback(
    (cb: RedrawCallback) => {
      redrawCallbacks.current = redrawCallbacks.current.filter(c => c !== cb);
    },
    [redrawCallbacks]
  );

  useEffect(() => {
    const requestId = requestAnimationFrame(redraw);
    return () => {
      cancelAnimationFrame(requestId);
    };
  }, [width, height, zoom, offsetTop, offsetLeft, redraw]);

  useEffect(() => {
    redrawCallbacks.current.forEach(cb => cb());
  }, [redrawCallbacks, lastUpdate]);

  return (
    <CanvasContext.Provider
      value={{
        width,
        height,
        zoom,
        offsetTop,
        offsetLeft,
        redrawCallbacks: redrawCallbacks.current,
        addRedrawListener,
        removeRedrawListener,
        redraw,
        lastUpdate,
      }}
    >
      {children}
    </CanvasContext.Provider>
  );
};

export function useCanvas() {
  const context = useContext(CanvasContext);
  if (!context) {
    throw new Error('A CanvasProvider wrapper is required to use this hook.');
  }
  const {
    width,
    height,
    zoom,
    offsetLeft,
    offsetTop,
    redraw,
    lastUpdate,
    addRedrawListener,
    removeRedrawListener,
  } = context;

  const xDomain = useMemo(
    () =>
      scaleLinear()
        .range([0, width])
        .domain([0, width /* * zoom*/]),
    [width]
  );
  const yDomain = useMemo(
    () =>
      scaleLinear()
        .range([height, 0])
        .domain([height /* * zoom*/, 0]),
    [height]
  );

  return {
    width,
    height,
    zoom,
    xDomain,
    yDomain,
    offsetLeft,
    offsetTop,
    redraw,
    lastUpdate,
    addRedrawListener,
    removeRedrawListener,
  };
}
