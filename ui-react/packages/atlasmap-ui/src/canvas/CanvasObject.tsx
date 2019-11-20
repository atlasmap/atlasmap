import React, { FunctionComponent } from 'react';

export interface ICanvasObjectProps {
  width: number;
  height: number;
  x: number;
  y: number;
}
export const CanvasObject: FunctionComponent<ICanvasObjectProps> = ({
  children,
  width,
  height,
  x,
  y,
}) => {
  return (
    <foreignObject
      width={width}
      height={height}
      x={x}
      y={y}
    >
      {children}
    </foreignObject>
  );
};
