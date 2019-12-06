import React, { FunctionComponent, useEffect } from 'react';
import { useDrag } from 'react-dnd';
import { getEmptyImage } from 'react-dnd-html5-backend';
import { useMappingNode } from '../../canvas';
import { Coords, ElementId } from '../../views/CanvasView';

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
}

export const DocumentField: FunctionComponent<IDocumentFieldProps> = ({
  id,
  name,
  type,
  documentType,
  showType,
  getCoords
}) => {
  const { setLineNode } = useMappingNode();

  const [{ color }, dragRef, preview] = useDrag<
    IFieldElementDragSource,
    undefined,
    { color: string | undefined }
    >({
    item: { id, type: documentType, name },
    collect: monitor => ({
      color: monitor.isDragging()
        ? 'var(--pf-global--primary-color--100)'
        : undefined,
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
      style={{ color }}
    >
      {name} {showType && `(${type})`}
    </span>
  );
};
