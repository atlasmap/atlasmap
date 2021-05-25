/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import { FunctionComponent, ReactElement } from 'react';
import { XYCoord, useDragLayer } from 'react-dnd';

import { IDragAndDropField } from './models';
import { useFieldsDnd } from './FieldsDndProvider';

export interface IIDraggedFieldChildrenProps {
  isDragging: boolean;
  currentOffset: XYCoord | null;
  draggedField: IDragAndDropField | null;
  getHoveredTarget: () => IDragAndDropField | null;
}

export interface IDraggedFieldProps {
  children: (props: IIDraggedFieldChildrenProps) => ReactElement | null;
}

export const DraggedField: FunctionComponent<IDraggedFieldProps> = ({
  children,
}) => {
  const { getHoveredTarget } = useFieldsDnd();
  const { isDragging, draggedField, currentOffset } = useDragLayer<
    Omit<IIDraggedFieldChildrenProps, 'getHoveredTarget'>
  >((monitor) => ({
    draggedField: monitor.getItem(),
    currentOffset: monitor.getClientOffset(),
    isDragging: monitor.isDragging(),
  }));

  return children({
    isDragging,
    currentOffset,
    draggedField,
    getHoveredTarget,
  });
};
