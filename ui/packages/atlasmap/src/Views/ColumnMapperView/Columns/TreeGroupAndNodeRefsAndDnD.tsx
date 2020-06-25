import React, { FunctionComponent } from "react";

import {
  LayerGroupIcon,
  FolderOpenIcon,
  FolderCloseIcon,
} from "@patternfly/react-icons";

import {
  DelayedBoolean,
  DocumentGroup,
  FieldDropTarget,
  NodeRef,
  TreeGroup,
  ITreeGroupProps,
} from "../../../UI";
import { AtlasmapDocumentType, IAtlasmapGroup } from "../../../Views";
import { css, StyleSheet } from "@patternfly/react-styles";

const style = StyleSheet.create({
  collection: {
    color: "white",
    height: ".5rem",
    marginLeft: "-1rem",
    marginBottom: ".2rem",
  },
});

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
  children: ITreeGroupProps["children"];
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
              expanded={isOver === true ? true : undefined}
              renderLabel={({ expanded }) => {
                const icon = group.isCollection ? (
                  isOver || expanded ? (
                    <span>
                      <FolderOpenIcon />
                      <LayerGroupIcon className={css(style.collection)} />
                    </span>
                  ) : (
                    <span>
                      <FolderCloseIcon />
                      <LayerGroupIcon className={css(style.collection)} />
                    </span>
                  )
                ) : undefined;
                return (
                  <DocumentGroup
                    name={group.name}
                    type={group.type}
                    showType={showTypes}
                    icon={icon}
                    expanded={isOver || expanded}
                  />
                );
              }}
            >
              {children}
            </TreeGroup>
          </NodeRef>
        )}
      </DelayedBoolean>
    )}
  </FieldDropTarget>
);
