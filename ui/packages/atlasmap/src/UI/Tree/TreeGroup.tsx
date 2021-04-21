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
import {
  AccordionContent,
  AccordionItem,
  AccordionToggle,
} from '@patternfly/react-core';
import React, {
  FunctionComponent,
  MouseEvent,
  PropsWithChildren,
  ReactNode,
  forwardRef,
  useCallback,
  useRef,
  useState,
} from 'react';

import { css } from '@patternfly/react-styles';
import styles from './TreeGroup.module.css';
import { useTreeFocus } from './TreeFocusProvider';

export interface ITreeGroupProps {
  id: string;
  level: number;
  setSize: number;
  position: number;
  expanded: boolean;
  onToggleExpand: (expand?: boolean) => Promise<void>;
  renderLabel: (props: {
    expanded: boolean;
    focused: boolean;
    isLoading: boolean;
  }) => ReactNode;
  children: (props: { expanded: boolean; focused: boolean }) => ReactNode;
}

export const TreeGroup = forwardRef<
  HTMLDivElement,
  PropsWithChildren<ITreeGroupProps>
>(function TreeGroup(
  {
    id,
    expanded,
    level,
    setSize,
    position,
    onToggleExpand,
    renderLabel,
    children,
  },
  ref,
) {
  const divRef = useRef<HTMLDivElement | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const toggleExpand = useCallback(
    (expand?: boolean) => {
      setIsLoading(true);
      onToggleExpand(expand).then(() => {
        setIsLoading(false);
      });
    },
    [onToggleExpand],
  );
  const toggleExpandNoPropagation = useCallback(
    (event: MouseEvent) => {
      event.stopPropagation();
      toggleExpand();
    },
    [toggleExpand],
  );

  const expand = useCallback(() => toggleExpand(true), [toggleExpand]);
  const collapse = useCallback(() => toggleExpand(false), [toggleExpand]);

  const { focused, handlers: focusHandlers } = useTreeFocus({
    ref: divRef,
    isExpandable: true,
    isExpanded: expanded,
    collapseTreeitem: collapse,
    expandTreeitem: expand,
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

  const Component: FunctionComponent = ({ children }) => (
    <span role={'heading'} aria-level={level + 2}>
      {children}
    </span>
  );

  return (
    <div
      ref={handleRef}
      role={'treeitem'}
      aria-level={level}
      aria-setsize={setSize}
      aria-posinset={position}
      aria-expanded={expanded}
      {...focusHandlers}
      onClick={(event) => {
        focusHandlers.onClick(event);
        if (!event.isPropagationStopped()) {
          toggleExpand();
        }
      }}
    >
      <AccordionItem>
        <AccordionToggle
          isExpanded={expanded}
          id={`${id}-toggle`}
          data-testid={`${id}-toggle`}
          className={styles.button}
          tabIndex={-1}
          component={Component}
          onClick={toggleExpandNoPropagation}
        >
          {renderLabel({
            expanded: expanded,
            focused: focused || false,
            isLoading: isLoading,
          })}
        </AccordionToggle>
        <AccordionContent
          isHidden={!expanded}
          className={css(styles.content, !expanded && styles.hiddenContent)}
          role="group"
        >
          {children({ expanded: expanded, focused: focused || false })}
        </AccordionContent>
      </AccordionItem>
    </div>
  );
});
