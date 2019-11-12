import React, { useCallback, useRef, DragEvent } from 'react';

export interface IUseMaterializedDragProps {
  onDrag?: (x: number, y: number) => void;
}

export function useMaterializedDrag<T = HTMLElement>({
  onDrag,
}: IUseMaterializedDragProps = {}) {
  const elRef = useRef<T | null>(null);

  const dragStartCoords = useRef<{ x: number; y: number }>({ x: 0, y: 0 });

  const handleDragStart = useCallback(
    (e: DragEvent<HTMLDivElement>) => {
      const fo = e.currentTarget.parentNode as SVGForeignObjectElement;
      dragStartCoords.current = {
        x: parseInt(fo.getAttribute('x') || '0', 10),
        y: parseInt(fo.getAttribute('y') || '0', 10),
      };
    },
    [dragStartCoords]
  );

  const handleDrag = useCallback(
    (e: React.DragEvent<HTMLDivElement>) => {
      let { x, y } = dragStartCoords.current;
      x = x + e.clientX;
      y = y + e.clientY;
      onDrag && onDrag(x, y);
    },
    [dragStartCoords, onDrag]
  );

  return {
    draggable: true,
    ref: elRef,
    onDrag: handleDrag,
    onDragStart: handleDragStart,
  };
}
