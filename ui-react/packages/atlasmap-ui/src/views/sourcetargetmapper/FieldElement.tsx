import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, useCallback, useRef } from 'react';
import { useDrag } from 'react-dnd';
import { useBoundingCanvasRect, useMappingNode } from '../../canvas';
import { ElementId, ElementType, IFieldsNode } from '../../models';

const styles = StyleSheet.create({
  element: {
    padding:
      'var(--pf-c-accordion__toggle--PaddingTop) var(--pf-c-accordion__toggle--PaddingRight) var(--pf-c-accordion__toggle--PaddingBottom) calc(var(--pf-c-accordion__toggle--PaddingLeft))',
    cursor: 'pointer',
  },
  rightAlign: {
    transform: 'scaleX(-1)',
    padding:
      'var(--pf-c-accordion__toggle--PaddingTop) var(--pf-c-accordion__toggle--PaddingLeft) var(--pf-c-accordion__toggle--PaddingBottom) var(--pf-c-accordion__toggle--PaddingRight)',
  },
});

export interface IFieldElementProps {
  node: IFieldsNode;
  type: ElementType;
  parentRef: HTMLElement | null;
  boxRef: HTMLElement | null;
  rightAlign?: boolean;
}

export interface IFieldElementDragSource {
  id: ElementId;
  type: ElementType;
}

export const FieldElement: FunctionComponent<IFieldElementProps> = ({
  node,
  type,
  parentRef,
  boxRef,
  rightAlign = false,
}) => {
  const ref = useRef<HTMLDivElement | null>(null);

  const getBoundingCanvasRect = useBoundingCanvasRect();
  const { setLineNode } = useMappingNode();
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


  const [{ opacity }, dragRef] = useDrag<
    IFieldElementDragSource,
    undefined,
    { opacity: number }
    >({
    item: { id: node.id, type },
    collect: monitor => ({
      opacity: monitor.isDragging() ? 0.4 : 1,
    }),
    begin: () => {
      setLineNode('dragsource', getCoords);
    }
  });
  setLineNode(node.id, getCoords);

  const handleRef = (el: HTMLDivElement) => {
    dragRef(el);
    ref.current = el;
  };

  return (
    <div
      ref={handleRef}
      className={css(styles.element, rightAlign && styles.rightAlign)}
      style={{ opacity }}
    >
      {node.element}
    </div>
  );
};
