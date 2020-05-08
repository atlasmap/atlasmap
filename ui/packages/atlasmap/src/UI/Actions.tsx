import React, { Children, FunctionComponent } from "react";

import { Toolbar, ToolbarItem } from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  toolbarItem: {
    minWidth: "1.5rem",
  },
});

export const Actions: FunctionComponent = ({ children }) => (
  <Toolbar>
    {Children.map(children, (c, idx) => (
      <ToolbarItem className={css(styles.toolbarItem)} key={idx}>
        {c}
      </ToolbarItem>
    ))}
  </Toolbar>
);
