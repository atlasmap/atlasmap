import React, { FunctionComponent } from "react";

import { Coords } from "./models";

export interface IArcEdgeProps {
  start: Coords;
  end: Coords;
  strokeWidth?: number;
  color?: string;
}
export const ArcEdge: FunctionComponent<IArcEdgeProps> = ({
  start,
  end,
  strokeWidth,
  color,
}) => {
  return (
    <line
      x1={start.x}
      y1={start.y}
      x2={end.x}
      y2={end.y}
      stroke={color}
      strokeWidth={strokeWidth}
    />
  );
};
