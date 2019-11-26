import {
  useState,
  useCallback,
  useLayoutEffect,
  useRef,
  MutableRefObject,
} from 'react';
import { BrowserRect } from '../models';

export interface UseDimensionsArgs {
  liveMeasure?: boolean;
}

export function useDimensions<T = HTMLDivElement>({
  liveMeasure = true,
}: UseDimensionsArgs = {}): [MutableRefObject<T | null>, BrowserRect, () => void] {
  const [dimensions, setDimensions] = useState<BrowserRect>({
    width: 0,
    height: 0,
    top: 0,
    left: 0,
    x: 0,
    y: 0,
    right: 0,
    bottom: 0,
  });
  const ref = useRef<T>(null);

  const measure = useCallback(() => {
    if (ref.current) {
      setDimensions(
        ((ref.current as unknown) as HTMLElement).getBoundingClientRect()
      );
    }
  }, [ref, setDimensions]);

  useLayoutEffect(() => {
    const requestId = requestAnimationFrame(() => {
      measure();
    });

    if (liveMeasure) {
      window.addEventListener('resize', measure);
      window.addEventListener('scroll', measure);
    }
    return () => {
      if (liveMeasure) {
        window.removeEventListener('resize', measure);
        window.removeEventListener('scroll', measure);
      }
      if (requestId) {
        cancelAnimationFrame(requestId);
      }
    };
  }, [liveMeasure, measure]);

  return [ref, dimensions, measure];
}
