import React, {
  forwardRef,
  PropsWithChildren,
  ReactElement,
  useRef,
} from "react";

import { FolderCloseIcon, FolderOpenIcon } from "@patternfly/react-icons";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
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
  name: string;
  icon?: ReactElement;
  type?: string;
  showType?: boolean;
  expanded?: boolean;
}

export const DocumentGroup = forwardRef<
  HTMLSpanElement,
  PropsWithChildren<IDocumentGroupProps>
>(({ name, type, icon, showType = false, expanded }, ref) => {
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
    <span className={css(styles.buttonContent)} ref={handleRef}>
      <span className={css(styles.buttonIcon)}>
        {icon || (expanded ? <FolderOpenIcon /> : <FolderCloseIcon />)}
      </span>
      {name}
      {showType && ` (${type})`}
    </span>
  );
});
