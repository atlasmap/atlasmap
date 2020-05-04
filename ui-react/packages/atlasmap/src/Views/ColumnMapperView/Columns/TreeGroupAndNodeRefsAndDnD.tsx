import React, { FunctionComponent } from "react";

import { LayerGroupIcon } from "@patternfly/react-icons";

import {
  DelayedBoolean,
  DocumentGroup,
  FieldDropTarget,
  NodeRef,
  TreeGroup,
} from "../../../UI";
import { AtlasmapDocumentType, IAtlasmapGroup } from "../../../Views";

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
}

export const TreeGroupAndNodeRefsAndDnD: FunctionComponent<ITreeGroupAndNodeRefsAndDnDProps> = ({
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
              expanded={isOver === true}
              renderLabel={({ expanded }) => (
                <DocumentGroup
                  name={group.name}
                  type={group.type}
                  showType={showTypes}
                  icon={group.isCollection ? <LayerGroupIcon /> : undefined}
                  expanded={isOver || expanded}
                />
              )}
            >
              {() => children}
            </TreeGroup>
          </NodeRef>
        )}
      </DelayedBoolean>
    )}
  </FieldDropTarget>
);
