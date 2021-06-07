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
import { Arc, IArcProps } from './Arc';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useState,
} from 'react';

import { Coords } from './models';
import { useCanvas } from './CanvasContext';
import { useNodeRect } from './NodeRefProvider';

function leftCoords(rect: DOMRect) {
  const { left, y } = rect;
  return {
    x: left,
    y: y,
  };
}

function rightCoords(rect: DOMRect) {
  const { right, y } = rect;
  return {
    x: right,
    y: y,
  };
}

function topCoords(rect: DOMRect) {
  const { x, top } = rect;
  return {
    x: x,
    y: top,
  };
}

function bottomCoords(rect: DOMRect) {
  const { x, bottom } = rect;
  return {
    x: x,
    y: bottom,
  };
}

export interface INodesArcProps
  extends Omit<Omit<Omit<IArcProps, 'start'>, 'end'>, 'type'> {
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
  const [linkType, setLinkType] = useState<'horizontal' | 'vertical'>(
    'horizontal',
  );

  const calculateCoords = useCallback(
    function calculateCoordsCb() {
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
              start: topCoords(start),
              end: bottomCoords(end),
              startSideSize: start.clipped ? 0 : start.width,
              endSideSize: end.clipped ? 0 : end.width,
            });
            setLinkType('vertical');
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
              start: leftCoords(start),
              end: rightCoords(end),
              startSideSize: start.clipped ? 0 : start.height,
              endSideSize: end.clipped ? 0 : end.height,
            });
            setLinkType('horizontal');
          } else {
            setCoords(null);
          }
        }
      } else {
        setCoords(null);
      }
    },
    [endId, startId, getRect],
  );

  useEffect(
    function onNodeArcRedrawCb() {
      addRedrawListener(calculateCoords);
      return () => {
        removeRedrawListener(calculateCoords);
      };
    },
    [addRedrawListener, removeRedrawListener, calculateCoords],
  );

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
          ? 'var(--pf-global--Color--light-300)'
          : 'var(--pf-global--Color--dark-200)')
      }
      {...props}
    />
  ) : null;
};
