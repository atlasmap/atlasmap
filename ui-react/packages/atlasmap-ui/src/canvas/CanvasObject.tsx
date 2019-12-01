import React, { FunctionComponent, HTMLAttributes, useEffect, useState } from 'react';
import { Coords } from '../models';
import { useMovable } from './useMovable';

export interface ICanvasObjectProps extends HTMLAttributes<SVGForeignObjectElement> {
  id: string;
  width: number;
  height: number;
  x: number;
  y: number;
  movable?: boolean;
}
export const CanvasObject: FunctionComponent<ICanvasObjectProps> = ({
  id,
  children,
  width,
  height,
  x,
  y,
  movable = true,
  ...props
}) => {
  const [coords, setCoords] = useState<Coords>({ x, y });
  useEffect(() => {
    setCoords({ x, y });
  }, [x, y]);
  const bind = useMovable({
    id,
    enabled: movable,
    initialPosition: coords,
    width,
    height,
    onDrag: (coords: Coords) => {
      setCoords(coords);
    },
    // xBoundaries: [-Infinity, mappingCoords.x - sourceTargetBoxesWidth - gutter],
  });
  return (
    <foreignObject
      width={width}
      height={height}
      x={movable ? coords.x : x}
      y={movable ? coords.y : y}
      {...props}
    >
      <div {...bind()} style={{ height: '100%' }}>
        {children}
      </div>
    </foreignObject>
  );
};
