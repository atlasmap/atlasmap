import { Coords } from '@src/models';
import { linkHorizontal } from 'd3-shape';
import React, { FunctionComponent, useMemo } from 'react';

export interface ICanvasLinkProps {
  start: Coords;
  end: Coords;
  width?: number;
  color?: string;
}

export const CanvasLink: FunctionComponent<ICanvasLinkProps> = ({
  start,
  end,
  color = 'grey',
  width = 3,
}) => {
  const link = useMemo(
    () =>
      linkHorizontal<any, { start: Coords; end: Coords }, Coords>()
        .context(null)
        .source(d => d.start)
        .target(d => d.end)
        .x(d => d.x)
        .y(d => d.y),
    []
  );

  const d = link({ start, end });

  return d ? (
    <path d={d} stroke={color} strokeWidth={width} fill={'none'} />
  ) : null;
};
