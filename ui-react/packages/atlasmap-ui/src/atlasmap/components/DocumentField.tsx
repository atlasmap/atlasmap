import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, useEffect } from 'react';
import { useDrag } from 'react-dnd';
import { getEmptyImage } from 'react-dnd-html5-backend';
import { useLinkNode } from '../../canvas';
import { Coords, ElementId } from '../../views/CanvasView';


const styles = StyleSheet.create({
  element: {
    display: 'flex',
  },
  isSelected: {
    background: 'var(--pf-global--BackgroundColor--150)',
    color: 'var(--pf-global--Color--100)',
    padding: '0.5rem'
  },
  isDragging: {
    color: 'var(--pf-global--primary-color--100)',
  }
});


export interface IFieldElementDragSource {
  id: ElementId;
  type: string;
  name: string;
}

export interface IDocumentFieldProps {
  id: string;
  name: string;
  type: string;
  documentType: string;
  showType: boolean;
  getCoords: () => Coords | null;
  isSelected: boolean;
}

export const DocumentField: FunctionComponent<IDocumentFieldProps> = ({
  id,
  name,
  type,
  documentType,
  showType,
  getCoords,
  isSelected
}) => {
  const { setLineNode } = useLinkNode();

  const [{ isDragging }, dragRef, preview] = useDrag<
    IFieldElementDragSource,
    undefined,
    { isDragging: boolean }
    >({
    item: { id, type: documentType, name },
    collect: monitor => ({
      isDragging: monitor.isDragging(),
    }),
    begin: () => {
      setLineNode('dragsource', getCoords);
    },
  });

  useEffect(() => {
    preview(getEmptyImage(), { captureDraggingState: true });
  }, [preview]);

  return (
    <span
      ref={dragRef}
      className={css(
        styles.element,
        isSelected && styles.isSelected,
        isDragging && styles.isDragging
      )}
    >
      {name} {showType && `(${type})`}
    </span>
  );
};
