import clamp from 'lodash.clamp'
import { useEffect } from 'react';
import { useDrag } from 'react-use-gesture';
import { useCanvas } from '../canvas';
import { Coords } from '../models';

export interface IUseMovableArgs {
  id: string;
  enabled: boolean;
  initialPosition: Coords;
  width: number;
  height: number;
  onDrag: (coords: Coords) => void;
  xBoundaries?: [number, number];
  yBoundaries?: [number, number];
}

export function useMovable({
  id,
  enabled,
  initialPosition,
  width,
  height,
  onDrag,
}: IUseMovableArgs) {

  const { rects, addRect, removeRect } = useCanvas();

  useEffect(() => {
    addRect({ id, ...initialPosition, width, height });
    return () => removeRect(id);
  }, [id, initialPosition, width, height, addRect, removeRect]);

  return useDrag(
    ({
      event,
      movement: [mX, mY],
      memo = [initialPosition.x, initialPosition.y],
    }) => {
      if (enabled) {
        event!.stopPropagation();
        const otherRects = rects
          .filter(r => r.id !== id);
        const leftBoundary = otherRects.reduce(
          (boundary, r) => r.x > memo[0] ? boundary : Math.max(boundary, r.x + r.width),
          -Infinity
        );
        const rightBoundary = otherRects.reduce(
          (boundary, r) => r.x < memo[0] ? boundary : Math.min(boundary, r.x - width),
          +Infinity
        );
        const x = clamp(memo[0] + mX, leftBoundary, rightBoundary);
        const y = memo[1] + mY;
        onDrag({ x, y });
        addRect({ id, x, y, width, height });
      }
      return memo;
    }
  );
}
