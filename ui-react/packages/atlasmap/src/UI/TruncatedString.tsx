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

export interface ITruncatedStringProps {
  title?: string;
}

export const TruncatedString: FunctionComponent<ITruncatedStringProps> = ({
  title,
  children,
}) => (
  <span
    className={css(styles.fieldName)}
    title={title || (typeof children === "string" ? children : undefined)}
  >
    {children}
  </span>
);
