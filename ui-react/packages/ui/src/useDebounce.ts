import { useEffect, useRef, useState } from 'react';

export const useDebounce = (callback: () => unknown, delay: number) => {
  const latestCallback = useRef<typeof callback | null>(null);
  const [lastCalledAt, setLastCalledAt] = useState<number | null>(null);

  useEffect(() => {
    latestCallback.current = callback;
  }, [callback]);

  useEffect(() => {
    if (lastCalledAt) {
      const fire = () => {
        setLastCalledAt(null);
        latestCallback.current && latestCallback.current();
      };

      const fireAt = lastCalledAt + delay;
      const id = setTimeout(fire, fireAt - Date.now());
      return () => clearTimeout(id);
    }
    return;
  }, [lastCalledAt, latestCallback, delay]);

  return () => setLastCalledAt(Date.now());
};