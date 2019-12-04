import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, useCallback, useEffect, useRef } from 'react';
import { useDrag } from 'react-dnd';
import { getEmptyImage } from 'react-dnd-html5-backend'
import { useBoundingCanvasRect, useMappingNode } from '../../../canvas';
import { ElementId, DocumentType, IFieldsNode } from '../../../models';

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
  documentType: DocumentType;
  lineConnectionSide: 'left' | 'right';
  getParentRef: () => HTMLElement | null;
  getBoxRef: () => HTMLElement | null;
  rightAlign?: boolean;
}

export interface IFieldElementDragSource {
  id: ElementId;
  type: DocumentType;
  name: string;
}

export const FieldElement: FunctionComponent<IFieldElementProps> = ({
  node,
  documentType,
  lineConnectionSide,
  getParentRef,
  getBoxRef,
  rightAlign = false,
}) => {
  const ref = useRef<HTMLDivElement | null>(null);

  const getBoundingCanvasRect = useBoundingCanvasRect();
  const { setLineNode, unsetLineNode } = useMappingNode();
  const getCoords = useCallback(() => {
    const parentRef = getParentRef();
    const boxRef = getBoxRef();
    if (ref.current && parentRef && boxRef) {
      let parentRect = getBoundingCanvasRect(parentRef);
      let boxRect = getBoundingCanvasRect(boxRef);
      let dimensions = getBoundingCanvasRect(ref.current);
      dimensions = dimensions.height > 0 ? dimensions : parentRect;
      return {
        x: lineConnectionSide === 'right' ? boxRect.right : boxRect.left,
        y: Math.min(
          Math.max(dimensions.top + dimensions.height / 2, boxRect.top),
          boxRect.height + boxRect.top
        ),
      };
    }
    // if (node.id === 'io.paul.Bicycle-/serialId')
    // console.log(node.id, ref.current, parentRef, boxRef)
    return null;
  }, [getBoundingCanvasRect, getBoxRef, getParentRef, documentType]);

  const [{ opacity }, dragRef, preview] = useDrag<
    IFieldElementDragSource,
    undefined,
    { opacity: number }
    >({
    item: { id: node.id, type: documentType, name: node.name },
    collect: monitor => ({
      opacity: monitor.isDragging() ? 0.4 : 1,
    }),
    begin: () => {
      setLineNode('dragsource', getCoords);
    }
  });

  useEffect(() => {
    setLineNode(node.id, getCoords);
    return () => {
      unsetLineNode(node.id);
    }
  }, [node, setLineNode, unsetLineNode, getCoords]);

  const handleRef = (el: HTMLDivElement) => {
    dragRef(el);
    ref.current = el;
  };

  useEffect(() => {
    preview(getEmptyImage(), { captureDraggingState: true })
  }, [preview]);

  return (
    <div
      ref={handleRef}
      className={css(styles.element, rightAlign && styles.rightAlign)}
      style={{ opacity }}
    >
      {node.name}
    </div>
  );
};
