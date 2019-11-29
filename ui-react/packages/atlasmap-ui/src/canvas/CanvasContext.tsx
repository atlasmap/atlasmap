import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { createContext, FunctionComponent, useContext } from 'react';
import { scaleLinear } from 'd3-scale';
import { Rect, Rects } from '../models';

type RedrawCallback = () => unknown;
type RedrawCallbacks = Array<RedrawCallback>;

export interface ICanvasContext {
  width: number;
  height: number;
  zoom: number;
  offsetTop: number;
  offsetLeft: number;
  panX: number;
  panY: number;
  lastUpdate: number;
  redraw: () => void;
  addRedrawListener: (callback: RedrawCallback) => void;
  removeRedrawListener: (callback: RedrawCallback) => void;
  rects: Rects;
  addRect: (rect: Rect) => void;
  removeRect: (id: string) => void;
}
const CanvasContext = createContext<ICanvasContext | null>(null);

export interface ICanvasProviderProps {
  width: number;
  height: number;
  zoom: number;
  offsetTop: number;
  offsetLeft: number;
  panX: number;
  panY: number;
}
export const CanvasProvider: FunctionComponent<ICanvasProviderProps> = ({
  children,
  width,
  height,
  zoom,
  offsetTop,
  offsetLeft,
  panX,
  panY,
}) => {
  const rects = useRef<Rects>([]);
  const addRect = (rect: Rect) => {
    removeRect(rect.id);
    rects.current = [...rects.current, rect];
  };
  const removeRect = (id: string) => {
    rects.current = rects.current.filter(r => r.id !== id);
  };

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
  }, [width, height, zoom, offsetLeft, offsetTop, panX, panY, redraw]);

  useEffect(() => {
    const requestId = requestAnimationFrame(() =>
      redrawCallbacks.current.forEach(cb => cb())
    );
    return () => {
      cancelAnimationFrame(requestId);
    };
  }, [lastUpdate, redrawCallbacks]);

  return (
    <CanvasContext.Provider
      value={{
        width,
        height,
        zoom,
        offsetTop,
        offsetLeft,
        panX,
        panY,
        addRedrawListener,
        removeRedrawListener,
        redraw,
        lastUpdate,
        rects: rects.current,
        addRect,
        removeRect
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
    panX,
    panY,
    redraw,
    lastUpdate,
    addRedrawListener,
    removeRedrawListener,
    rects,
    addRect,
    removeRect
  } = context;

  const xDomain = useMemo(
    () =>
      scaleLinear()
        .range([0, width])
        .domain([0, width * zoom])
    ,
    [width, zoom]
  );
  const yDomain = useMemo(
    () =>
      scaleLinear()
        .range([height, 0])
        .domain([height * zoom, 0]),
    [height, zoom]
  );

  return {
    width,
    height,
    zoom,
    xDomain,
    yDomain,
    offsetLeft,
    offsetTop,
    panX,
    panY,
    redraw,
    lastUpdate,
    addRedrawListener,
    removeRedrawListener,
    rects,
    addRect,
    removeRect
  };
}
