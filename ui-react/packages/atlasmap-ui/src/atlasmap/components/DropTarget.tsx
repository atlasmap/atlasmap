import React, { FunctionComponent, ReactChild, useCallback, useEffect, useRef } from 'react';
import { useDrop } from 'react-dnd';
import { useBoundingCanvasRect, useMappingNode } from '../../canvas';
import { ElementId, IMappings } from '../../views/CanvasView';
import { IFieldElementDragSource } from './DocumentField';

export interface IDropTargetProps {
  node: IMappings;
  boxRef: HTMLElement | null;
  selectedMapping: string | undefined;
  addToMapping: (
    elementId: ElementId,
    mappingId: string
  ) => void;
  children: (props: { isOver: boolean, canDrop: boolean }) => ReactChild;
}

export const DropTarget: FunctionComponent<IDropTargetProps> = ({
  node,
  boxRef,
  selectedMapping,
  addToMapping,
  children
}) => {
  const ref = useRef<HTMLDivElement | null>(null);

  const getBoundingCanvasRect = useBoundingCanvasRect();
  const { setLineNode, unsetLineNode } = useMappingNode();

  const getCoords = useCallback(() => {
    if (ref.current && boxRef) {
      let boxRect = getBoundingCanvasRect(boxRef);
      let dimensions = getBoundingCanvasRect(ref.current);
      return {
        left: dimensions.left,
        right: dimensions.right,
        y: Math.min(
          Math.max(dimensions.top + dimensions.height / 2, boxRect.top),
          boxRect.height + boxRect.top
        ),
      };
    } else {
      return { left: 0, right: 0, y: 0 };
    }
  }, [ref, boxRef, getBoundingCanvasRect]);

  const getFromSourceCoords = () => {
    const { left, y } = getCoords();
    return {
      x: left,
      y,
    };
  };
  const getToTargetCoords = () => {
    const { right, y } = getCoords();
    return {
      x: right,
      y,
    };
  };

  const [{ isOver, canDrop }, dropRef] = useDrop<
    IFieldElementDragSource,
    void,
    { isOver: boolean; canDrop: boolean }
    >({
    accept: ['source', 'target'],
    drop: item => addToMapping(item.id, node.id),
    collect: monitor => ({
      isOver: monitor.isOver(),
      canDrop: monitor.canDrop(),
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
    },
    hover: (_, monitor) => {
      const type = monitor.getItemType();
      const canDrop = monitor.canDrop();
      if (canDrop) {
        setLineNode(
          'dragtarget',
          type === 'source' ? getFromSourceCoords : getToTargetCoords
        );
      }
    },
  });

  setLineNode(`to-${node.id}`, getFromSourceCoords);
  setLineNode(`from-${node.id}`, getToTargetCoords);

  const isSelected = node.id === selectedMapping;

  useEffect(() => {
    if (!isOver) {
      unsetLineNode('dragtarget');
    }
  }, [isOver, isSelected, unsetLineNode]);

  const handleRef = (el: HTMLDivElement | null) => {
    ref.current = el;
    dropRef(el);
  };

  return (
    <div ref={handleRef}>
      {children({ canDrop, isOver })}
    </div>
  );
};
