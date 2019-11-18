import { Title } from '@patternfly/react-core';
import React, { forwardRef, HTMLAttributes, useRef } from 'react';
import { CanvasObject } from '../../canvas';
import { Coords, ElementType, IMappings } from '../../models';
import { Box } from './Box';
import { MappingElement } from './MappingElement';

export interface IMappingsBoxProps extends HTMLAttributes<HTMLDivElement> {
  width: number;
  height: number;
  position: Coords;
  scrollable: boolean;
  mappings: IMappings[];
  type: ElementType;
  title: string;
  rightAlign?: boolean;
  hidden?: boolean;
}
export const MappingsBox = forwardRef<HTMLDivElement, IMappingsBoxProps>(({
  width,
  height,
  position,
  scrollable,
  mappings,
  type,
  title,
  rightAlign = false,
  hidden = false,
  ...props
}, ref) => {
  const mappingsRef = useRef<HTMLDivElement | null>(null);
  return (
    <CanvasObject
      width={width}
      height={height}
      {...position}
    >
      <div
        ref={ref}
        style={{
          height: scrollable ? '100%' : undefined,
          opacity: hidden ? 0 : 1
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
          ref={mappingsRef}
          style={{
            alignItems: 'center'
          }}
        >
          <div
            ref={ref}
            style={{
              height: scrollable ? '100%' : undefined,
              overflowY: 'auto',
              display: 'flex',
              flexFlow: 'column',
              width: '100%',
              padding: '0 0.5rem'
            }}
            {...props}
          >
          {mappings.map(m => {
            return (
              <MappingElement
                key={m.id}
                node={m}
                type={'mapping'}
                boxRef={mappingsRef.current}
              />
            );
          })}
          </div>
        </Box>
      </div>
    </CanvasObject>
  )
});
