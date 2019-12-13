import { AddCircleOIcon } from '@patternfly/react-icons';
import { Button, Label } from '@patternfly/react-core';
import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, useEffect, useRef } from 'react';
import { useDrag } from 'react-dnd';
import { getEmptyImage } from 'react-dnd-html5-backend';
import { Coords, useLinkNode } from '../../Canvas';
import { ElementId } from '../../CanvasView';

const styles = StyleSheet.create({
  element: {
    display: 'flex',
    flexFlow: 'column',
  },
  isDragging: {
    color: 'var(--pf-global--active-color--400)',
  },
  isOver: {
    color: 'var(--pf-global--active-color--400)',
  },
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
  showAddToMapping: boolean;
  isOver?: boolean;
  onAddToMapping: () => void;
}

export const DocumentField: FunctionComponent<IDocumentFieldProps> = ({
  id,
  name,
  type,
  documentType,
  showType,
  getCoords,
  isSelected,
  showAddToMapping,
  onAddToMapping,
  isOver = false,
  children,
}) => {
  const { setLineNode } = useLinkNode();

  const ref = useRef<HTMLDivElement | null>();
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

  useEffect(() => {
    if (ref.current) {
      ref.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [isSelected]);

  const handleRef = (el: HTMLDivElement | null) => {
    dragRef(el);
    ref.current = el;
  };

  const content = isSelected ? <Label>{name}</Label> : name;

  return (
    <span
      ref={handleRef}
      className={css(
        styles.element,
        isOver && styles.isOver,
        isDragging && styles.isDragging
      )}
    >
      <span>
        {showAddToMapping ? (
          <Button
            variant={'link'}
            onClick={onAddToMapping}
            isInline={true}
            icon={<AddCircleOIcon />}
          >
            {content}
          </Button>
        ) : (
          content
        )}
        {showType && ` (${type})`}
      </span>
      {children}
    </span>
  );
};
