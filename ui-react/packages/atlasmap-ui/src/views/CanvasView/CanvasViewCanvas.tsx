import React, { FunctionComponent } from 'react';
import { Canvas } from '../../canvas';
import { useCanvasViewContext } from './CanvasViewProvider';

export interface ICanvasViewCanvasProps {
  width: number;
  height: number;
  offsetLeft: number;
  offsetTop: number;
}

export const CanvasViewCanvas: FunctionComponent<ICanvasViewCanvasProps> = ({
  children,
  width,
  height,
  offsetLeft,
  offsetTop,
}) => {
  const { freeView, isPanning, pan, zoom, bindCanvas } = useCanvasViewContext();
  return (
    <Canvas
      width={width}
      height={height}
      offsetLeft={offsetLeft}
      offsetTop={offsetTop}
      allowPanning={freeView}
      isPanning={freeView ? isPanning : false}
      panX={freeView ? pan.x : 0}
      panY={freeView ? pan.y : 0}
      zoom={freeView ? zoom : 1}
      {...bindCanvas()}
    >
      {children}
    </Canvas>
  )
};
