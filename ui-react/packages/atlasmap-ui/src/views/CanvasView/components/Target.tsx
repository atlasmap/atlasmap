import React, { FunctionComponent, ReactElement } from 'react';
import { useCanvasViewLayoutContext } from '../CanvasViewLayoutProvider';
import { FieldsBox } from './index';

export interface ITargetProps {
  header: ReactElement | string;
}

export const Target: FunctionComponent<ITargetProps> = ({ children, header }) => {
  const { targetWidth, boxHeight, initialTargetCoords } = useCanvasViewLayoutContext();
  return (
    <FieldsBox
      id={'targets'}
      initialWidth={targetWidth}
      initialHeight={boxHeight}
      position={initialTargetCoords}
      header={header}
      rightAlign={true}
      hidden={false}
    >
      {children}
    </FieldsBox>
  )
};
