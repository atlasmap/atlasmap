import React, { FunctionComponent } from "react";

import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  wrapper: {
    minHeight: "100%",
    flex: 1,
    padding: "1rem",
    boxSizing: "border-box",
    maxWidth: 1000,
    alignSelf: "flex-start",
  },
});

export const MainContent: FunctionComponent = ({ children }) => (
  <div className={css(styles.wrapper)} role={"main"}>
    {children}
  </div>
);
