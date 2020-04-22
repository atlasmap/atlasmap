import React, { FunctionComponent } from "react";
import { Canvas, ICanvasProps } from "../Canvas";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  canvas: {
    backgroundColor: "var(--pf-global--BackgroundColor--light-300)",
  },
});

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
