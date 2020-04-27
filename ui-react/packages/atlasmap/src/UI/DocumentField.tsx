import React, {
  forwardRef,
  PropsWithChildren,
  ReactChild,
  ReactElement,
} from "react";

import { css, StyleSheet } from "@patternfly/react-styles";

import { FieldName } from "./FieldName";
import { useToggle } from "./useToggle";

const styles = StyleSheet.create({
  element: {
    color: "var(--pf-global--Color--100)",
    border: "3px solid transparent",
    position: "relative",
  },
  row: {
    padding: "0 1rem",
    display: "flex",
  },
  nameWrapper: {
    display: "flex !important",
    alignItems: "center",
  },
  nameIcon: {
    width: "48px",
  },
  statusIcons: {
    display: "flex",
    alignItems: "center",
    padding: "0 1rem",
    "& > *": {
      margin: "0 0.5rem",
    },
  },
  isDragging: {
    color: "var(--pf-global--active-color--400)",
  },
  isSelected: {
    borderColor: "var(--pf-global--primary-color--100)",
  },
  actions: {
    position: "absolute",
    top: 0,
    right: 0,
    height: "100%",
    textAlign: "right",
    transition: "all 0.2s",
    background:
      "linear-gradient(to left, var(--pf-global--BackgroundColor--100) calc(100% - 2rem), rgba(255, 255, 255, 0.5))",
    paddingLeft: "2rem",
  },
});

export interface IDocumentFieldProps {
  name: ReactChild;
  type: string;
  icon?: ReactElement;
  statusIcons?: (ReactElement | null)[];
  actions?: (ReactElement | null)[];
  showType?: boolean;
  isDragging?: boolean;
  isFocused?: boolean;
}

export const DocumentField = forwardRef<
  HTMLDivElement,
  PropsWithChildren<IDocumentFieldProps>
>(
  (
    {
      name,
      type,
      icon,
      statusIcons,
      actions,
      showType = false,
      isDragging = false,
      isFocused = false,
      children,
    },
    ref,
  ) => {
    const {
      state: isHovering,
      toggleOff: hideActions,
      toggleOn: showActions,
    } = useToggle(false);
    return (
      <div
        ref={ref}
        className={css(styles.element, isDragging && styles.isDragging)}
        onMouseEnter={showActions}
        onMouseLeave={hideActions}
      >
        <div className={css(styles.row)}>
          {icon && <div className={css(styles.nameIcon)}>{icon}</div>}
          <div className={css(styles.nameWrapper)}>
            <FieldName>{name}</FieldName>
            <span>{showType && ` (${type})`}</span>
            <span className={styles.statusIcons}>
              {statusIcons && statusIcons?.filter((a) => a)}
            </span>
          </div>
        </div>
        {(isHovering || isFocused) && actions && (
          <div className={css(styles.actions)}>{actions?.filter((a) => a)}</div>
        )}
        {children}
      </div>
    );
  },
);
