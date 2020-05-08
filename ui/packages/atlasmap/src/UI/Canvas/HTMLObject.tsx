import React, { forwardRef, SVGProps } from "react";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  foreignObject: { overflow: "visible" },
});

export interface IHTMLObjectProps extends SVGProps<SVGForeignObjectElement> {
  width: number;
  height: number;
  x: number;
  y: number;
}

export const HTMLObject = forwardRef<SVGForeignObjectElement, IHTMLObjectProps>(
  ({ children, width, height, x, y, className, ...props }, ref) => {
    return (
      <foreignObject
        width={width}
        height={height}
        x={x}
        y={y}
        className={css(styles.foreignObject, className)}
        {...props}
        ref={ref}
      >
        {children}
      </foreignObject>
    );
  },
);
