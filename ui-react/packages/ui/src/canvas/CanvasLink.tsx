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
  width = 2,
}) => {
  const s = width;
  const r = 10;

  const csx = start.x;
  const cex = end.x;

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
    <g>
      <path d={d} stroke={color} strokeWidth={s} fill={'none'} />
      <rect
        x={csx - r / 2}
        y={start.y - r / 2}
        width={r}
        height={r}
        stroke={color}
        strokeWidth={s}
        fill={'#ffffff'}
        transform={`rotate(45 ${csx} ${start.y})`}
      />
      <rect
        x={cex - r / 2}
        y={end.y - r / 2}
        width={r}
        height={r}
        stroke={color}
        strokeWidth={s}
        fill={'#ffffff'}
        transform={`rotate(45 ${cex} ${end.y})`}
      />
    </g>
  ) : null;
};
