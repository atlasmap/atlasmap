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
  FocusEvent,
  FunctionComponent,
  KeyboardEvent,
  MouseEvent as ReactMouseEvent,
  RefObject,
  createContext,
  useContext,
  useState,
} from 'react';

interface ITreeFocusContext {
  focusedItem: HTMLElement | null;
  setFocusedItem: (item: HTMLElement | null) => void;
}

const TreeFocusContext = createContext<ITreeFocusContext | null>(null);

export const TreeFocusProvider: FunctionComponent = ({ children }) => {
  const [focusedItem, setFocusedItem] = useState<HTMLElement | null>(null);
  return (
    <TreeFocusContext.Provider value={{ focusedItem, setFocusedItem }}>
      {children}
    </TreeFocusContext.Provider>
  );
};

export interface IUseTreeFocusProps {
  ref: RefObject<HTMLDivElement | null>;
  isExpandable?: boolean;
  isExpanded?: boolean;
  collapseTreeitem?: () => void;
  expandTreeitem?: () => void;
}

export const useTreeFocus = ({
  ref,
  isExpandable = false,
  isExpanded = false,
  collapseTreeitem,
  expandTreeitem,
}: IUseTreeFocusProps) => {
  const context = useContext(TreeFocusContext);
  if (!context) {
    throw new Error(`useTreeFocus can be used only inside a Tree component`);
  }
  const { focusedItem, setFocusedItem } = context;
  const getFocusableItems = (el: HTMLElement) => {
    return Array.from<HTMLElement>(
      el.closest('[role=tree]')?.querySelectorAll('[role=treeitem]') || [],
    ).filter((el) => {
      const group = el.parentElement?.closest('[aria-expanded]');
      if (group) {
        return group.getAttribute('aria-expanded') === 'true';
      }
      return true;
    });
  };

  const setFocus = (index: number, nodes: HTMLElement[]) => {
    nodes.forEach((n) => n.setAttribute('tabindex', '-1'));
    nodes[index].setAttribute('tabindex', '0');
    nodes[index].focus();
  };

  const setFocusToPreviousItem = () => {
    if (ref.current) {
      const nodes = getFocusableItems(ref.current);
      const idx = nodes?.indexOf(ref.current);
      if (idx > 0) {
        setFocus(idx - 1, nodes);
      }
    }
  };
  const setFocusToNextItem = () => {
    if (ref.current) {
      const nodes = getFocusableItems(ref.current);
      const idx = nodes?.indexOf(ref.current);
      if (idx < nodes.length - 1) {
        setFocus(idx + 1, nodes);
      }
    }
  };
  // const setFocusToParentItem = () => {
  //   if (level > 1 && parentItem) {
  //     setFocus({ level: level - 1, position: parentItem.position });
  //   }
  // };
  const setFocusToFirstItem = () => {
    if (ref.current) {
      const nodes = getFocusableItems(ref.current);
      setFocus(0, nodes);
    }
  };
  const setFocusToLastItem = () => {
    if (ref.current) {
      const nodes = getFocusableItems(ref.current);
      setFocus(nodes.length - 1, nodes);
    }
  };
  const onKeyDown = (event: KeyboardEvent) => {
    // don't handle keyboard events performed on child elements
    if (event.target !== ref.current) {
      return;
    }
    // ignore special keys
    if (event.altKey || event.ctrlKey || event.metaKey) {
      return;
    }
    switch (event.key) {
      case ' ':
      case 'Enter':
        if (event.target === ref.current) {
          // Create simulated mouse event to mimic the behavior of ATs
          // and let the event handler handleClick do the housekeeping.
          event.target.dispatchEvent(
            new MouseEvent('click', {
              view: window,
              bubbles: true,
              cancelable: true,
            }),
          );
          setFocusedItem(ref.current);
        }
        break;
      case 'Escape':
        setFocusedItem(null);
        event.preventDefault();
        break;
      case 'ArrowUp':
        setFocusToPreviousItem();
        break;
      case 'ArrowDown':
        setFocusToNextItem();
        break;
      case 'ArrowRight':
        if (isExpandable) {
          if (isExpanded) {
            setFocusToNextItem();
          } else {
            if (expandTreeitem) {
              expandTreeitem();
            }
          }
        }
        break;
      case 'ArrowLeft':
        if (isExpandable && isExpanded) {
          if (collapseTreeitem) {
            collapseTreeitem();
          }
        } else {
          // if (itemLevel === level) {
          //   setFocusToParentItem();
          // }
        }
        break;
      case 'Home':
        setFocusToFirstItem();
        break;
      case 'End':
        setFocusToLastItem();
        break;
      default:
        break;
    }
  };
  const onClick = (event: ReactMouseEvent<HTMLElement>) => {
    if (ref.current) {
      if (isExpandable && isExpanded) {
        event.stopPropagation();
      } else {
        const nodes = getFocusableItems(ref.current);
        const idx = nodes?.indexOf(ref.current);
        setFocus(idx, nodes);
        setFocusedItem(ref.current);
      }
    }
  };
  return {
    focused: focusedItem && focusedItem === ref.current,
    handlers: {
      onKeyDown,
      onClick,
      onBlur: (event: FocusEvent<HTMLElement>) => {
        if (
          (event.relatedTarget as HTMLElement | null)?.closest(
            '[role=tree]',
          ) !== ref.current?.closest('[role=tree]')
        ) {
          setFocusedItem(null);
        }
      },
    },
  };
};
