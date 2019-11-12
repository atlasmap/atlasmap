import { useCanvas } from '@src';
import { useMappingNode } from '@src/canvas/CanvasLinks';
import { MappingNode, MappingNodeType } from '@src/models';
import React, { FunctionComponent, useCallback, useRef } from 'react';

export interface IFieldElementProps {
  node: MappingNode;
  type: MappingNodeType;
  parentRef: HTMLElement | null;
  boxRef: HTMLElement | null;
}

export const FieldElement: FunctionComponent<IFieldElementProps> = ({
  node,
  type,
  parentRef,
  boxRef,
}) => {
  const { zoom, offsetLeft, offsetTop } = useCanvas();
  const ref = useRef<HTMLDivElement | null>(null);
  const setLineNode = useMappingNode();
  const getCoords = useCallback(() => {
    if (ref.current && parentRef && boxRef) {
      let parentRect = parentRef.getBoundingClientRect();
      let boxRect = boxRef.getBoundingClientRect();
      let dimensions = ref.current.getBoundingClientRect();
      dimensions = dimensions.height > 0 ? dimensions : parentRect;
      return {
        x: (type === 'source' ? boxRect.right : boxRect.left) - offsetLeft,
        y: Math.min(
          Math.max(
            dimensions.top - offsetTop + dimensions.height / 2,
            boxRect.top - offsetTop
          ),
          boxRect.height + boxRect.top - offsetTop
        ),
      };
    } else {
      return { x: 0, y: 0 };
    }
  }, [
    parentRef,
    type,
    boxRef,
    offsetLeft,
    offsetTop,
  ]);
  setLineNode(node.id, getCoords);
  return (
    <div
      ref={ref}
      style={{
        padding: `calc(0.3rem * ${zoom}) 0`,
        borderTop: '1px solid #eee',
        borderBottom: '1px solid #eee',
        marginTop: '-1px',
      }}
    >
      {node.element}
    </div>
  );
};
