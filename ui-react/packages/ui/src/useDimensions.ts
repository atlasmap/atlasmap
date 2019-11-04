import { useState, useCallback, useLayoutEffect, useRef, MutableRefObject } from 'react';

function getDimensionObject(node: HTMLElement) {
  const rect = node.getBoundingClientRect();

  return {
    width: rect.width,
    height: rect.height,
    top: "x" in rect ? rect.x : rect.top,
    left: "y" in rect ? rect.y : rect.left,
    x: "x" in rect ? rect.x : rect.left,
    y: "y" in rect ? rect.y : rect.top,
    right: rect.right,
    bottom: rect.bottom
  };
}

export type Dimension = ReturnType<typeof getDimensionObject>;

export interface UseDimensionsArgs {
  liveMeasure?: boolean;
}

export const useDimensions = ({
   liveMeasure = true
 }: UseDimensionsArgs = {}): [
  MutableRefObject<HTMLDivElement | null>,
  Dimension
] => {
  const [dimensions, setDimensions] = useState<Dimension>({
    width: 0,
    height: 0,
    top: 0,
    left: 0,
    x: 0,
    y: 0,
    right: 0,
    bottom: 0,
  });
  const ref = useRef<HTMLDivElement>(null);

  const measure = useCallback(() =>
    window.requestAnimationFrame(() => {
      if (ref.current) {
        setDimensions(getDimensionObject(ref.current))
      }
    }),
    [ref, setDimensions]
  );

  useLayoutEffect(() => {
    measure();

    if (liveMeasure) {
      window.addEventListener("resize", measure);
      window.addEventListener("scroll", measure);
    }
    return () => {
      if (liveMeasure) {
        window.removeEventListener("resize", measure);
        window.removeEventListener("scroll", measure);
      }
    };
  }, [liveMeasure, measure]);

  return [ref, dimensions];
};

