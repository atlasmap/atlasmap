import React, { FunctionComponent, HTMLAttributes } from 'react';

export interface ICanvasObjectProps extends HTMLAttributes<SVGForeignObjectElement> {
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
  ...props
}) => {
  return (
    <foreignObject
      width={width}
      height={height}
      x={x}
      y={y}
      {...props}
    >
      {children}
    </foreignObject>
  );
};
