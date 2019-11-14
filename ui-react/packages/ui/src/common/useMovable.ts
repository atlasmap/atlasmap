import { Coords } from '@src/models';
import { useDrag } from 'react-use-gesture';
import clamp from 'lodash.clamp'

const defaultBoundaries: [number, number] = [-Infinity, +Infinity];

export interface IUseMovableArgs {
  enabled: boolean;
  initialPosition: Coords;
  onDrag: (coords: Coords) => void;
  xBoundaries?: [number, number],
  yBoundaries?: [number, number]
}

export function useMovable({
  enabled,
  initialPosition,
  onDrag,
  xBoundaries = defaultBoundaries,
  yBoundaries = defaultBoundaries
}: IUseMovableArgs) {
  return useDrag(
    ({ event, movement: [x, y], memo = [initialPosition.x, initialPosition.y] }) => {
      if (enabled) {
        event!.stopPropagation();
        onDrag({
          x: clamp(x + memo[0], xBoundaries[0], xBoundaries[1]),
          y: clamp(y + memo[1], yBoundaries[0], yBoundaries[1]),
        });
      }
      return memo;
    }
  );
}