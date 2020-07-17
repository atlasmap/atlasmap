import {
  Card,
  CardActions,
  CardBody,
  CardHeader,
  Title,
  Button,
  CardTitle,
} from "@patternfly/react-core";
import { css } from "@patternfly/react-styles";
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
import { useToggle } from "./useToggle";
import { AngleDownIcon, AngleRightIcon } from "@patternfly/react-icons";

import { TruncatedString } from "./TruncatedString";
import styles from "./Document.css";

export interface IDocumentProps
  extends Omit<HTMLAttributes<HTMLDivElement>, "title"> {
  title?: string;
  actions?: (ReactElement | null | undefined)[];
  footer?: ReactNode;
  selected?: boolean;
  selectable?: boolean;
  dropTarget?: boolean;
  dropAccepted?: boolean;
  stacked?: boolean;
  scrollIntoView?: boolean;
  noPadding?: boolean;
  noShadows?: boolean;
  startExpanded?: boolean;
  onSelect?: () => void;
  onDeselect?: () => void;
}

export const Document = forwardRef<
  HTMLDivElement,
  PropsWithChildren<IDocumentProps>
>(function Document(
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
    noShadows = false,
    startExpanded = true,
    onSelect,
    onDeselect,
    children,
    ...props
  },
  ref,
) {
  const { state: isExpanded, toggle: toggleExpanded } = useToggle(
    startExpanded,
  );
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
          noShadows && styles.noShadows,
          dropAccepted && !dropTarget && styles.dropAccepted,
          dropTarget && styles.dropTarget,
        )}
        isSelected={makeCardSelected}
        isSelectable={makeCardSelected || selectable}
        aria-label={title}
      >
        {(title || actions) && (
          <CardHeader data-codemods="true" className={css(styles.head)}>
            <CardActions className={css(styles.actions)}>
              {actions?.filter((a) => a)}
            </CardActions>
            {title && (
              <CardTitle className={css(styles.header)}>
                <Button
                  variant={"plain"}
                  onClick={toggleExpanded}
                  aria-label={"Expand/collapse this card"}
                  data-testid={`expand-collapse-${title}-button`}
                  className={css(styles.headerButton)}
                >
                  <Title size="lg" headingLevel={"h2"} aria-label={title}>
                    <TruncatedString title={title}>
                      {isExpanded ? <AngleDownIcon /> : <AngleRightIcon />}{" "}
                      {title}
                    </TruncatedString>
                  </Title>
                </Button>
              </CardTitle>
            )}
          </CardHeader>
        )}
        <CardBody
          className={css(
            noPadding ? styles.bodyNoPadding : styles.bodyWithPadding,
            !isExpanded && styles.hidden,
          )}
        >
          {children}
        </CardBody>
        {footer}
      </Card>
    </div>
  );
});
