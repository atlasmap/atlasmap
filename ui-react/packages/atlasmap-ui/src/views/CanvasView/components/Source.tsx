import React, { FunctionComponent, ReactElement } from 'react';
import { useCanvasViewLayoutContext } from '../CanvasViewLayoutProvider';
import { FieldsBox } from './FieldsBox';

export interface ISourceProps {
  header: ReactElement | string;
}

export const Source: FunctionComponent<ISourceProps> = ({ children, header }) => {
  const {
    sourceWidth,
    boxHeight,
    initialSourceCoords,
  } = useCanvasViewLayoutContext();
  return (
    <FieldsBox
      id={'sources'}
      initialWidth={sourceWidth}
      initialHeight={boxHeight}
      position={initialSourceCoords}
      header={header}
    >
      {children}
    </FieldsBox>
  );
};
