import React, {
  FunctionComponent,
  ReactElement,
  useEffect,
} from 'react';
import { CanvasObject, useCanvas, Coords } from '../../Canvas';
import { useDimensions } from '../../common';
import { useCanvasViewOptionsContext } from '../CanvasViewOptionsProvider';
import { Box } from './Box';

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
