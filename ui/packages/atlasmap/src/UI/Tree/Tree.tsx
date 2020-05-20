import React, { FunctionComponent, useRef, useLayoutEffect } from "react";

import { Accordion } from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";

import { TreeFocusProvider } from "./TreeFocusProvider";

const styles = StyleSheet.create({
  accordion: {
    padding: "0 1rem 0 0 !important", // TODO: this padding should be 0 and the right spacing should be given by DocumentGroup
  },
});

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
          noBoxShadow={true}
          role={"tree"}
        >
          {children}
        </Accordion>
      </div>
    </TreeFocusProvider>
  );
};
