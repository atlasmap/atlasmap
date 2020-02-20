import React, { FunctionComponent, useEffect } from "react";

import { Label } from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";

import { useDimensions } from "../useDimensions";
import { DraggedField } from "./DraggedField";

const styles = StyleSheet.create({
  canvasObject: { pointerEvents: "none" },
  canvasInner: {
    display: "inline-block",
    margin: "auto",
  },
});

export const FieldDragLayer: FunctionComponent = () => {
  const [ref, dimensions, measure] = useDimensions();
  useEffect(measure);

  return (
    <DraggedField>
      {({ isDragging, currentOffset, draggedField }) =>
        isDragging && currentOffset && draggedField ? (
          <div
            style={{
              position: "absolute",
              zIndex: 1000,
              width: dimensions.width,
              height: dimensions.height,
              left: currentOffset.x - dimensions.width / 2,
              top: currentOffset.y - dimensions.height,
            }}
            className={css(styles.canvasObject)}
          >
            <div ref={ref} className={css(styles.canvasInner)}>
              <Label>{draggedField.name}</Label>
            </div>
          </div>
        ) : null
      }
    </DraggedField>
  );
};
