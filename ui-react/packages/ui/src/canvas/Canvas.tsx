import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, useCallback } from 'react';
import { useDimensions } from '../common';
import { CanvasProvider } from './CanvasContext';
import { CanvasTransforms } from './CanvasTransforms';


const styles = StyleSheet.create({
  svg: {
    width: '100%',
    height: '100%',
    '& path': {
      transition: 'stroke 0.35s'
    }
  }
});

export interface ICanvasProps {
  width: number;
  height: number;
  zoom: number;
  panX: number;
  panY: number;
  allowPanning: boolean;
  isPanning: boolean;
}

export const Canvas: FunctionComponent<ICanvasProps> = ({
  children,
  width,
  height,
  zoom,
  panX,
  panY,
  allowPanning,
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
        className={css(styles.svg)}
        style={{
          cursor: allowPanning ? (isPanning ? 'grabbing' : 'grab') : undefined,
          userSelect: allowPanning && isPanning ? 'none' : 'auto',
        }}
      >
        <CanvasTransforms>{children}</CanvasTransforms>
      </svg>
    </CanvasProvider>
  );
};
