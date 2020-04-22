import React, { FunctionComponent } from "react";
import { HTMLObject, useCanvas } from "../Canvas";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  wrapper: {
    width: "100%",
    height: "100%",
    display: "flex",
    flexFlow: "row nowrap",
    overflow: "auto",
  },
});

export const Columns: FunctionComponent = ({ children }) => {
  const {
    dimensions: { width, height },
  } = useCanvas();
  return (
    <HTMLObject width={width} height={height} x={0} y={0}>
      <div className={css(styles.wrapper)}>{children}</div>
    </HTMLObject>
  );
};
