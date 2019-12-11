import { css, StyleSheet } from '@patternfly/react-styles';
import React, {
  ReactChild,
  useEffect,
} from 'react';
import { useLinkNode } from '../../../canvas';
import { useCanvasViewFieldsContext } from '../CanvasViewFieldsProvider';
import { Coords, IFieldsNode } from '../models';
import { useLinkable } from './useLinkable';

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
  getBoxRef: () => HTMLElement | null;
  rightAlign?: boolean;
  renderNode: (
    node: IFieldsNode,
    getCoords: () => Coords | null,
    boxRef: HTMLElement | null
  ) => ReactChild;
  expandParent: (expanded: boolean) => void;
}

export function FieldElement ({
  node,
  lineConnectionSide,
  getBoxRef,
  getParentRef,
  rightAlign = false,
  renderNode,
  expandParent
}: IFieldElementProps) {
  const { addField, removeField } = useCanvasViewFieldsContext();
  const { setLineNode, unsetLineNode } = useLinkNode();
  const { ref, getLeftSideCoords, getRightSideCoords } = useLinkable({ getBoxRef, getParentRef });

  const getCoords = lineConnectionSide === 'right' ? getRightSideCoords : getLeftSideCoords;

  useEffect(() => {
    setLineNode(node.id, getCoords);
    addField(node.id, {
      requireVisible: expandParent,
    });
    return () => {
      unsetLineNode(node.id);
      removeField(node.id)
    };
  }, [node, setLineNode, unsetLineNode, getCoords, addField, removeField, expandParent]);

  return (
    <div
      ref={ref}
      className={css(styles.element, rightAlign && styles.rightAlign)}
    >
      {renderNode(node as IFieldsNode, getCoords, getBoxRef())}
    </div>
  );
}
