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
          height: scrollable ? '100%' : undefined
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
              overflowY: 'auto'
            }}
            {...props}
          >
          {mappings.map(m => {
            return (
              <MappingElement
                node={{
                  id: m.id,
                  element: <>{`${m.sourceFields.length}:${m.targetFields.length}`}</>
                }}
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
