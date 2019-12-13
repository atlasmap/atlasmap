import React, { FunctionComponent, ReactChild, useEffect } from 'react';
import { useDrop } from 'react-dnd';
import { useLinkNode } from '../../Canvas';
import { ElementId, useLinkable } from '../../CanvasView';
import { IFieldElementDragSource } from './DocumentField';

export interface IDropTargetProps {
  onDrop: (elementId: ElementId) => void;
  boxRef: HTMLElement | null;
  isFieldDroppable: (documentType: string, fieldId: string) => boolean;
  children: (props: { isOver: boolean; canDrop: boolean }) => ReactChild;
}

export const DropTarget: FunctionComponent<IDropTargetProps> = ({
  onDrop,
  boxRef,
  isFieldDroppable,
  children,
}) => {
  const { ref, getLeftSideCoords, getRightSideCoords } = useLinkable({
    getBoxRef: () => boxRef,
  });
  const { setLineNode, unsetLineNode } = useLinkNode();

  const [{ isOver, canDrop, type }, dropRef] = useDrop<
    IFieldElementDragSource,
    void,
    { isOver: boolean; canDrop: boolean; type?: string }
  >({
    accept: ['source', 'target'],
    drop: item => onDrop(item.id),
    collect: monitor => ({
      isOver: monitor.isOver(),
      canDrop: monitor.canDrop(),
      type: monitor.getItemType() as string | undefined,
    }),
    canDrop: (props, monitor) => {
      const type = monitor.getItemType();
      return isFieldDroppable(type as string, props.id);
    },
  });

  useEffect(() => {
    if (isOver && type && canDrop) {
      setLineNode(
        'dragtarget',
        type === 'source' ? getLeftSideCoords : getRightSideCoords
      );
    } else {
      unsetLineNode('dragtarget');
    }
  }, [
    getLeftSideCoords,
    getRightSideCoords,
    type,
    isOver,
    setLineNode,
    unsetLineNode,
    canDrop,
  ]);

  const handleRef = (el: HTMLDivElement | null) => {
    ref.current = el;
    dropRef(el);
  };

  return <div ref={handleRef}>{children({ canDrop, isOver })}</div>;
};
