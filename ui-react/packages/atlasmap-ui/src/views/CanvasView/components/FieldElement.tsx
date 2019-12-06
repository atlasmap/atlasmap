import { css, StyleSheet } from '@patternfly/react-styles';
import React, {
  ReactElement,
  useEffect,
} from 'react';
import { useMappingNode } from '../../../canvas';
import { Coords, IFieldsGroup, IFieldsNode } from '../models';
import { useLinkable } from './useLinkable';

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
  getBoxRef,
  getParentRef,
  rightAlign = false,
  renderNode,
}: IFieldElementProps) {
  const { setLineNode, unsetLineNode } = useMappingNode();
  const { ref, getLeftSideCoords, getRightSideCoords } = useLinkable({ getBoxRef, getParentRef });

  const getCoords = lineConnectionSide === 'right' ? getRightSideCoords : getLeftSideCoords;

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
