import { css, StyleSheet } from '@patternfly/react-styles';
import React, {
  ReactElement,
  useCallback,
  useEffect,
  useRef,
} from 'react';
import { useBoundingCanvasRect, useMappingNode } from '../../../canvas';
import { Coords, IFieldsGroup, IFieldsNode } from '../models';

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
  lineConnectionSide: 'left' | 'right';
  getParentRef: () => HTMLElement | null;
  getBoxRef: () => HTMLElement | null;
  rightAlign?: boolean;
  renderNode: (
    node: IFieldsGroup | IFieldsNode,
    getCoords: () => Coords | null,
  ) => ReactElement;
}

export function FieldElement ({
  node,
  lineConnectionSide,
  getParentRef,
  getBoxRef,
  rightAlign = false,
  renderNode,
}: IFieldElementProps) {
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
    return null;
  }, [getParentRef, getBoxRef, getBoundingCanvasRect, lineConnectionSide]);


  useEffect(() => {
    setLineNode(node.id, getCoords);
    return () => {
      unsetLineNode(node.id);
    };
  }, [node, setLineNode, unsetLineNode, getCoords]);

  return (
    <div
      ref={ref}
      className={css(styles.element, rightAlign && styles.rightAlign)}
    >
      {renderNode(node as IFieldsNode, getCoords)}
    </div>
  );
}
