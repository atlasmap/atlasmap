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
import React, { useCallback, useEffect, useState } from 'react';
import { RectWithId } from './models';
import { useCanvas } from './CanvasContext';

export default function PrintMovableRects() {
  const { addRedrawListener, removeRedrawListener, getRects } = useCanvas();
  const [rects, setRects] = useState<RectWithId[]>([]);

  const updateRects = useCallback(() => {
    setRects(getRects());
  }, [getRects]);

  useEffect(() => {
    addRedrawListener(updateRects);
    return () => {
      removeRedrawListener(updateRects);
    };
  }, [addRedrawListener, removeRedrawListener, updateRects]);

  return (
    <g>
      {rects.map((r, idx) => (
        <rect
          x={r.x}
          y={r.y}
          width={r.width}
          height={r.height}
          stroke={'black'}
          strokeDasharray={2}
          fillOpacity={0}
          key={idx}
          style={{ pointerEvents: 'none' }}
        />
      ))}
    </g>
  );
}
