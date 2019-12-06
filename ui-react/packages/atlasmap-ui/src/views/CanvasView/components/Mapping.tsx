import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, ReactElement, useRef } from 'react';
import { useCanvasViewLayoutContext } from '../CanvasViewLayoutProvider';
import { useCanvasViewOptionsContext } from '../CanvasViewOptionsProvider';
import { FieldsBox } from './FieldsBox';

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
  const { materializedMappings } = useCanvasViewOptionsContext();
  const { mappingWidth, boxHeight, initialMappingCoords } = useCanvasViewLayoutContext();
  const ref = useRef<HTMLDivElement | null>(null);
  return (
    <FieldsBox
      id={'mappings'}
      initialWidth={mappingWidth}
      initialHeight={boxHeight}
      position={initialMappingCoords}
      header={'Mapping'}
      hidden={!materializedMappings}
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
