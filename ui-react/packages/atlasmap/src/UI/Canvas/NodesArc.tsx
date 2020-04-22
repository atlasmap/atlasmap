import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useState,
} from "react";
import { Arc, IArcProps } from "./Arc";
import { useCanvas } from "./CanvasContext";
import { Coords } from "./models";
import { useNodeRect } from "./NodeRefProvider";

function leftCoords(rect: DOMRect, clipped?: boolean) {
  const { left, y, height } = rect;
  return {
    x: left,
    y: y + (clipped ? 0 : height / 2),
  };
}

function rightCoords(rect: DOMRect, clipped?: boolean) {
  const { right, y, height } = rect;
  return {
    x: right,
    y: y + (clipped ? 0 : height / 2),
  };
}

function topCoords(rect: DOMRect, clipped?: boolean) {
  const { x, top, width } = rect;
  return {
    x: x + (clipped ? 0 : width / 2),
    y: top,
  };
}

function bottomCoords(rect: DOMRect, clipped?: boolean) {
  const { x, bottom, width } = rect;
  return {
    x: x + (clipped ? 0 : width / 2),
    y: bottom,
  };
}

export interface INodesArcProps
  extends Omit<Omit<Omit<IArcProps, "start">, "end">, "type"> {
  start: string;
  end: string;
}

export const NodesArc: FunctionComponent<INodesArcProps> = ({
  start: startId,
  end: endId,
  color,
  ...props
}) => {
  const { addRedrawListener, removeRedrawListener } = useCanvas();
  const getRect = useNodeRect();
  const [coords, setCoords] = useState<{
    start: Coords;
    end: Coords;
    startSideSize: number;
    endSideSize: number;
  } | null>(null);
  const [linkType, setLinkType] = useState<"horizontal" | "vertical">(
    "horizontal",
  );

  const calculateCoords = useCallback(() => {
    const startRect = getRect(startId);
    const endRect = getRect(endId);
    if (startRect && endRect) {
      const [smallRect, bigRect] =
        startRect.width < endRect.width
          ? [startRect, endRect]
          : [endRect, startRect];
      const sameVerticalSpace =
        (smallRect.left > bigRect.left && smallRect.left < bigRect.right) ||
        (smallRect.right < bigRect.right && smallRect.right > bigRect.left);
      if (sameVerticalSpace) {
        const [aboveId, bottomId] =
          startRect.top > endRect.top ? [startId, endId] : [endId, startId];
        const start = getRect(aboveId);
        const end = getRect(bottomId);
        if (start && end) {
          setCoords({
            start: topCoords(start, start.clipped),
            end: bottomCoords(end, end.clipped),
            startSideSize: start.clipped ? 0 : start.width,
            endSideSize: end.clipped ? 0 : end.width,
          });
          setLinkType("vertical");
        } else {
          setCoords(null);
        }
      } else {
        const [leftId, rightId] =
          startRect.left > endRect.left ? [startId, endId] : [endId, startId];
        const start = getRect(leftId);
        const end = getRect(rightId);
        if (start && end) {
          setCoords({
            start: leftCoords(start, start.clipped),
            end: rightCoords(end, end.clipped),
            startSideSize: start.clipped ? 0 : start.height,
            endSideSize: end.clipped ? 0 : end.height,
          });
          setLinkType("horizontal");
        } else {
          setCoords(null);
        }
      }
    } else {
      setCoords(null);
    }
  }, [endId, startId, getRect]);

  useEffect(() => {
    addRedrawListener(calculateCoords);
    return () => {
      removeRedrawListener(calculateCoords);
    };
  }, [addRedrawListener, removeRedrawListener, calculateCoords]);

  return coords ? (
    <Arc
      start={coords.start}
      end={coords.end}
      type={linkType}
      startSideSize={coords.startSideSize}
      endSideSize={coords.endSideSize}
      color={
        color ||
        ((coords.startSideSize && coords.endSideSize) === 0
          ? "var(--pf-global--Color--light-300)"
          : "var(--pf-global--Color--dark-200)")
      }
      {...props}
    />
  ) : null;
};
