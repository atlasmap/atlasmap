import { css, StyleSheet } from '@patternfly/react-styles';
import React, { FunctionComponent, useEffect, useRef } from 'react';
import { useDrag } from 'react-dnd';
import { getEmptyImage } from 'react-dnd-html5-backend';
import { Coords, useLinkNode } from '../../Canvas';
import { ElementId, IMapping } from '../../CanvasView';
import { IAtlasmapField } from '../models';
import { Button, Label, ButtonVariant } from '@patternfly/react-core';
import {
  AddCircleOIcon,
  EditIcon,
  MinusCircleIcon,
  TrashIcon,
} from '@patternfly/react-icons';

const styles = StyleSheet.create({
  buttonRightAlign: {
    alignItems: 'right',
    alignSelf: 'right',
    paddingRight: '0.5rem !important',
    direction: 'rtl',
    float: 'right',
  },
  element: {
    display: 'flex',
    flexFlow: 'column',
    width: '70%',
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
  onCreateMapping: (target: IAtlasmapField) => void;
  onAddToMapping: (mapping: IMapping) => void;
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
  isConstantOrProperty: boolean;
  onCreateMapping: (target: IAtlasmapField) => void;
  onDropToMapping: (mapping: IMapping) => void;
  onClickAddToMapping: () => void;
  onDeleteConstProp: (field: string) => void;
  onEditConstProp: (field: string) => void;
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
  onClickAddToMapping,
  onDeleteConstProp,
  onEditConstProp,
  onDropToMapping,
  onCreateMapping,
  isOver = false,
  isConstantOrProperty,
  children,
}) => {
  const { setLineNode } = useLinkNode();
  const ref = useRef<HTMLDivElement | null>();
  const [{ isDragging }, dragRef, preview] = useDrag<
    IFieldElementDragSource,
    undefined,
    { isDragging: boolean }
  >({
    item: {
      id,
      type: documentType,
      name,
      onAddToMapping: onDropToMapping,
      onCreateMapping,
    },
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
            onClick={onClickAddToMapping}
            isInline={true}
            icon={isSelected ? <MinusCircleIcon /> : <AddCircleOIcon />}
          >
            {content}
          </Button>
        ) : (
          content
        )}
        {showType && ` (${type})`}
        {isConstantOrProperty && (
          <span>
            <Button
              className={css(styles.buttonRightAlign)}
              variant={ButtonVariant.plain}
              aria-label="Edit constant or property"
              key={'edit-element'}
              onClick={() => onEditConstProp(name)}
            >
              <EditIcon size="sm" />
            </Button>
            <Button
              className={css(styles.buttonRightAlign)}
              variant={ButtonVariant.plain}
              aria-label="Delete constant or property"
              key={'delete-element'}
              onClick={() => onDeleteConstProp(name)}
            >
              <TrashIcon size="sm" />
            </Button>
          </span>
        )}
      </span>
      {children}
    </span>
  );
};
