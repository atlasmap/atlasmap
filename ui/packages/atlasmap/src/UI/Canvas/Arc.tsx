import { linkHorizontal, linkVertical } from "d3-shape";
import React, { FunctionComponent, SVGAttributes, useMemo } from "react";

import { css, StyleSheet } from "@patternfly/react-styles";

import { Coords } from "./models";
import { useToggle } from "../useToggle";

const styles = StyleSheet.create({
  clickable: {
    cursor: "pointer",
  },
});

export interface ILinkEdgeProps {
  start: Coords;
  end: Coords;
  strokeWidth?: number;
  color?: string;
}

const ArcEdge: FunctionComponent<ILinkEdgeProps> = ({
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

export interface IArcProps extends Omit<SVGAttributes<SVGPathElement>, "end"> {
  start: Coords;
  end: Coords;
  startSideSize?: number;
  endSideSize?: number;
  type?: "horizontal" | "vertical";
  strokeWidth?: number;
  color?: string;
  hoveredColor?: string;
  sideStrokeWidth?: number;
}

export const Arc: FunctionComponent<IArcProps> = ({
  start,
  end,
  type = "horizontal",
  color = "grey",
  hoveredColor = color,
  strokeWidth = 2,
  startSideSize = strokeWidth,
  endSideSize = strokeWidth,
  sideStrokeWidth = 5,
  className,
  ...props
}) => {
  const {
    state: hovered,
    toggleOn: toggleHoverOn,
    toggleOff: toggleHoverOff,
  } = useToggle(false);
  const s = strokeWidth;
  const appliedColor = hovered ? hoveredColor : color;

  const link = useMemo(
    () =>
      (type === "horizontal" ? linkHorizontal : linkVertical)<
        any,
        { start: Coords; end: Coords },
        Coords
      >()
        .context(null)
        .source((d) => d.start)
        .target((d) => d.end)
        .x((d) => d.x)
        .y((d) => d.y),
    [type],
  );

  const d = link({ start, end });

  startSideSize = startSideSize / 2;
  endSideSize = endSideSize / 2;
  return d ? (
    <g onMouseEnter={toggleHoverOn} onMouseLeave={toggleHoverOff}>
      <path d={d} stroke={appliedColor} strokeWidth={s} fill={"none"} />
      <path
        d={d}
        stroke={"transparent"}
        strokeWidth={20}
        fill={"none"}
        className={css(props.onClick && styles.clickable)}
        {...props}
      />
      <ArcEdge
        start={
          type === "horizontal"
            ? {
                x: start.x - sideStrokeWidth / 2,
                y: start.y - startSideSize + 1,
              }
            : {
                x: start.x - startSideSize + 1,
                y: start.y - sideStrokeWidth / 2,
              }
        }
        end={
          type === "horizontal"
            ? {
                x: start.x - sideStrokeWidth / 2,
                y: start.y + startSideSize - 1,
              }
            : {
                x: start.x + startSideSize - 1,
                y: start.y - sideStrokeWidth / 2,
              }
        }
        color={appliedColor}
        strokeWidth={sideStrokeWidth}
      />
      <ArcEdge
        start={
          type === "horizontal"
            ? { x: end.x + sideStrokeWidth / 2, y: end.y - endSideSize + 1 }
            : { x: end.x - endSideSize + 1, y: end.y + sideStrokeWidth / 2 }
        }
        end={
          type === "horizontal"
            ? { x: end.x + sideStrokeWidth / 2, y: end.y + endSideSize - 1 }
            : { x: end.x + endSideSize - 1, y: end.y + sideStrokeWidth / 2 }
        }
        color={appliedColor}
        strokeWidth={sideStrokeWidth}
      />
    </g>
  ) : null;
};
