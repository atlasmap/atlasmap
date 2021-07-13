/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import { useCallback, useEffect, useState } from 'react';

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
