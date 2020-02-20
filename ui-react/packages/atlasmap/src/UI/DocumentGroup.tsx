import React, {
  forwardRef,
  MouseEvent,
  PropsWithChildren,
  ReactElement,
  useCallback,
  useState,
  useEffect,
  useRef,
  FunctionComponent,
} from "react";
import {
  AccordionContent,
  AccordionItem,
  AccordionToggle,
} from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";
import { FolderCloseIcon, FolderOpenIcon } from "@patternfly/react-icons";
import { useDocumentFocus } from "./DocumentFocusProvider";

const styles = StyleSheet.create({
  button: {
    paddingRight: "var(--pf-global--spacer--md) !important",
  },
  buttonContent: {
    display: "flex",
    alignItems: "center",
  },
  buttonIcon: {
    marginRight: "1rem",
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

export interface IDocumentGroupProps {
  id: string;
  name: string;
  icon?: ReactElement;
  type?: string;
  showType?: boolean;
  expanded?: boolean;
  level: number;
  setSize: number;
  position: number;
  onClick?: (event: MouseEvent) => void;
}

export const DocumentGroup = forwardRef<
  HTMLDivElement,
  PropsWithChildren<IDocumentGroupProps>
>(
  (
    {
      id,
      name,
      type,
      icon,
      showType = false,
      expanded,
      level,
      setSize,
      position,
      onClick,
      children,
    },
    ref,
  ) => {
    const divRef = useRef<HTMLDivElement | null>(null);
    const [isExpanded, setIsExpanded] = useState(false);
    const toggleExpand = useCallback(
      (event: MouseEvent) => {
        setIsExpanded(!isExpanded);
        if (onClick) {
          onClick(event);
        }
      },
      [isExpanded, onClick],
    );

    const expand = useCallback(() => setIsExpanded(true), []);
    const collapse = useCallback(() => setIsExpanded(false), []);

    useEffect(() => {
      if (expanded !== undefined) {
        setIsExpanded(expanded);
      }
    }, [expanded]);

    const { handlers: focusHandlers } = useDocumentFocus({
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
            toggleExpand(event);
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
          >
            <span className={css(styles.buttonContent)}>
              <span className={css(styles.buttonIcon)}>
                {icon ||
                  (isExpanded ? <FolderOpenIcon /> : <FolderCloseIcon />)}
              </span>
              {name}
              {showType && ` (${type})`}
            </span>
          </AccordionToggle>
          <AccordionContent
            isHidden={!isExpanded}
            className={css(styles.content, !isExpanded && styles.hiddenContent)}
            role="group"
          >
            {children}
          </AccordionContent>
        </AccordionItem>
      </div>
    );
  },
);
