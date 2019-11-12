import { CanvasProvider } from '@src/canvas/CanvasContext';
import { useDimensions } from '@src/useDimensions';
import React, { FunctionComponent, useCallback } from 'react';

export interface ICanvasProps {
  width: number;
  height: number;
  zoom: number;
}

export const Canvas: FunctionComponent<ICanvasProps> = ({
  children,
  width,
  height,
  zoom,
}) => {
  const [ref, dimensions] = useDimensions<SVGSVGElement>();
  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
  }, []);

  return (
    <CanvasProvider
      width={width}
      height={height}
      zoom={zoom}
      offsetLeft={dimensions.left}
      offsetTop={dimensions.top}
    >
      <svg
        onDragOver={handleDragOver}
        ref={ref}
        style={{ width: '100%', height: '100%' }}
      >
        {children}
      </svg>
    </CanvasProvider>
  );
};
