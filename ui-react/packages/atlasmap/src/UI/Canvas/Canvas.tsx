import { css, StyleSheet } from "@patternfly/react-styles";
import React, {
  CSSProperties,
  FunctionComponent,
  SVGAttributes,
  useMemo,
  useEffect,
} from "react";
import { useCanvas } from "./CanvasContext";
import { CanvasTransforms } from "./CanvasTransforms";
import tile from "./canvasBg";
import { useDimensions } from "../useDimensions";

const styles = StyleSheet.create({
  canvasWrapper: {
    flex: "1",
    height: "100%",
    overflow: "hidden",
  },
  svg: {
    display: "block",
    "& *": {
      transition: "stroke 0.35s, stroke-width 0.15s",
    },
  },
  panning: {
    backgroundImage: `url(${tile})`,
    backgroundRepeat: "repeat",
  },
});

export interface ICanvasProps extends SVGAttributes<SVGSVGElement> {
  isFilled?: boolean;
}

export const Canvas: FunctionComponent<ICanvasProps> = ({
  children,
  className,
  style,
  isFilled = true,
  ...props
}) => {
  const {
    initialWidth,
    initialHeight,
    setDimension,
    addRedrawListener,
    removeRedrawListener,
  } = useCanvas();
  const [dimensionsRef, dimensions, measure] = useDimensions();
  useEffect(() => {
    setDimension({
      width: dimensions.width,
      height: dimensions.height,
      offsetLeft: dimensions.left,
      offsetTop: dimensions.top,
    });
  }, [dimensions, setDimension]);

  useEffect(() => {
    addRedrawListener(measure);
    return () => removeRedrawListener(measure);
  }, [addRedrawListener, measure, removeRedrawListener]);

  const { allowPanning, isPanning, zoom, bindCanvas } = useCanvas();
  const svgStyle = useMemo(() => {
    return {
      cursor: allowPanning ? (isPanning ? "grabbing" : "grab") : undefined,
      userSelect: allowPanning && isPanning ? "none" : "auto",
      backgroundSize: `${30 * zoom}px ${30 * zoom}px`,
      width: initialWidth || "100%",
      height: initialHeight || "100%",
      ...style,
    } as CSSProperties;
  }, [allowPanning, initialHeight, initialWidth, isPanning, style, zoom]);
  return (
    <div ref={dimensionsRef} className={css(isFilled && styles.canvasWrapper)}>
      <svg
        {...props}
        className={css(styles.svg, allowPanning && styles.panning, className)}
        style={svgStyle}
        {...bindCanvas()}
      >
        <CanvasTransforms>{children}</CanvasTransforms>
      </svg>
    </div>
  );
};
