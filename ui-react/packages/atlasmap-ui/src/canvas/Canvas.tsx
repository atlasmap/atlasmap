import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, HTMLAttributes } from 'react';
import { CanvasProvider } from './CanvasContext';
import { CanvasTransforms } from './CanvasTransforms';


const styles = StyleSheet.create({
  svg: {
    transition: 'all 0.15s linear',
    '& path': {
      transition: 'stroke 0.35s'
    }
  }
});

export interface ICanvasProps extends HTMLAttributes<SVGSVGElement> {
  width: number;
  height: number;
  panX: number;
  panY: number;
  isPanning: boolean;
  zoom: number;
  offsetLeft: number;
  offsetRight: number;
  allowPanning: boolean;
}

export const Canvas: FunctionComponent<ICanvasProps> = ({
  children,
  width,
  height,
  panX,
  panY,
  isPanning,
  zoom,
  offsetLeft,
  offsetRight,
  allowPanning,
  ...props
}) => {

  return (
    <CanvasProvider
      width={width}
      height={height}
      zoom={zoom}
      offsetLeft={offsetLeft}
      offsetTop={offsetRight}
      panX={panX}
      panY={panY}
    >
      <svg
        className={css(styles.svg)}
        style={{
          cursor: allowPanning ? (isPanning ? 'grabbing' : 'grab') : undefined,
          userSelect: allowPanning && isPanning ? 'none' : 'auto',
        }}
        width={width}
        height={height}
        {...props}
      >
        <CanvasTransforms>{children}</CanvasTransforms>
      </svg>
    </CanvasProvider>
  );
};
