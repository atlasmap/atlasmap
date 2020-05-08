import React, {
  ReactElement,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { createContext, FunctionComponent, useContext } from "react";
import { scaleLinear } from "d3-scale";
import { Coords, RectWithId, Rects } from "./models";
import { useGesture } from "react-use-gesture";

type RedrawCallback = () => unknown;
type RedrawCallbacks = Array<RedrawCallback>;

export interface ICanvasDimension {
  width: number;
  height: number;
  offsetTop: number;
  offsetLeft: number;
}

export interface ICanvasContext {
  initialWidth?: number;
  initialHeight?: number;
  dimensions: ICanvasDimension;
  setDimension: (dimension: ICanvasDimension) => void;
  panX: number;
  panY: number;
  addRedrawListener: (callback: RedrawCallback) => void;
  removeRedrawListener: (callback: RedrawCallback) => void;
  addRect: (rect: RectWithId) => void;
  removeRect: (id: string) => void;
  getRects: () => Rects;
  zoom: number;
  updateZoom: (tick: number) => void;
  resetZoom: () => void;
  pan: Coords;
  setPan: (pan: Coords) => void;
  resetPan: () => void;
  allowPanning: boolean;
  isPanning: boolean;
  bindCanvas: ReturnType<typeof useGesture>;
}
const CanvasContext = createContext<ICanvasContext | null>(null);

export interface ICanvasProviderProps {
  allowPanning?: boolean;
  initialWidth?: number;
  initialHeight?: number;
  initialZoom?: number;
  initialPanX?: number;
  initialPanY?: number;
}
export const CanvasProvider: FunctionComponent<ICanvasProviderProps> = ({
  children,
  allowPanning = false,
  initialWidth = 0,
  initialHeight = 0,
  initialZoom = 1,
  initialPanX = 0,
  initialPanY = 0,
}) => {
  const [canvasDimension, setCanvasDimension] = useState<ICanvasDimension>({
    width: 0,
    height: 0,
    offsetLeft: 0,
    offsetTop: 0,
  });
  const rects = useRef<Rects>([]);
  const removeRect = useCallback((id: string) => {
    rects.current = rects.current.filter((r) => r.id !== id);
  }, []);
  const addRect = useCallback(
    (rect: RectWithId) => {
      removeRect(rect.id);
      rects.current = [...rects.current, rect];
    },
    [removeRect],
  );
  const getRects = useCallback(() => rects.current, []);
  const redrawCallbacks = useRef<RedrawCallbacks>([]);
  const addRedrawListener = useCallback((cb: RedrawCallback) => {
    redrawCallbacks.current = [...redrawCallbacks.current, cb];
  }, []);
  const removeRedrawListener = useCallback((cb: RedrawCallback) => {
    redrawCallbacks.current = redrawCallbacks.current.filter((c) => c !== cb);
  }, []);

  useEffect(function effectLoop() {
    let frame = requestAnimationFrame(function loop() {
      frame = requestAnimationFrame(loop);

      for (let i = 0, len = redrawCallbacks.current.length; i < len; i++) {
        redrawCallbacks.current[i]();
      }
    });
    return function cancelEffectLoop() {
      cancelAnimationFrame(frame);
    };
  }, []);

  const [{ x: panX, y: panY }, setPan] = useState<Coords>({
    x: initialPanX,
    y: initialPanY,
  });
  const [zoom, setZoom] = useState(initialZoom);

  const updateZoom = useCallback(
    (tick: number) => {
      setZoom((currentZoom) => Math.max(0.2, Math.min(2, currentZoom + tick)));
    },
    [setZoom],
  );

  const resetZoom = useCallback(() => setZoom(1), [setZoom]);
  const resetPan = useCallback(() => setPan({ x: 0, y: 0 }), [setPan]);
  const [isPanning, setIsPanning] = useState(false);
  const bindCanvas = useGesture(
    {
      onDrag: ({ movement: [x, y], first, last, memo = [panX, panY] }) => {
        if (first) setIsPanning(true);
        if (last) setIsPanning(false);
        setPan({ x: x + memo[0], y: y + memo[1] });
        return memo;
      },
      onWheel: ({ delta }) => {
        updateZoom(delta[1] * -0.001);
      },
    },
    {
      drag: {
        delay: true,
      },
      enabled: allowPanning,
    },
  );

  return (
    <CanvasContext.Provider
      value={{
        initialWidth,
        initialHeight,
        dimensions: canvasDimension,
        setDimension: setCanvasDimension,
        panX,
        panY,
        addRedrawListener,
        removeRedrawListener,
        addRect,
        removeRect,
        getRects,
        zoom,
        updateZoom,
        resetZoom,
        pan: { x: panX, y: panY },
        setPan,
        resetPan,
        allowPanning,
        isPanning,
        bindCanvas,
      }}
    >
      {children}
    </CanvasContext.Provider>
  );
};

export function useCanvas() {
  const context = useContext(CanvasContext);
  if (!context) {
    throw new Error("A CanvasProvider wrapper is required to use this hook.");
  }
  const {
    dimensions: { width, height },
    zoom,
  } = context;

  const xDomain = useMemo(
    () =>
      scaleLinear()
        .range([0, width])
        .domain([0, width * zoom]),
    [width, zoom],
  );
  const yDomain = useMemo(
    () =>
      scaleLinear()
        .range([height, 0])
        .domain([height * zoom, 0]),
    [height, zoom],
  );

  return {
    ...context,
    xDomain,
    yDomain,
  };
}

export interface IWithCanvasProps {
  children: (props: ICanvasContext) => ReactElement;
}

export const WithCanvas: FunctionComponent<IWithCanvasProps> = ({
  children,
}) => {
  const context = useCanvas();
  return children(context);
};
