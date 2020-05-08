import React, { PropsWithChildren, forwardRef } from "react";

import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  body: {
    flex: "1",
    overflowX: "visible",
    overflowY: "scroll",
    padding: "0 2rem",
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
      backgroundColor: "#dedede",
    },
  },
});

export const ColumnBody = forwardRef<HTMLDivElement, PropsWithChildren<{}>>(
  ({ children }, ref) => {
    return (
      <div className={css(styles.body)} ref={ref}>
        {children}
      </div>
    );
  },
);
