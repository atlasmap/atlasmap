import { useCallback } from 'react';
import { useCanvas } from './CanvasContext';

export function useBoundingCanvasRect() {
  const { xDomain, yDomain, offsetLeft, offsetTop, panX, panY } = useCanvas();
  const getBoundingCanvasRect = useCallback(
    (element: Element) => {
      const rect = element.getBoundingClientRect() as DOMRect;
      const canvasRect = {
        x: xDomain(rect.x - offsetLeft - panX),
        y: yDomain(rect.y - offsetTop - panY),
        width: xDomain(rect.width),
        height: yDomain(rect.height),
        top: yDomain(rect.top - offsetTop - panY),
        right: xDomain(rect.right - offsetLeft - panX),
        bottom: yDomain(rect.bottom - offsetTop - panY),
        left: xDomain(rect.left - offsetLeft - panX),
      };
      return canvasRect;
    },
    [offsetLeft, offsetTop, panX, panY, xDomain, yDomain]
  );
  return getBoundingCanvasRect;
}
