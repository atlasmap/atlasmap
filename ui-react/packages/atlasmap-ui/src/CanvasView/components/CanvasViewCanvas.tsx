import React, { FunctionComponent } from 'react';
import { Canvas } from '../../Canvas';
import { useCanvasViewOptionsContext } from '../CanvasViewOptionsProvider';
import { useCanvasViewContext } from '../CanvasViewCanvasProvider';
import { useDimensions } from '../../common';
import { css, StyleSheet } from '@patternfly/react-styles';

const styles = StyleSheet.create({
  canvasWrapper: {
    height: '100%',
    flex: '1',
    overflow: 'hidden',
  },
});

export const CanvasViewCanvas: FunctionComponent = ({ children }) => {
  const { isPanning, pan, zoom, bindCanvas } = useCanvasViewContext();
  const { freeView } = useCanvasViewOptionsContext();
  const [dimensionsRef, { width, height, top, left }] = useDimensions();
  return (
    <div ref={dimensionsRef} className={css(styles.canvasWrapper)}>
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
  );
};
