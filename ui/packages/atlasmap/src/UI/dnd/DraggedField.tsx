import { FunctionComponent, ReactElement } from "react";
import { useDragLayer, XYCoord } from "react-dnd";

import { IDragAndDropField } from "./models";
import { useFieldsDnd } from "./FieldsDndProvider";

export interface IIDraggedFieldChildrenProps {
  isDragging: boolean;
  currentOffset: XYCoord | null;
  draggedField: IDragAndDropField | null;
  hoveredTarget: IDragAndDropField | null;
}

export interface IDraggedFieldProps {
  children: (props: IIDraggedFieldChildrenProps) => ReactElement | null;
}

export const DraggedField: FunctionComponent<IDraggedFieldProps> = ({
  children,
}) => {
  const { hoveredTarget } = useFieldsDnd();
  const { isDragging, draggedField, currentOffset } = useDragLayer<
    Omit<IIDraggedFieldChildrenProps, "hoveredTarget">
  >((monitor) => ({
    draggedField: monitor.getItem(),
    currentOffset: monitor.getClientOffset(),
    isDragging: monitor.isDragging(),
  }));

  return children({ isDragging, currentOffset, draggedField, hoveredTarget });
};
