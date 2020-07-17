import React, { FunctionComponent } from "react";
import { HTMLObject, useCanvas } from "../Canvas";
import { css } from "@patternfly/react-styles";

import styles from "./Columns.css";

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
