import React, { Children, FunctionComponent } from "react";

import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  toolbar: {
    display: "flex",
    flexFlow: "row no-wrap",
  },
  toolbarItem: {
    minWidth: "1.5rem",
    flex: "0 1 auto",
  },
});

export const Actions: FunctionComponent = ({ children }) => (
  <div className={css(styles.toolbar)}>
    {Children.map(children, (c, idx) => (
      <div className={css(styles.toolbarItem)} key={idx}>
        {c}
      </div>
    ))}
  </div>
);
