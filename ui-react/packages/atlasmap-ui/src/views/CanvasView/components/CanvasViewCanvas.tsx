import React, { FunctionComponent } from 'react';
import { Canvas } from '../../../canvas';
import { useDimensions } from '../../../common';
import { useCanvasViewOptionsContext } from '../CanvasViewOptionsProvider';
import { useCanvasViewContext } from '../CanvasViewCanvasProvider';

export const CanvasViewCanvas: FunctionComponent = ({
  children
}) => {
  const { isPanning, pan, zoom, bindCanvas } = useCanvasViewContext();
  const { freeView } = useCanvasViewOptionsContext();
  const [dimensionsRef, { width, height, top, left }] = useDimensions();
  return (
    <div
      ref={dimensionsRef}
      style={{
        height: '100%',
        flex: '1',
        overflow: 'hidden',
      }}
    >
      <Canvas
        width={width}
        height={height}
        offsetLeft={left}
        offsetTop={top}
        allowPanning={freeView}
        isPanning={freeView ? isPanning : false}
        panX={freeView ? pan.x : 0}
        panY={freeView ? pan.y : 0}
        zoom={freeView ? zoom : 1}
        {...bindCanvas()}
      >
        {children}
      </Canvas>
    </div>
  )
};
