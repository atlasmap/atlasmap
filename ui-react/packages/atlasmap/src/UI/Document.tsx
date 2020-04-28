import {
  Card,
  CardActions,
  CardBody,
  CardHead,
  CardHeader,
  Title,
} from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";
import React, {
  forwardRef,
  HTMLAttributes,
  KeyboardEvent,
  MouseEvent,
  PropsWithChildren,
  ReactElement,
  ReactNode,
  useCallback,
} from "react";

const styles = StyleSheet.create({
  card: {},
  stacked: {
    margin: "1rem 0",
  },
  head: {
    paddingRight: "0 !important",
  },
  noPadding: {
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
  noPadding?: boolean;
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
      noPadding = false,
      onSelect,
      onDeselect,
      children,
      ...props
    },
    ref,
  ) => {
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

    return (
      <div
        className={css(stacked && styles.stacked)}
        onClick={handleClick}
        onKeyDown={handleKey}
        ref={ref}
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
          <CardBody className={css(noPadding && styles.noPadding)}>
            {children}
          </CardBody>
          {footer}
        </Card>
      </div>
    );
  },
);
