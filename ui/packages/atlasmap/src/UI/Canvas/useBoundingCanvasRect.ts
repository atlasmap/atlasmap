import { useCallback } from "react";
import { useCanvas } from "./CanvasContext";

export function useBoundingCanvasRect() {
  const {
    xDomain,
    yDomain,
    dimensions: { offsetLeft, offsetTop },
    panX,
    panY,
  } = useCanvas();
  const convertClientX = useCallback(
    (x: number) => xDomain(x - offsetLeft - panX),
    [offsetLeft, panX, xDomain],
  );
  const convertClientY = useCallback(
    (y: number) => yDomain(y - offsetTop - panY),
    [offsetTop, panY, yDomain],
  );
  const convertDOMRectToCanvasRect = useCallback(
    (rect: DOMRect): DOMRect => {
      return {
        x: convertClientX(rect.x),
        y: convertClientY(rect.y),
        width: xDomain(rect.width),
        height: yDomain(rect.height),
        top: convertClientY(rect.top),
        right: convertClientX(rect.right),
        bottom: convertClientY(rect.bottom),
        left: convertClientX(rect.left),
      } as DOMRect;
    },
    [convertClientX, convertClientY, xDomain, yDomain],
  );
  return {
    getBoundingCanvasRect: useCallback(
      (element: Element): DOMRect => {
        const rect = element.getBoundingClientRect() as DOMRect;
        return convertDOMRectToCanvasRect(rect);
      },
      [convertDOMRectToCanvasRect],
    ),
    convertDOMRectToCanvasRect,
    convertClientX,
    convertClientY,
  };
}
