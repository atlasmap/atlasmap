import {
  useState,
  useCallback,
  useLayoutEffect,
  useRef,
  MutableRefObject,
} from 'react';
import { BrowserRect } from '../CanvasView';

const noSize = {
  width: 0,
  height: 0,
  top: 0,
  left: 0,
  x: 0,
  y: 0,
  right: 0,
  bottom: 0,
};

function areRectsDifferent(a: DOMRect, b: DOMRect) {
  return (
    a.width !== b.width ||
    a.height !== b.height ||
    a.top !== b.top ||
    a.left !== b.left ||
    a.x !== b.x ||
    a.y !== b.y ||
    a.right !== b.right ||
    a.bottom !== b.bottom
  );
}

export interface UseDimensionsArgs {
  liveMeasure?: boolean;
}

export function useDimensions<T = HTMLDivElement>({
  liveMeasure = true,
}: UseDimensionsArgs = {}): [
  MutableRefObject<T | null>,
  BrowserRect,
  () => void
] {
  const previousDimensions = useRef<BrowserRect>(noSize);
  const [dimensions, setDimensions] = useState<BrowserRect>(noSize);
  const ref = useRef<T>(null);

  const measure = useCallback(() => {
    if (ref.current) {
      const d = ((ref.current as unknown) as HTMLElement).getBoundingClientRect();
      if (
        areRectsDifferent(d as DOMRect, previousDimensions.current as DOMRect)
      ) {
        setDimensions(d);
        previousDimensions.current = d;
      }
    }
  }, [setDimensions]);

  useLayoutEffect(() => {
    measure();

    if (liveMeasure) {
      window.addEventListener('resize', measure);
      window.addEventListener('scroll', measure);
    }
    return () => {
      if (liveMeasure) {
        window.removeEventListener('resize', measure);
        window.removeEventListener('scroll', measure);
      }
    };
  }, [liveMeasure, measure]);

  return [ref, dimensions, measure];
}
