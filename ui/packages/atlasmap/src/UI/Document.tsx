import {
  Card,
  CardActions,
  CardBody,
  CardHead,
  CardHeader,
  Title,
  Button,
  TextInput,
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
  useRef,
} from "react";
import { useToggle } from "./useToggle";
import { AngleDownIcon, AngleRightIcon } from "@patternfly/react-icons";
import { TruncatedString } from "./TruncatedString";

const styles = StyleSheet.create({
  card: {
    padding: "0 !important",
  },
  stacked: {
    margin: "1rem 0",
  },
  head: {
    padding: "0.5rem 0 !important",
  },
  header: {
    width: "100%",
    overflow: "hidden",
  },
  actions: {
    alignSelf: "center",
  },
  headerButton: {
    maxWidth: "100%",
  },
  bodyNoPadding: {
    padding: "0 !important",
  },
  bodyWithPadding: {
    padding: "0 1rem 1rem !important",
  },
  noCardPadding: {},
  hidden: {
    display: "none",
  },
  noShadows: {
    boxShadow: "none !important",
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
  isEditingTitle?: boolean;
  onTitleChange?: (title: string) => void;
  onStopEditingTitle?: (cancel?: boolean) => void;
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
    isEditingTitle,
    onTitleChange,
    onStopEditingTitle,
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
          if (isEditingTitle && event.key === "Enter" && onStopEditingTitle) {
            onStopEditingTitle();
          } else if (onSelect) {
            onSelect();
          }
          break;
        case "Escape":
          if (isEditingTitle && onStopEditingTitle) {
            onStopEditingTitle(true);
          } else if (onDeselect) {
            onDeselect();
          }
          break;
      }
    },
    [isEditingTitle, onDeselect, onSelect, onStopEditingTitle],
  );
  const makeCardSelected = selected || dropTarget || dropAccepted;
  // TODO: Figure out how to do select all on focus. nameRef code doesn't work as suggested by PatternFly docs
  const nameRef = useRef<HTMLInputElement>(null);

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
          <CardHead className={css(styles.head)}>
            <CardActions className={css(styles.actions)}>
              {actions?.filter((a) => a)}
            </CardActions>
            {title && (
              <CardHeader className={css(styles.header)}>
                {isEditingTitle ? (
                  <TextInput
                    defaultValue={title}
                    type="text"
                    aria-label="Edit title"
                    onChange={onTitleChange}
                    autoFocus
                    onBlur={() => onStopEditingTitle && onStopEditingTitle()}
                    ref={nameRef}
                    onFocus={() =>
                      nameRef && nameRef.current && nameRef.current.select()
                    }
                  />
                ) : (
                  <Button
                    variant={"plain"}
                    onClick={toggleExpanded}
                    aria-label={"Expand/collapse this card"}
                    data-testid={`expand-collapse-${title}-button`}
                    className={css(styles.headerButton)}
                  >
                    <Title size={"lg"} headingLevel={"h2"} aria-label={title}>
                      <TruncatedString title={title}>
                        {isExpanded ? <AngleDownIcon /> : <AngleRightIcon />}{" "}
                        {title}
                      </TruncatedString>
                    </Title>
                  </Button>
                )}
              </CardHeader>
            )}
          </CardHead>
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
