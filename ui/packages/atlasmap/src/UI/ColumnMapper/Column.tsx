import React, { FunctionComponent, useMemo } from "react";
import { css } from "@patternfly/react-styles";

import styles from "./Column.css";

export interface IColumnProps {
  totalColumns?: number;
  visible?: boolean;
}

export const Column: FunctionComponent<IColumnProps> = ({
  totalColumns = 1,
  visible = true,
  children,
  ...props
}) => {
  const style = useMemo(() => ({ flex: `0 0 ${100 / totalColumns}%` }), [
    totalColumns,
  ]);
  return (
    <div
      className={css(styles.column, !visible && styles.hidden)}
      style={style}
      {...props}
    >
      {children}
    </div>
  );
};
