/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import { Coords, RectWithId } from "./models";

import { useCanvas } from "./CanvasContext";
import { useDrag } from "react-use-gesture";
import { useEffect } from "react";

function intersectRect(r1: RectWithId, r2: RectWithId) {
  return !(
    r2.x > r1.x + r1.width ||
    r2.x + r2.width < r1.x ||
    r2.y > r1.y + r1.height ||
    r2.y + r2.height < r1.y
  );
}

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
  const { getRects, addRect, removeRect } = useCanvas();
  // const { convertClientX, convertClientY } = useBoundingCanvasRect();

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
        const otherRects = getRects().filter((r) => r.id !== id);
        const isColliding = otherRects.reduce(
          (colliding, r) =>
            colliding ||
            intersectRect(r, {
              id: "",
              x: memo[0] + mX,
              y: memo[1] + mY,
              width,
              height,
            }),
          false,
        );
        if (!isColliding) {
          const x = memo[0] + mX;
          const y = memo[1] + mY;
          onDrag({ x, y });
          addRect({ id, x, y, width, height });
        }
      }
      return memo;
    },
  );
}
