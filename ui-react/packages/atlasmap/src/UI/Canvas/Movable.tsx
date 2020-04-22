import { css, StyleSheet } from "@patternfly/react-styles";
import { MutableRefObject, ReactElement, useState } from "react";
import { useMovable } from "./useMovable";
import { Coords } from "./models";
import { useDimensions } from "../useDimensions";
import { useBoundingCanvasRect } from "./useBoundingCanvasRect";

const styles = StyleSheet.create({
  movable: {
    cursor: "grab",
    "&:active": {
      cursor: "grabbing",
    },
  },
});

export interface IMovableChildren<T> {
  x?: number;
  y?: number;
  bind: ReturnType<typeof useMovable>;
  ref: MutableRefObject<T | null>;
  className?: string;
}

export interface IMovableProps<T> {
  id: string;
  enabled?: boolean;
  children: (props: IMovableChildren<T>) => ReactElement;
}

export function Movable<T = HTMLDivElement>({
  id,
  enabled = true,
  children,
}: IMovableProps<T>) {
  const { convertDOMRectToCanvasRect } = useBoundingCanvasRect();
  const [coords, setCoords] = useState<Coords | undefined>();
  const [ref, rect] = useDimensions<T>();
  const { width, height, left, top } = convertDOMRectToCanvasRect(rect);
  const bind = useMovable({
    id,
    enabled,
    initialPosition: coords ? coords : { x: left, y: top },
    width,
    height,
    onDrag: (coords: Coords) => {
      setCoords(coords);
    },
  });
  return children({
    ...coords,
    bind,
    ref,
    className: enabled ? css(styles.movable) : undefined,
  });
}
