import React, { PropsWithChildren, forwardRef } from "react";

import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  body: {
    background: "var(--pf-global--BackgroundColor--150)",
    border: "1px solid var(--pf-global--BorderColor--100)",
    flex: "1",
    overflowX: "visible",
    overflowY: "scroll",
    padding: "0 var(--pf-global--spacer--md)",
    "&::-webkit-scrollbar": {
      width: "10px",
      backgroundColor: "transparent",
    },
    "&::-webkit-scrollbar-track": {
      backgroundColor: "transparent",
    },
    "&::-webkit-scrollbar-thumb": {
      borderRadius: "10px",
      boxShadow: "inset 0 0 6px rgba(0,0,0,.1)",
      backgroundColor: "rgba(200, 200, 200, 0.8)",
    },
  },
});

export const ColumnBody = forwardRef<HTMLDivElement, PropsWithChildren<{}>>(
  function ColumnBody({ children }, ref) {
    return (
      <div className={css(styles.body)} ref={ref}>
        {children}
      </div>
    );
  },
);
