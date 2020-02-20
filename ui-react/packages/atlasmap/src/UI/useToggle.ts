import { useState, useCallback } from "react";

type OnToggleReturnType = Promise<boolean> | boolean;

export function useToggle(
  initialState: boolean,
  onToggle?: (toggled: boolean) => OnToggleReturnType,
) {
  const [state, setState] = useState(initialState);
  const toggle = useCallback(async () => {
    const newState = onToggle ? await onToggle(!state) : !state;
    setState(newState);
  }, [onToggle, state]);
  const toggleOff = () => setState(false);
  const toggleOn = () => setState(true);
  return { state, toggle, toggleOff, toggleOn };
}
