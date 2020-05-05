import React, { FunctionComponent, ReactNode } from "react";

import { Button, Tooltip } from "@patternfly/react-core";
import {
  BoltIcon,
  BullseyeIcon,
  CircleIcon,
  GripVerticalIcon,
  LayerGroupIcon,
} from "@patternfly/react-icons";

import {
  DocumentField,
  DraggableField,
  FieldDropTarget,
  IDocumentFieldProps,
  IDragAndDropField,
  NodeRef,
  TreeItem,
} from "../../../UI";
import { AtlasmapDocumentType, IAtlasmapField } from "../../../Views";

export interface ITreeItemFieldAndNodeRefsAndDnDProps {
  fieldId: string;
  field: IAtlasmapField;
  showTypes: boolean;
  boundaryId?: string;
  overrideWidth?: string;
  parentId: string;
  acceptDropType: AtlasmapDocumentType;
  draggableType: AtlasmapDocumentType;
  level?: number;
  position?: number;
  setSize?: number;
  renderActions?: (field: IAtlasmapField) => IDocumentFieldProps["actions"];
  renderPreview?: (field: IAtlasmapField) => ReactNode;
  canDrop: (source: IAtlasmapField, target: IDragAndDropField) => boolean;
  onDrop: (source: IAtlasmapField, target: IDragAndDropField) => void;
}

export const TreeItemWithFieldAndNodeRefsAndDnD: FunctionComponent<ITreeItemFieldAndNodeRefsAndDnDProps> = ({
  fieldId,
  field,
  showTypes,
  boundaryId,
  overrideWidth,
  parentId,
  acceptDropType,
  draggableType,
  level = 1,
  position = 1,
  setSize = 1,
  canDrop,
  onDrop,
  renderActions = () => [],
  renderPreview,
}) => {
  const preview = renderPreview && renderPreview(field);
  return (
    <FieldDropTarget
      key={fieldId}
      target={{
        id: field.id,
        name: field.name,
        type: draggableType,
        payload: field,
      }}
      canDrop={(item) => canDrop(field, item)}
      accept={[acceptDropType]}
    >
      {({ isDroppable, isTarget }) => (
        <DraggableField
          field={{
            type: draggableType,
            id: field.id,
            name: field.name,
            payload: field,
          }}
          onDrop={(_, target) => onDrop(field, target)}
        >
          {({ isDragging }) => (
            <NodeRef
              id={[
                fieldId,
                isDragging ? "dnd-start" : undefined,
                isTarget ? "dnd-target-field" : undefined,
              ]}
              parentId={parentId}
              boundaryId={boundaryId}
              overrideWidth={overrideWidth}
            >
              <TreeItem level={level} position={position} setSize={setSize}>
                {({ focused }) => (
                  <>
                    <DocumentField
                      name={field.name}
                      icon={
                        isDroppable || isTarget ? (
                          <Button
                            variant={isTarget ? "link" : "plain"}
                            tabIndex={-1}
                            aria-label={"Drop target"}
                            isDisabled={!isDroppable}
                            data-testid={`is-droppable-${field.name}-button`}
                          >
                            <BullseyeIcon size="sm" />
                          </Button>
                        ) : (
                          <Button
                            variant={"plain"}
                            tabIndex={-1}
                            aria-hidden={true}
                            data-testid={`grip-${field.name}-button`}
                          >
                            <GripVerticalIcon />
                          </Button>
                        )
                      }
                      type={field.type}
                      showType={showTypes}
                      isDragging={isDragging}
                      isFocused={focused}
                      isSelected={!!preview}
                      statusIcons={[
                        field.isConnected ? (
                          <Tooltip
                            key="connected"
                            position={"auto"}
                            enableFlip={true}
                            content={<div>This field is connected</div>}
                          >
                            <CircleIcon
                              label={"This field is connected"}
                              size="sm"
                              tabIndex={-1}
                            />
                          </Tooltip>
                        ) : null,
                        field.isCollection ? (
                          <Tooltip
                            key={"collection"}
                            position={"auto"}
                            enableFlip={true}
                            content={<div>This field is a collection</div>}
                          >
                            <LayerGroupIcon
                              label={"This field is a collection"}
                              size="sm"
                              tabIndex={-1}
                            />
                          </Tooltip>
                        ) : null,
                        field.hasTransformations ? (
                          <Tooltip
                            key={"transformations"}
                            position={"auto"}
                            enableFlip={true}
                            content={<div>This field has transformations</div>}
                          >
                            <BoltIcon
                              label={"This field has transformations"}
                              size="sm"
                              tabIndex={-1}
                            />
                          </Tooltip>
                        ) : null,
                      ]}
                      actions={renderActions(field)}
                    >
                      {preview}
                    </DocumentField>
                  </>
                )}
              </TreeItem>
            </NodeRef>
          )}
        </DraggableField>
      )}
    </FieldDropTarget>
  );
};
