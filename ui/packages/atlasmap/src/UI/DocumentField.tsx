import React, {
  forwardRef,
  PropsWithChildren,
  ReactChild,
  ReactElement,
} from "react";

import { css } from "@patternfly/react-styles";

import { TruncatedString } from "./TruncatedString";
import { useToggle } from "./useToggle";
import styles from "./DocumentField.css";

export interface IDocumentFieldProps {
  name: ReactChild;
  type: string;
  icon?: ReactElement;
  statusIcons?: (ReactElement | null)[];
  actions?: (ReactElement | null)[];
  showType?: boolean;
  isDragging?: boolean;
  isFocused?: boolean;
  isSelected?: boolean;
  isDisabled?: boolean;
}

export const DocumentField = forwardRef<
  HTMLDivElement,
  PropsWithChildren<IDocumentFieldProps>
>(function DocumentField(
  {
    name,
    type,
    icon,
    statusIcons,
    actions,
    showType = false,
    isDragging = false,
    isFocused = false,
    isSelected = false,
    isDisabled = false,
    children,
  },
  ref,
) {
  const {
    state: isHovering,
    toggleOff: hideActions,
    toggleOn: showActions,
  } = useToggle(false);
  return (
    <div
      ref={ref}
      className={css(
        styles.element,
        isDragging && styles.isDragging,
        isSelected && styles.isSelected,
        isDisabled && styles.isDisabled,
      )}
      onMouseEnter={!isDisabled ? showActions : undefined}
      onMouseLeave={!isDisabled ? hideActions : undefined}
    >
      <div className={css(styles.row)}>
        {icon && <div className={css(styles.nameIcon)}>{icon}</div>}
        <div
          className={css(styles.nameWrapper)}
          data-testid={`document-${name}-field`}
        >
          <TruncatedString>{name}</TruncatedString>
          <span>{showType && ` (${type})`}</span>
          <span className={styles.statusIcons}>
            {statusIcons && statusIcons?.filter((a) => a)}
          </span>
        </div>
        {(isHovering || isFocused) && actions && !isDisabled && (
          <div className={css(styles.actions)}>{actions?.filter((a) => a)}</div>
        )}
      </div>
      {children}
    </div>
  );
});
