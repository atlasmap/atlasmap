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
import { AtlasmapDocumentType, IAtlasmapGroup } from '../../../Views';
import {
  DelayedBoolean,
  DocumentGroup,
  FieldDropTarget,
  ITreeGroupProps,
  NodeRef,
  TreeGroup,
} from '../../../UI';
import React, { FunctionComponent } from 'react';

import { LayerGroupIcon } from '@patternfly/react-icons';

export interface ITreeGroupAndNodeRefsAndDnDProps {
  fieldId: string;
  group: IAtlasmapGroup;
  showTypes: boolean;
  boundaryId?: string;
  overrideWidth?: string;
  parentId: string;
  acceptDropType: AtlasmapDocumentType;
  draggableType: AtlasmapDocumentType;
  level?: number;
  position?: number;
  setSize?: number;
  children: ITreeGroupProps['children'];
}

export const TreeGroupAndNodeRefsAndDnD: FunctionComponent<
  ITreeGroupAndNodeRefsAndDnDProps
> = ({
  fieldId,
  group,
  showTypes,
  boundaryId,
  overrideWidth,
  parentId,
  acceptDropType,
  draggableType,
  level = 1,
  position = 1,
  setSize = 1,
  children,
}) => (
  <FieldDropTarget
    key={fieldId}
    target={{
      id: group.id,
      name: group.name,
      type: draggableType,
    }}
    canDrop={() => false}
    accept={[acceptDropType]}
  >
    {({ isOver }) => (
      <DelayedBoolean value={isOver}>
        {(isOver) => (
          <NodeRef
            id={fieldId}
            parentId={parentId}
            boundaryId={boundaryId}
            overrideWidth={overrideWidth}
          >
            <TreeGroup
              id={fieldId}
              level={level}
              position={position}
              setSize={setSize}
              expanded={isOver === true ? true : undefined}
              renderLabel={({ expanded }) => (
                <DocumentGroup
                  name={group.name}
                  type={group.type}
                  showType={showTypes}
                  icon={group.isCollection ? <LayerGroupIcon /> : undefined}
                  iconTooltip={
                    group.isCollection
                      ? 'This object is a collection'
                      : undefined
                  }
                  expanded={isOver || expanded}
                />
              )}
            >
              {children}
            </TreeGroup>
          </NodeRef>
        )}
      </DelayedBoolean>
    )}
  </FieldDropTarget>
);
