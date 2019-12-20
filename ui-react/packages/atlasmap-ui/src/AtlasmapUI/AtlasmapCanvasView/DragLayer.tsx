import { Label } from '@patternfly/react-core';
import React, { FunctionComponent, useEffect } from 'react';
import { useDragLayer } from 'react-dnd';
import { IFieldElementDragSource } from './DocumentField';
import { CanvasObject, useCanvas } from '../../Canvas';
import { useDimensions } from '../../common';
import { css, StyleSheet } from '@patternfly/react-styles';

const styles = StyleSheet.create({
  canvasObject: { pointerEvents: 'none' },
  canvasInner: {
    display: 'inline-block',
    margin: 'auto',
  },
});

export const DragLayer: FunctionComponent = () => {
  const [ref, dimensions, measure] = useDimensions();
  const { offsetLeft, offsetTop } = useCanvas();
  const { isDragging, item, currentOffset, initialOffset } = useDragLayer(
    monitor => ({
      item: monitor.getItem() as IFieldElementDragSource,
      itemType: monitor.getItemType(),
      initialOffset: monitor.getInitialSourceClientOffset(),
      currentOffset: monitor.getClientOffset(),
      isDragging: monitor.isDragging(),
    })
  );

  useEffect(measure, [isDragging]);

  return !isDragging || !currentOffset || !initialOffset ? null : (
    <CanvasObject
      id={'draglayer'}
      width={dimensions.width}
      height={dimensions.height}
      x={currentOffset.x - offsetLeft - dimensions.width / 2}
      y={currentOffset.y - offsetTop - dimensions.height}
      className={css(styles.canvasObject)}
    >
      <div ref={ref} className={css(styles.canvasInner)}>
        <Label>{item.name}</Label>
      </div>
    </CanvasObject>
  );
};
