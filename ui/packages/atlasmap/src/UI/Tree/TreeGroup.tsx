import React, {
  forwardRef,
  FunctionComponent,
  MouseEvent,
  PropsWithChildren,
  ReactNode,
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import {
  AccordionContent,
  AccordionItem,
  AccordionToggle,
} from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";

import { useTreeFocus } from "./TreeFocusProvider";

const styles = StyleSheet.create({
  button: {
    padding: "0 !important",
  },
  content: {
    fontSize: "inherit !important",
    "& > div": {
      boxSizing: "border-box",
      padding:
        "var(--pf-c-accordion__expanded-content-body--PaddingTop) 0 var(--pf-c-accordion__expanded-content-body--PaddingBottom) 1.2rem !important",
    },
  },
  hiddenContent: {
    display: "none",
  },
});

export interface ITreeGroupProps {
  id: string;
  level: number;
  setSize: number;
  position: number;
  expanded?: boolean;
  renderLabel: (props: { expanded: boolean; focused: boolean }) => ReactNode;
  children: (props: { expanded: boolean; focused: boolean }) => ReactNode;
}

export const TreeGroup = forwardRef<
  HTMLDivElement,
  PropsWithChildren<ITreeGroupProps>
>(({ id, expanded, level, setSize, position, renderLabel, children }, ref) => {
  const divRef = useRef<HTMLDivElement | null>(null);
  const [isExpanded, setIsExpanded] = useState(false);
  const toggleExpand = useCallback(() => {
    setIsExpanded(!isExpanded);
  }, [isExpanded]);
  const toggleExpandNoPropagation = useCallback(
    (event: MouseEvent) => {
      event.stopPropagation();
      toggleExpand();
    },
    [toggleExpand],
  );

  const expand = useCallback(() => setIsExpanded(true), []);
  const collapse = useCallback(() => setIsExpanded(false), []);

  useEffect(() => {
    if (expanded !== undefined) {
      setIsExpanded(expanded);
    }
  }, [expanded]);

  const { focused, handlers: focusHandlers } = useTreeFocus({
    ref: divRef,
    isExpandable: true,
    isExpanded,
    collapseTreeitem: collapse,
    expandTreeitem: expand,
  });

  const handleRef = (el: HTMLDivElement | null) => {
    divRef.current = el;
    if (ref) {
      if (typeof ref === "function") {
        ref(el);
      } else {
        // @ts-ignore
        ref.current = el;
      }
    }
  };

  const Component: FunctionComponent = ({ children }) => (
    <span role={"heading"} aria-level={level + 2}>
      {children}
    </span>
  );

  return (
    <div
      ref={handleRef}
      role={"treeitem"}
      aria-level={level}
      aria-setsize={setSize}
      aria-posinset={position}
      aria-expanded={isExpanded}
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
          isExpanded={isExpanded}
          id={`${id}-toggle`}
          className={css(styles.button)}
          tabIndex={-1}
          component={Component}
          onClick={toggleExpandNoPropagation}
        >
          {renderLabel({ expanded: isExpanded, focused: focused || false })}
        </AccordionToggle>
        <AccordionContent
          isHidden={!isExpanded}
          className={css(styles.content, !isExpanded && styles.hiddenContent)}
          role="group"
        >
          {children({ expanded: isExpanded, focused: focused || false })}
        </AccordionContent>
      </AccordionItem>
    </div>
  );
});
