import { Title } from '@patternfly/react-core';
import { CanvasObject } from '@src';
import { Coords, ElementType, IMappings } from '@src/models';
import { Box } from '@src/views/sourcetargetmapper/Box';
import { MappingElement } from '@src/views/sourcetargetmapper/MappingElement';
import React, { forwardRef, HTMLAttributes, useRef } from 'react';

export interface IFieldsBoxProps extends HTMLAttributes<HTMLDivElement> {
  width: number;
  height: number;
  position: Coords;
  scrollable: boolean;
  mappings: IMappings[];
  type: ElementType;
  title: string;
  rightAlign?: boolean;
}
export const MappingsBox = forwardRef<HTMLDivElement, IFieldsBoxProps>(({
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
          footer={<p>{mappings.length} mappings</p>}
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
