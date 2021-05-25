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
import { MutableRefObject, ReactElement, useState } from 'react';

import { Coords } from './models';
import styles from './Movable.module.css';
import { useBoundingCanvasRect } from './useBoundingCanvasRect';
import { useDimensions } from '../useDimensions';
import { useMovable } from './useMovable';

export interface IMovableChildren<T> {
  x?: number;
  y?: number;
  bind: ReturnType<typeof useMovable>;
  ref: MutableRefObject<T | null>;
  className?: string;
}

export interface IMovableProps<T> {
  id: string;
  enabled?: boolean;
  children: (props: IMovableChildren<T>) => ReactElement;
}

export function Movable<T = HTMLDivElement>({
  id,
  enabled = true,
  children,
}: IMovableProps<T>) {
  const { convertDOMRectToCanvasRect } = useBoundingCanvasRect();
  const [coords, setCoords] = useState<Coords | undefined>();
  const [ref, rect] = useDimensions<T>();
  const { width, height, left, top } = convertDOMRectToCanvasRect(rect);
  const bind = useMovable({
    id,
    enabled,
    initialPosition: coords ? coords : { x: left, y: top },
    width,
    height,
    onDrag: (coords: Coords) => {
      setCoords(coords);
    },
  });
  return children({
    ...coords,
    bind,
    ref,
    className: enabled ? styles.movable : undefined,
  });
}
