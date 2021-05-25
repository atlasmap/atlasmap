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
import React, {
  HTMLAttributes,
  PropsWithChildren,
  ReactNode,
  forwardRef,
  useRef,
} from 'react';

import { useTreeFocus } from './TreeFocusProvider';

export interface ITreeItemProps extends HTMLAttributes<HTMLDivElement> {
  level: number;
  setSize: number;
  position: number;
  children: (props: { focused: boolean }) => ReactNode;
}

export const TreeItem = forwardRef<
  HTMLDivElement,
  PropsWithChildren<ITreeItemProps>
>(function TreeItem({ level, setSize, position, children, ...props }, ref) {
  const divRef = useRef<HTMLDivElement | null>(null);
  const { focused, handlers: focusHandlers } = useTreeFocus({
    ref: divRef,
  });

  const handleRef = (el: HTMLDivElement | null) => {
    divRef.current = el;
    if (ref) {
      if (typeof ref === 'function') {
        ref(el);
      } else {
        // @ts-ignore
        ref.current = el;
      }
    }
  };
  return (
    <div
      ref={handleRef}
      role={'treeitem'}
      aria-level={level}
      aria-setsize={setSize}
      aria-posinset={position}
      {...focusHandlers}
      {...props}
    >
      {children({ focused: focused || false })}
    </div>
  );
});
