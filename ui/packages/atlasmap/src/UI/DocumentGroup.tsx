import React, {
  forwardRef,
  PropsWithChildren,
  ReactElement,
  useRef,
} from "react";

import { FolderCloseIcon, FolderOpenIcon } from "@patternfly/react-icons";
import { css } from "@patternfly/react-styles";
import { Tooltip } from "@patternfly/react-core";

import styles from "./DocumentGroup.css";

export interface IDocumentGroupProps {
  name: string;
  icon?: ReactElement;
  iconTooltip?: string;
  type?: string;
  showType?: boolean;
  expanded?: boolean;
}

export const DocumentGroup = forwardRef<
  HTMLSpanElement,
  PropsWithChildren<IDocumentGroupProps>
>(function DocumentGroup(
  { name, type, icon, iconTooltip, showType = false, expanded },
  ref,
) {
  const spanRef = useRef<HTMLSpanElement | null>(null);

  const handleRef = (el: HTMLSpanElement | null) => {
    spanRef.current = el;
    if (ref) {
      if (typeof ref === "function") {
        ref(el);
      } else {
        // @ts-ignore
        ref.current = el;
      }
    }
  };

  return (
    <span
      className={css(styles.buttonContent)}
      ref={handleRef}
      data-testid={`field-group-${name}-expanded-${expanded}-field`}
    >
      <span className={css(styles.buttonIcon)}>
        {expanded ? <FolderOpenIcon /> : <FolderCloseIcon />}
      </span>
      {name}
      {showType && ` (${type})`}
      {icon && (
        <Tooltip enableFlip content={iconTooltip}>
          <span className={css(styles.icon)}>{icon}</span>
        </Tooltip>
      )}
    </span>
  );
});
