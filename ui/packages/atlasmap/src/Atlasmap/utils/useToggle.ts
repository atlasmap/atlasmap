import { useState, useCallback, useEffect } from "react";

type OnToggleReturnType = Promise<boolean> | boolean;

export function useToggle(
  initialState: boolean,
  onToggle?: (toggled: boolean) => OnToggleReturnType,
) {
  const [state, setState] = useState(initialState);
  useEffect(() => setState(initialState), [initialState]);
  const toggle = useCallback(async () => {
    const newState = onToggle ? await onToggle(!state) : !state;
    setState(newState);
  }, [onToggle, state]);
  const toggleOff = useCallback(() => setState(false), []);
  const toggleOn = useCallback(() => setState(true), []);
  return { state, toggle, toggleOff, toggleOn, setToggle: setState };
}
