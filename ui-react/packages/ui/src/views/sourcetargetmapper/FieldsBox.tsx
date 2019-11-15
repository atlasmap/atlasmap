import { Title } from '@patternfly/react-core';
import React, { forwardRef, HTMLAttributes, useRef } from 'react';
import { CanvasObject } from '../../canvas';
import { Coords, ElementType, IFieldsGroup } from '../../models';
import { Box } from './Box';
import { FieldGroup } from './FieldGroup';
import { FieldGroupList } from './FieldGroupList';

export interface IFieldsBoxProps extends HTMLAttributes<HTMLDivElement> {
  width: number;
  height: number;
  position: Coords;
  scrollable: boolean;
  fields: IFieldsGroup[];
  type: ElementType;
  title: string;
  rightAlign?: boolean;
}
export const FieldsBox = forwardRef<HTMLDivElement, IFieldsBoxProps>(({
  width,
  height,
  position,
  scrollable,
  fields,
  type,
  title,
  rightAlign = false,
  ...props
}, ref) => {
  const fieldsRef = useRef<HTMLDivElement | null>(null);
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
            <Title size={'2xl'} headingLevel={'h2'}>
              {title}
            </Title>
          }
          rightAlign={rightAlign}
          ref={fieldsRef}
        >
          <FieldGroupList>
            {fields.map(s => {
              return (
                <FieldGroup
                  isVisible={true}
                  group={s}
                  key={s.id}
                  boxRef={fieldsRef.current}
                  type={type}
                  rightAlign={rightAlign}
                />
              );
            })}
          </FieldGroupList>
        </Box>
      </div>
    </CanvasObject>
  )
});
