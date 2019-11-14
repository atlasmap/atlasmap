import { CanvasProvider } from '@src/canvas/CanvasContext';
import { CanvasTransforms } from '@src/canvas/CanvasTransforms';
import { useDimensions } from '@src/common/useDimensions';
import React, { FunctionComponent, useCallback } from 'react';

export interface ICanvasProps {
  width: number;
  height: number;
  zoom: number;
  panX: number;
  panY: number;
  isPanning: boolean;
}

export const Canvas: FunctionComponent<ICanvasProps> = ({
  children,
  width,
  height,
  zoom,
  panX,
  panY,
  isPanning,
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
      panX={panX}
      panY={panY}
    >
      <svg
        onDragOver={handleDragOver}
        ref={ref}
        style={{
          width: '100%',
          height: '100%',
          cursor: isPanning ? 'grabbing' : 'grab',
          userSelect: isPanning ? 'none' : 'auto',
        }}
      >
        <CanvasTransforms>{children}</CanvasTransforms>
      </svg>
    </CanvasProvider>
  );
};
