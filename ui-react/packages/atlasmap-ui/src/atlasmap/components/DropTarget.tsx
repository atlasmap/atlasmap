import React, { FunctionComponent, ReactChild, useEffect } from 'react';
import { useDrop } from 'react-dnd';
import { useMappingNode } from '../../canvas';
import { ElementId, IMappings, useLinkable } from '../../views/CanvasView';
import { IFieldElementDragSource } from './DocumentField';

export interface IDropTargetProps {
  node: IMappings;
  addToMapping: (
    elementId: ElementId,
    mappingId: string
  ) => void;
  boxRef: HTMLElement | null;
  children: (props: { isOver: boolean, canDrop: boolean }) => ReactChild;
}

export const DropTarget: FunctionComponent<IDropTargetProps> = ({
  node,
  addToMapping,
  boxRef,
  children
}) => {
  const { ref, getLeftSideCoords, getRightSideCoords } = useLinkable({ getBoxRef: () => boxRef });
  const { setLineNode, unsetLineNode } = useMappingNode();

  const [{ isOver, canDrop, type }, dropRef] = useDrop<
    IFieldElementDragSource,
    void,
    { isOver: boolean; canDrop: boolean, type?: string }
    >({
    accept: ['source', 'target'],
    drop: item => addToMapping(item.id, node.id),
    collect: monitor => ({
      isOver: monitor.isOver(),
      canDrop: monitor.canDrop(),
      type: monitor.getItemType() as string | undefined
    }),
    canDrop: (props, monitor) => {
      const type = monitor.getItemType();
      if (node.sourceFields.length === 1 && node.targetFields.length === 1) {
        if (
          type === 'source' &&
          !node.sourceFields.find(f => f.id === props.id)
        ) {
          return true;
        } else if (!node.targetFields.find(f => f.id === props.id)) {
          return true;
        }
      } else if (
        type === 'source' &&
        node.targetFields.length === 1 &&
        !node.sourceFields.find(f => f.id === props.id)
      ) {
        return true;
      } else if (
        type === 'target' &&
        node.sourceFields.length === 1 &&
        !node.targetFields.find(f => f.id === props.id)
      ) {
        return true;
      }
      return false;
    }
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
  }, [getLeftSideCoords, getRightSideCoords, type, isOver, setLineNode, unsetLineNode, canDrop]);

  const handleRef = (el: HTMLDivElement | null) => {
    ref.current = el;
    dropRef(el)
  };

  return (
    <div ref={handleRef}>
      {children({ canDrop, isOver })}
    </div>
  );
};
