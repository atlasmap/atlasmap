import React, {
  forwardRef,
  HTMLAttributes,
  MouseEvent,
  KeyboardEvent,
  PropsWithChildren,
  ReactNode,
  useCallback,
  useEffect,
  useRef,
  ReactElement,
  useLayoutEffect,
} from "react";

import {
  Accordion,
  Card,
  CardActions,
  CardBody,
  CardHead,
  CardHeader,
  Title,
} from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";
import { DocumentFocusProvider } from "./DocumentFocusProvider";

const styles = StyleSheet.create({
  card: {},
  stacked: {
    margin: "1rem 0",
  },
  head: {
    paddingRight: "0 !important",
  },
  body: {
    padding: "0 !important",
  },
  hidden: {
    display: "none",
  },
  dropTarget: {
    "&:before": {
      background: "var(--pf-global--active-color--400) !important",
    },
  },
  dropAccepted: {
    "&:before": {
      background: "var(--pf-global--success-color--100) !important",
    },
  },
  accordion: {
    padding: "0 !important",
  },
});

export interface IDocumentProps
  extends Omit<HTMLAttributes<HTMLDivElement>, "title"> {
  title?: string;
  actions?: (ReactElement | null)[];
  footer?: ReactNode;
  selected?: boolean;
  selectable?: boolean;
  dropTarget?: boolean;
  dropAccepted?: boolean;
  stacked?: boolean;
  scrollIntoView?: boolean;
  onSelect?: () => void;
  onDeselect?: () => void;
}

export const Document = forwardRef<
  HTMLDivElement,
  PropsWithChildren<IDocumentProps>
>(
  (
    {
      id,
      title,
      actions,
      footer,
      selected = false,
      dropTarget = false,
      dropAccepted = false,
      stacked = true,
      selectable = false,
      scrollIntoView = false,
      onSelect,
      onDeselect,
      children,
      ...props
    },
    ref,
  ) => {
    const documentRef = useRef<HTMLDivElement | null>(null);
    const handleClick = useCallback(
      (event: MouseEvent) => {
        event.stopPropagation();
        if (onSelect) {
          onSelect();
        }
      },
      [onSelect],
    );
    const handleKey = useCallback(
      (event: KeyboardEvent) => {
        event.stopPropagation();

        switch (event.key) {
          case "Enter":
          case "Space":
            if (onSelect) {
              onSelect();
            }
            break;
          case "Escape":
            if (onDeselect) {
              onDeselect();
            }
            break;
        }
      },
      [onDeselect, onSelect],
    );
    const makeCardSelected = selected || dropTarget || dropAccepted;

    const handleRef = (el: HTMLDivElement | null) => {
      if (ref) {
        if (typeof ref === "function") {
          ref(el);
        } else {
          // @ts-ignore
          // by default forwardedRef.current is readonly. Let's ignore it
          ref.current = el;
        }
      }
      documentRef.current = el;
    };

    useEffect(() => {
      if (scrollIntoView && documentRef.current) {
        documentRef.current.scrollIntoView();
      }
    }, [scrollIntoView]);

    useLayoutEffect(() => {
      if (documentRef.current) {
        const itemInTabSequence = documentRef.current.querySelector(
          `[role=treeitem][tabindex="0"]`,
        );
        if (!itemInTabSequence) {
          const firstTreeItem = documentRef.current.querySelector(
            "[role=treeitem]",
          );
          if (firstTreeItem) {
            firstTreeItem.setAttribute("tabindex", "0");
          }
        }
      }
    });

    return (
      <DocumentFocusProvider>
        <div
          className={css(stacked && styles.stacked)}
          ref={handleRef}
          onClick={handleClick}
          onKeyDown={handleKey}
          {...props}
        >
          <Card
            isCompact={true}
            className={css(
              styles.card,
              dropAccepted && !dropTarget && styles.dropAccepted,
              dropTarget && styles.dropTarget,
            )}
            isSelected={makeCardSelected}
            isSelectable={makeCardSelected || selectable}
            aria-label={title}
          >
            {(title || actions) && (
              <CardHead className={css(styles.head)}>
                <CardActions>{actions?.filter((a) => a)}</CardActions>
                {title && (
                  <CardHeader>
                    <Title size={"lg"} headingLevel={"h2"} aria-label={title}>
                      {title}
                    </Title>
                  </CardHeader>
                )}
              </CardHead>
            )}
            <CardBody className={css(styles.body)}>
              <Accordion
                asDefinitionList={false}
                className={css(styles.accordion)}
                noBoxShadow={true}
                role={"tree"}
                aria-labelledby={id}
              >
                {children}
              </Accordion>
            </CardBody>
            {footer}
          </Card>
        </div>
      </DocumentFocusProvider>
    );
  },
);
