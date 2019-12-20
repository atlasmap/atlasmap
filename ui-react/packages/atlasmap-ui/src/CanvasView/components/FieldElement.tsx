import { css, StyleSheet } from '@patternfly/react-styles';
import React, { ReactChild, useEffect } from 'react';
import { useLinkNode } from '../../Canvas';
import { useCanvasViewFieldsContext } from '../CanvasViewFieldsProvider';
import { IFieldsNode } from '../models';
import { useLinkable } from './useLinkable';
import { Coords } from '../../Canvas/models';
import { useBoxContext } from "./BoxProvider";

const styles = StyleSheet.create({
  element: {
    padding:
      '0.1rem var(--pf-c-accordion__toggle--PaddingRight) 0.1rem calc(var(--pf-c-accordion__toggle--PaddingLeft))',
    cursor: 'pointer',
  },
  rightAlign: {
    transform: 'scaleX(-1)',
    padding:
      '0.1rem var(--pf-c-accordion__toggle--PaddingLeft) 0.1rem var(--pf-c-accordion__toggle--PaddingRight)',
  },
});

export interface IFieldElementProps {
  node: IFieldsNode;
  lineConnectionSide: 'left' | 'right';
  getParentRef: () => HTMLElement | null;
  rightAlign?: boolean;
  renderNode: (
    node: IFieldsNode,
    getCoords: () => Coords | null,
    scrollableAreaRef: HTMLElement | null
  ) => ReactChild;
  expandParent: (expanded: boolean) => void;
}

export function FieldElement({
  node,
  lineConnectionSide,
  getParentRef,
  rightAlign = false,
  renderNode,
  expandParent,
}: IFieldElementProps) {
  const { addField, removeField } = useCanvasViewFieldsContext();
  const { setLineNode, unsetLineNode } = useLinkNode();
  const { getScrollableAreaRef } = useBoxContext();
  const { ref, getLeftSideCoords, getRightSideCoords } = useLinkable({
    getScrollableAreaRef,
    getParentRef,
  });

  const getCoords =
    lineConnectionSide === 'right' ? getRightSideCoords : getLeftSideCoords;

  useEffect(() => {
    setLineNode(node.id, getCoords);
    addField(node.id, {
      requireVisible: expandParent,
    });
    return () => {
      unsetLineNode(node.id);
      removeField(node.id);
    };
  }, [
    node,
    setLineNode,
    unsetLineNode,
    getCoords,
    addField,
    removeField,
    expandParent,
  ]);

  return (
    <div
      ref={ref}
      className={css(styles.element, rightAlign && styles.rightAlign)}
    >
      {renderNode(node as IFieldsNode, getCoords, getScrollableAreaRef())}
    </div>
  );
}
