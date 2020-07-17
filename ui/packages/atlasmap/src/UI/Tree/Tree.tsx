import React, { FunctionComponent, useRef, useLayoutEffect } from "react";

import { Accordion } from "@patternfly/react-core";
import { css } from "@patternfly/react-styles";

import { TreeFocusProvider } from "./TreeFocusProvider";

import styles from "./Tree.css";

export const Tree: FunctionComponent = ({ children }) => {
  const ref = useRef<HTMLDivElement | null>(null);

  useLayoutEffect(() => {
    if (ref.current) {
      const itemInTabSequence = ref.current.querySelector(
        `[role=treeitem][tabindex="0"]`,
      );
      if (!itemInTabSequence) {
        const firstTreeItem = ref.current.querySelector("[role=treeitem]");
        if (firstTreeItem) {
          firstTreeItem.setAttribute("tabindex", "0");
        }
      }
    }
  });
  return (
    <TreeFocusProvider>
      <div ref={ref}>
        <Accordion
          asDefinitionList={false}
          className={css(styles.accordion)}
          role={"tree"}
        >
          {children}
        </Accordion>
      </div>
    </TreeFocusProvider>
  );
};
