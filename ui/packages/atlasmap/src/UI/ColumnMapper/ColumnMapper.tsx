import React, { FunctionComponent } from "react";
import { Canvas, ICanvasProps } from "../Canvas";
import { css } from "@patternfly/react-styles";

import styles from "./ColumnMapper.css";

export interface IColumnMapperProps extends ICanvasProps {}

export const ColumnMapper: FunctionComponent<IColumnMapperProps> = ({
  className,
  children,
  ...props
}) => {
  return (
    <Canvas {...props} className={css(styles.canvas, className)}>
      {children}
    </Canvas>
  );
};
