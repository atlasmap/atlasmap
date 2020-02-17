import React, {
  FunctionComponent,
  HTMLAttributes,
  SVGProps,
  useEffect,
  useState,
} from 'react';
import { useMovable } from './useMovable';
import { Coords } from './models';
import { css, StyleSheet } from '@patternfly/react-styles';

const styles = StyleSheet.create({
  foreignObject: { overflow: 'visible' },
});

export interface ICanvasObjectProps extends SVGProps<SVGForeignObjectElement> {
  id: string;
  width: number;
  height: number;
  x: number;
  y: number;
  movable?: boolean;
  childrenProps?: HTMLAttributes<HTMLDivElement>;
}

export const CanvasObject: FunctionComponent<ICanvasObjectProps> = ({
  id,
  children,
  width,
  height,
  x,
  y,
  movable = true,
  childrenProps = {},
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
      className={css(styles.foreignObject)}
      {...props}
    >
      <div {...bind()} {...childrenProps}>
        {children}
      </div>
    </foreignObject>
  );
};
