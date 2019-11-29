import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, ReactElement, useEffect, useRef } from 'react';
import { useCanvas } from '../../../canvas';
import { useCanvasViewLayoutContext } from '../CanvasViewLayoutProvider';
import { FieldsBox } from './index';

const styles = StyleSheet.create({
  content: {
    overflowY: 'auto',
    overflowX: 'visible',
    flex: '1',
    height: '100%',
  },
});

export interface IMappingProps {
  children: (props: { ref: HTMLElement | null }) => ReactElement;
}

export const Mapping: FunctionComponent<IMappingProps> = ({ children }) => {
  const { mappingWidth, boxHeight, initialMappingCoords } = useCanvasViewLayoutContext();
  const { redraw } = useCanvas();
  const ref = useRef<HTMLDivElement | null>(null);
  useEffect(() => {
    if (ref.current) {
      redraw();
    }
  }, [redraw]);
  return (
    <FieldsBox
      id={'mappings'}
      initialWidth={mappingWidth}
      initialHeight={boxHeight}
      position={initialMappingCoords}
      header={'Mapping'}
      hidden={false}
    >
      <div
        className={css(styles.content)}
        ref={ref}
      >
        {children({ ref: ref.current })}
      </div>
    </FieldsBox>
  )
};
