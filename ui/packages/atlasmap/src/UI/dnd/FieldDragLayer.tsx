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
import React, { FunctionComponent, useEffect } from 'react';

import { DraggedField } from './DraggedField';
import { Label } from '@patternfly/react-core';
import styles from './FieldDragLayer.module.css';
import { useDimensions } from '../useDimensions';

export const FieldDragLayer: FunctionComponent = () => {
  const [ref, dimensions, measure] = useDimensions();
  useEffect(measure);

  return (
    <DraggedField>
      {({ isDragging, currentOffset, draggedField }) =>
        isDragging && currentOffset && draggedField ? (
          <div
            style={{
              position: 'absolute',
              zIndex: 1000,
              width: dimensions.width,
              height: dimensions.height,
              left: currentOffset.x - dimensions.width / 2,
              top: currentOffset.y - dimensions.height,
            }}
            className={styles.canvasObject}
          >
            <div ref={ref} className={styles.canvasInner}>
              <Label>{draggedField.name}</Label>
            </div>
          </div>
        ) : null
      }
    </DraggedField>
  );
};
