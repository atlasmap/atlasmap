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
import {
  MutableRefObject,
  useCallback,
  useLayoutEffect,
  useRef,
  useState,
} from 'react';

const noSize = {
  width: 0,
  height: 0,
  top: 0,
  left: 0,
  x: 0,
  y: 0,
  right: 0,
  bottom: 0,
} as DOMRect;

function areRectsDifferent(a: DOMRect, b: DOMRect) {
  return (
    a.width !== b.width ||
    a.height !== b.height ||
    a.top !== b.top ||
    a.left !== b.left ||
    a.x !== b.x ||
    a.y !== b.y ||
    a.right !== b.right ||
    a.bottom !== b.bottom
  );
}

export interface UseDimensionsArgs {
  liveMeasure?: boolean;
}

export function useDimensions<T = HTMLDivElement>({
  liveMeasure = true,
}: UseDimensionsArgs = {}): [MutableRefObject<T | null>, DOMRect, () => void] {
  const previousDimensions = useRef<DOMRect>(noSize);
  const [dimensions, setDimensions] = useState<DOMRect>(noSize);
  const ref = useRef<T>(null);

  const measure = useCallback(() => {
    if (ref.current) {
      const d = (ref.current as unknown as HTMLElement).getBoundingClientRect();
      if (
        areRectsDifferent(d as DOMRect, previousDimensions.current as DOMRect)
      ) {
        setDimensions(d);
        previousDimensions.current = d;
      }
    }
  }, [setDimensions]);

  useLayoutEffect(() => {
    measure();

    if (liveMeasure) {
      window.addEventListener('resize', measure);
      window.addEventListener('scroll', measure);
    }
    return () => {
      if (liveMeasure) {
        window.removeEventListener('resize', measure);
        window.removeEventListener('scroll', measure);
      }
    };
  }, [liveMeasure, measure]);

  return [ref, dimensions, measure];
}
