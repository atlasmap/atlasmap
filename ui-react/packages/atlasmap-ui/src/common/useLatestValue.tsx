import { ReactNode, useCallback, useRef, useState } from 'react';

export function useLatestValue(): [ReactNode | undefined, (el: ReactNode) => void] {
  const [element, setElement] = useState<ReactNode | undefined>();
  const previousElement = useRef<ReactNode | null>();
  const handleSetElement = useCallback(
    (newElement: ReactNode) => {
      if (previousElement.current !== newElement) {
        previousElement.current = newElement;
        setElement(previousElement.current);
      }
    },
    [setElement, previousElement]
  );
  return [element, handleSetElement];
}