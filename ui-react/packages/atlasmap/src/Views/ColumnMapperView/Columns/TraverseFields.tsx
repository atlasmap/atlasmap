import React, { FunctionComponent } from "react";

import { Button, Tooltip } from "@patternfly/react-core";
import {
  BoltIcon,
  BullseyeIcon,
  CircleIcon,
  GripVerticalIcon,
  LayerGroupIcon,
} from "@patternfly/react-icons";

import {
  DelayedBoolean,
  DocumentField,
  DocumentGroup,
  DraggableField,
  FieldDropTarget,
  IDocumentFieldProps,
  IDragAndDropField,
  NodeRef,
  TreeGroup,
  TreeItem,
} from "../../../UI";
import {
  AtlasmapDocumentType,
  AtlasmapFields,
  IAtlasmapField,
  IAtlasmapGroup,
} from "../../../Views";

export interface ITraverseFields
  extends Omit<Omit<IFieldOrGroupProps, "field">, "fieldId"> {
  fields: AtlasmapFields;
}

export const TraverseFields: FunctionComponent<ITraverseFields> = ({
  fields,
  idPrefix,
  ...props
}) => {
  return (
    <>
      {fields.map((field, idx) => (
        <FieldOrGroup
          key={idx}
          field={field}
          idPrefix={idPrefix}
          setSize={fields.length}
          position={idx + 1}
          {...props}
        />
      ))}
    </>
  );
};

export interface IFieldOrGroupProps
  extends Omit<Omit<ITreeItemFieldAndNodeRefsAndDnDProps, "field">, "fieldId">,
    Omit<Omit<ITreeGroupAndNodeRefsAndDnDProps, "group">, "fieldId"> {
  idPrefix: string;
  field: IAtlasmapGroup | IAtlasmapField;
}

const FieldOrGroup: FunctionComponent<IFieldOrGroupProps> = ({
  field,
  idPrefix,
  level = 1,
  ...props
}) => {
  const fieldId = `${idPrefix}${field.id}`;
  const maybeGroup = field as IAtlasmapGroup;
  const maybeField = field as IAtlasmapField;
  if (maybeGroup.fields) {
    return (
      <TreeGroupAndNodeRefsAndDnD
        fieldId={fieldId}
        group={maybeGroup}
        level={level}
        {...props}
      >
        <TraverseFields
          {
            ...props /* spreading the props must be done before everything else so to override the values fed to the Group */
          }
          fields={maybeGroup.fields as AtlasmapFields}
          parentId={fieldId}
          level={level + 1}
          idPrefix={idPrefix}
        />
      </TreeGroupAndNodeRefsAndDnD>
    );
  }
  return (
    <TreeItemWithFieldAndNodeRefsAndDnD
      fieldId={fieldId}
      field={maybeField}
      level={level}
      {...props}
    />
  );
};

interface ITreeGroupAndNodeRefsAndDnDProps {
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

const TreeGroupAndNodeRefsAndDnD: FunctionComponent<ITreeGroupAndNodeRefsAndDnDProps> = ({
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

interface ITreeItemFieldAndNodeRefsAndDnDProps {
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
  canDrop: (source: IAtlasmapField, target: IDragAndDropField) => boolean;
  onDrop: (source: IAtlasmapField, target: IDragAndDropField) => void;
}

const TreeItemWithFieldAndNodeRefsAndDnD: FunctionComponent<ITreeItemFieldAndNodeRefsAndDnDProps> = ({
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
}) => (
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
                <DocumentField
                  name={field.name}
                  icon={
                    isDroppable || isTarget ? (
                      <Button
                        variant={isTarget ? "link" : "plain"}
                        tabIndex={-1}
                        aria-label={"Drop target"}
                        isDisabled={!isDroppable}
                      >
                        <BullseyeIcon size="sm" />
                      </Button>
                    ) : (
                      <Button
                        variant={"plain"}
                        tabIndex={-1}
                        aria-hidden={true}
                      >
                        <GripVerticalIcon />
                      </Button>
                    )
                  }
                  type={field.type}
                  showType={showTypes}
                  isDragging={isDragging}
                  isFocused={focused}
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
                />
              )}
            </TreeItem>
          </NodeRef>
        )}
      </DraggableField>
    )}
  </FieldDropTarget>
);
