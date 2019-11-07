import { CanvasProvider, ICanvasContext } from '@src/CanvasContext';
import React, { forwardRef, PropsWithChildren, useCallback } from 'react';

export const Canvas = forwardRef<SVGSVGElement, PropsWithChildren<ICanvasContext>>(({ children, width, height, zoom }, ref) => {

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
  }, []);

  return (
    <CanvasProvider width={width} height={height} zoom={zoom}>
      <svg
        onDragOver={handleDragOver}
        ref={ref}
        style={{ width: '100%', height: '100%' }}
      >
        {children}
      </svg>
    </CanvasProvider>
  )
});