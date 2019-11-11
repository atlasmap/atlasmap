import { useState, useCallback, useLayoutEffect, useRef, MutableRefObject } from 'react';

export interface UseDimensionsArgs {
  liveMeasure?: boolean;
}

export const useDimensions = ({
   liveMeasure = true
 }: UseDimensionsArgs = {}): [
  MutableRefObject<HTMLDivElement | null>,
  ClientRect | DOMRect,
  () => void
] => {
  const [dimensions, setDimensions] = useState<ClientRect | DOMRect>({
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

  const measure = useCallback(() => {
      const requestId = requestAnimationFrame(() => {
        if (ref.current) {
          setDimensions(ref.current.getBoundingClientRect())
        }
      });
      return () => {
        cancelAnimationFrame(requestId);
      }
    },
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

  return [ref, dimensions, measure];
};

