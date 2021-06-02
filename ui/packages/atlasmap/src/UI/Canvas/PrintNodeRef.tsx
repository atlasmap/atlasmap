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
import { NodeRect, useNodeRect } from './NodeRefProvider';
import React, {
  HTMLAttributes,
  forwardRef,
  useCallback,
  useEffect,
  useState,
} from 'react';

import { useCanvas } from './CanvasContext';

const printNodeRef = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  function PrintNodeRef({ id, children, ...props }, ref) {
    const { addRedrawListener, removeRedrawListener } = useCanvas();
    const getRect = useNodeRect();
    const [rect, setRect] = useState<NodeRect | null>(null);

    const getAndSetRect = useCallback(() => {
      if (id) {
        const nodeRect = getRect(id);
        setRect(nodeRect ? getRect(id) : null);
      }
    }, [getRect, id]);

    useEffect(() => {
      addRedrawListener(getAndSetRect);
      return () => {
        removeRedrawListener(getAndSetRect);
      };
    }, [addRedrawListener, removeRedrawListener, getAndSetRect]);

    return (
      <div {...props} ref={ref}>
        <pre>{JSON.stringify(rect, null, 2)}</pre>
        {children}
      </div>
    );
  },
);
export default printNodeRef;
