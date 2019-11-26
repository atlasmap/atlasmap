import { Title } from '@patternfly/react-core';
import React, { FunctionComponent, HTMLAttributes, useEffect } from 'react';
import { CanvasObject, useCanvas } from '../../canvas';
import { useDimensions } from '../../common';
import { Coords } from '../../models';
import { Box } from './Box';

export interface IMappingsBoxProps extends HTMLAttributes<HTMLDivElement> {
  width: number;
  height?: number;
  position: Coords;
  scrollable: boolean;
  title: string;
  rightAlign?: boolean;
  hidden?: boolean;
}
export const FieldsBox: FunctionComponent<IMappingsBoxProps> = ({
  width,
  height,
  position,
  scrollable,
  title,
  rightAlign = false,
  hidden = false,
  children,
  ...props
}) => {
  const [ref, dimensions, measure ] = useDimensions();
  const { yDomain, addRedrawListener, removeRedrawListener } = useCanvas();

  useEffect(() => {
    addRedrawListener(measure);
    return () => {
      removeRedrawListener(measure);
    };
  }, [
    addRedrawListener,
    removeRedrawListener,
    measure,
  ]);

  return (
    <CanvasObject
      id={title}
      width={width}
      height={height || yDomain(dimensions.height)}
      movable={!scrollable}
      {...position}
    >
      <div
        ref={ref}
        style={{
          height: scrollable ? '100%' : undefined,
          opacity: hidden ? 0 : 1,
          padding: '0 0.5rem'
        }}
        {...props}
      >
        <Box
          header={
            <Title size={'2xl'} headingLevel={'h2'} style={{ textAlign: 'center' }}>
              {title}
            </Title>
          }
          rightAlign={rightAlign}
          style={{
            alignItems: 'center',
          }}
        >
          <div
            ref={ref}
            style={{
              height: scrollable ? '100%' : undefined,
              overflow: 'visible',
              display: 'flex',
              flexFlow: 'column',
              width: '100%',
            }}
            {...props}
          >
            {children}
          </div>
        </Box>
      </div>
    </CanvasObject>
  )
};
