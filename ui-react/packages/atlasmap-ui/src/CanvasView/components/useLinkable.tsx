import { useCallback, useRef } from 'react';
import { useBoundingCanvasRect } from '../../Canvas';

export interface IUseLinkableArgs {
  getParentRef?: () => HTMLElement | null;
  getScrollableAreaRef: () => HTMLElement | null;
}

export function useLinkable({ getScrollableAreaRef, getParentRef }: IUseLinkableArgs) {
  const ref = useRef<HTMLDivElement | null>(null);
  const getBoundingCanvasRect = useBoundingCanvasRect();

  const getCoords = useCallback(() => {
    const parentRef = getParentRef ? getParentRef() : null;
    const scrollableAreaRef = getScrollableAreaRef();
    if (ref.current && scrollableAreaRef) {
      const boxRect = getBoundingCanvasRect(scrollableAreaRef);
      let dimensions = getBoundingCanvasRect(ref.current);
      if (parentRef) {
        const parentRect = getBoundingCanvasRect(parentRef);
        dimensions = dimensions.height > 0 ? dimensions : parentRect;
      }
      return {
        left: boxRect.left,
        right: boxRect.right,
        y: Math.min(
          Math.max(dimensions.top + dimensions.height / 2, boxRect.top),
          boxRect.height + boxRect.top
        ),
      };
    }
    return null;
  }, [getParentRef, getScrollableAreaRef, getBoundingCanvasRect]);

  const getLeftSideCoords = () => {
    const coords = getCoords();
    if (coords) {
      const { left, y } = coords;
      return {
        x: left,
        y,
      };
    }
    return null;
  };

  const getRightSideCoords = () => {
    const coords = getCoords();
    if (coords) {
      const { right, y } = coords;
      return {
        x: right,
        y,
      };
    }
    return null;
  };
  return {
    ref,
    getLeftSideCoords,
    getRightSideCoords,
  };
}
