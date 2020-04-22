import React, { FunctionComponent } from "react";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  fieldName: {
    display: "inline-block",
    whiteSpace: "nowrap",
    overflow: "hidden",
    textOverflow: "ellipsis",
    width: "100%",
  },
});

export const FieldName: FunctionComponent = ({ children }) => (
  <div
    className={css(styles.fieldName)}
    title={typeof children === "string" ? children : undefined}
  >
    {children}
  </div>
);
