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
import { useCallback } from 'react';
import { useCanvas } from './CanvasContext';

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
