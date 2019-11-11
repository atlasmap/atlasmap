import { useCanvasInfo } from '@src/canvas/CanvasContext';
import { CanvasLinkCoord } from '@src/models';
import { linkHorizontal } from 'd3-shape';
import React, { FunctionComponent, useMemo } from 'react';


export interface ICanvasLinkProps {
  start: CanvasLinkCoord;
  end: CanvasLinkCoord;
  width?: number;
  color?: string;
}

export const CanvasLink: FunctionComponent<ICanvasLinkProps> = ({
  start,
  end,
  color = 'grey',
  width = 3,
}) => {
  const { xDomain, yDomain } = useCanvasInfo();

  const link = useMemo(
    () =>
      linkHorizontal<any, { start: CanvasLinkCoord; end: CanvasLinkCoord }, CanvasLinkCoord>()
        .context(null)
        .source(d => d.start)
        .target(d => d.end)
        .x(d => xDomain.invert(d.x))
        .y(d => yDomain.invert(d.y)),
    [xDomain, yDomain]
  );

  const d = link({ start, end });

  return d ? (
    <path d={d} stroke={color} strokeWidth={width} fill={'none'} />
  ) : null;
};
