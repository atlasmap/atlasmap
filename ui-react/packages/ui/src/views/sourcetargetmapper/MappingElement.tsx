import { css, StyleSheet } from '@patternfly/react-styles';
import { useMappingNode } from '@src/canvas/CanvasLinks';
import { useBoundingCanvasRect } from '@src/canvas/useBoundingCanvasRect';
import { IFieldsNode, ElementType } from '@src/models';
import React, { FunctionComponent, useCallback, useRef } from 'react';

const styles = StyleSheet.create({
  element: {
    width: '80px',
    background: '#fff',
    borderRadius: '5px',
    padding: '1rem',
    margin: '1rem',
    border: '1px solid #ddd',
    textAlign: 'center'
  },
});

export interface IMappingElementProps {
  node: IFieldsNode;
  type: ElementType;
  boxRef: HTMLElement | null;
}

export const MappingElement: FunctionComponent<IMappingElementProps> = ({
  node,
  type,
  boxRef,
}) => {
  const ref = useRef<HTMLDivElement | null>(null);
  const getBoundingCanvasRect = useBoundingCanvasRect();
  const setLineNode = useMappingNode();
  const getCoords = useCallback(() => {
    if (ref.current && boxRef) {
      let boxRect = getBoundingCanvasRect(boxRef);
      let dimensions = getBoundingCanvasRect(ref.current);
      return {
        left: dimensions.left,
        right: dimensions.right,
        y: Math.min(
          Math.max(dimensions.top + dimensions.height / 2, boxRect.top),
          boxRect.height + boxRect.top
        ),
      };
    } else {
      return { left: 0, right: 0, y: 0 };
    }
  }, [ref, type, boxRef, getBoundingCanvasRect]);
  setLineNode(`to-${node.id}`, () => {
    const { left, y } = getCoords();
    return {
      x: left,
      y,
    }
  });
  setLineNode(`from-${node.id}`, () => {
    const { right, y } = getCoords();
    return {
      x: right,
      y,
    }
  });
  return (
    <div
      ref={ref}
      className={css(styles.element)}
    >
      {node.element}
    </div>
  );
};
