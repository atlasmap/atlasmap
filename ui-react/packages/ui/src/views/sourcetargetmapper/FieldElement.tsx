import { css, StyleSheet } from '@patternfly/react-styles';
import { useMappingNode } from '@src/canvas/CanvasLinks';
import { useBoundingCanvasRect } from '@src/canvas/useBoundingCanvasRect';
import { IFieldsNode, NodeType } from '@src/models';
import React, { FunctionComponent, useCallback, useRef } from 'react';

const styles = StyleSheet.create({
  rightAlign: {
    padding: '0.3rem',
    borderBottom: '1px solid #eee',
    transform: 'scaleX(-1)'
  }
});

export interface IFieldElementProps {
  node: IFieldsNode;
  type: NodeType;
  parentRef: HTMLElement | null;
  boxRef: HTMLElement | null;
  rightAlign?: boolean;
}

export const FieldElement: FunctionComponent<IFieldElementProps> = ({
  node,
  type,
  parentRef,
  boxRef,
  rightAlign = false
}) => {
  const ref = useRef<HTMLDivElement | null>(null);
  const getBoundingCanvasRect = useBoundingCanvasRect();
  const setLineNode = useMappingNode();
  const getCoords = useCallback(() => {
    if (ref.current && parentRef && boxRef) {
      let parentRect = getBoundingCanvasRect(parentRef);
      let boxRect = getBoundingCanvasRect(boxRef);
      let dimensions = getBoundingCanvasRect(ref.current);
      dimensions = dimensions.height > 0 ? dimensions : parentRect;
      return {
        x: type === 'source' ? boxRect.right : boxRect.left,
        y: Math.min(
          Math.max(dimensions.top + dimensions.height / 2, boxRect.top),
          boxRect.height + boxRect.top
        ),
      };
    } else {
      return { x: 0, y: 0 };
    }
  }, [ref, parentRef, type, boxRef, getBoundingCanvasRect]);
  setLineNode(node.id, getCoords);
  return (
    <div
      ref={ref}
      className={css(rightAlign && styles.rightAlign)}
    >
      {node.element}
    </div>
  );
};
