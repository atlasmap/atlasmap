import React, { FunctionComponent, ReactElement, useEffect } from 'react';
import { CanvasObject, useCanvas, Coords } from '../../Canvas';
import { useDimensions } from '../../common';
import { useCanvasViewOptionsContext } from '../CanvasViewOptionsProvider';
import { Box } from './Box';
import { css, StyleSheet } from '@patternfly/react-styles';

const styles = StyleSheet.create({
  boxWrapper: { height: '100%' },
});

export interface IMappingsBoxProps {
  id: string;
  initialWidth: number;
  initialHeight: number;
  position: Coords;
  header: ReactElement | string;
  visible?: boolean;
  rightAlign?: boolean;
}
export const FieldsBox: FunctionComponent<IMappingsBoxProps> = ({
  id,
  initialWidth,
  initialHeight,
  position,
  header,
  rightAlign = false,
  visible = true,
  children,
}) => {
  const { freeView } = useCanvasViewOptionsContext();
  const scrollable = !freeView;

  const [ref, dimensions, measure] = useDimensions();
  const { yDomain, addRedrawListener, removeRedrawListener } = useCanvas();

  useEffect(() => {
    addRedrawListener(measure);
    return () => {
      removeRedrawListener(measure);
    };
  }, [addRedrawListener, removeRedrawListener, measure]);

  return (
    <CanvasObject
      id={id}
      width={initialWidth}
      height={scrollable ? initialHeight : yDomain(dimensions.height)}
      movable={!scrollable}
      childrenProps={{
        className: css(scrollable && styles.boxWrapper)
      }}
      {...position}
    >
      <Box
        header={header}
        rightAlign={rightAlign}
        scrollable={scrollable}
        visible={visible}
        ref={ref}
      >
        {children}
      </Box>
    </CanvasObject>
  );
};
