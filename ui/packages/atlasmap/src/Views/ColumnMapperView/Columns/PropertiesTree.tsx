import React, { FunctionComponent } from "react";

import { Button, Tooltip } from "@patternfly/react-core";
import { EditIcon, TrashIcon } from "@patternfly/react-icons";

import { Tree, IDragAndDropField } from "../../../UI";
import {
  IAtlasmapDocument,
  IAtlasmapField,
  IAtlasmapMapping,
} from "../../models";
import { commonActions } from "./commonActions";
import {
  SOURCES_DRAGGABLE_TYPE,
  SOURCES_FIELD_ID_PREFIX,
  SOURCES_HEIGHT_BOUNDARY_ID,
  SOURCES_PROPERTIES_ID,
  SOURCES_WIDTH_BOUNDARY_ID,
  TARGETS_DRAGGABLE_TYPE,
  TARGETS_FIELD_ID_PREFIX,
  TARGETS_HEIGHT_BOUNDARY_ID,
  TARGETS_PROPERTIES_ID,
  TARGETS_WIDTH_BOUNDARY_ID,
} from "./constants";
import { TraverseFields, ITraverseFieldsProps } from "./TraverseFields";

export interface IPropertiesTreeCallbacks {
  isSource: boolean;
  onDrop: (source: IAtlasmapField, target: IDragAndDropField) => void;
  canDrop: (source: IAtlasmapField, target: IDragAndDropField) => boolean;
  onShowMappingDetails: (mapping: IAtlasmapMapping) => void;
  canAddToSelectedMapping: (source: IAtlasmapField) => boolean;
  onAddToSelectedMapping: (source: IAtlasmapField) => void;
  canRemoveFromSelectedMapping: (source: IAtlasmapField) => boolean;
  onRemoveFromSelectedMapping: (source: IAtlasmapField) => void;
  onEditProperty: (name: string, isSource: boolean) => void;
  onDeleteProperty: (name: string, isSource: boolean) => void;
  canStartMapping: (field: IAtlasmapField) => boolean;
  onStartMapping: (field: IAtlasmapField) => void;
}

export interface IPropertiesTreeProps extends IPropertiesTreeCallbacks {
  fields: IAtlasmapDocument["fields"];
  showTypes: boolean;
  renderPreview: ITraverseFieldsProps["renderPreview"];
}

export const PropertiesTree: FunctionComponent<IPropertiesTreeProps> = ({
  isSource,
  fields,
  showTypes,
  onDrop,
  canDrop,
  onShowMappingDetails,
  canAddToSelectedMapping,
  onAddToSelectedMapping,
  canRemoveFromSelectedMapping,
  onRemoveFromSelectedMapping,
  onEditProperty,
  onDeleteProperty,
  canStartMapping,
  onStartMapping,
  renderPreview,
}) => (
  <Tree>
    <TraverseFields
      fields={fields}
      showTypes={showTypes}
      parentId={isSource ? SOURCES_PROPERTIES_ID : TARGETS_PROPERTIES_ID}
      boundaryId={
        isSource ? SOURCES_HEIGHT_BOUNDARY_ID : TARGETS_HEIGHT_BOUNDARY_ID
      }
      overrideWidth={
        isSource ? SOURCES_WIDTH_BOUNDARY_ID : TARGETS_WIDTH_BOUNDARY_ID
      }
      idPrefix={isSource ? SOURCES_FIELD_ID_PREFIX : TARGETS_FIELD_ID_PREFIX}
      acceptDropType={TARGETS_DRAGGABLE_TYPE}
      draggableType={SOURCES_DRAGGABLE_TYPE}
      onDrop={onDrop}
      canDrop={canDrop}
      renderActions={(field) => [
        ...commonActions({
          connectedMappings: field.mappings,
          onShowMappingDetails: onShowMappingDetails,
          canAddToSelectedMapping: canAddToSelectedMapping(field),
          onAddToSelectedMapping: () => onAddToSelectedMapping(field),
          canRemoveFromSelectedMapping: canRemoveFromSelectedMapping(field),
          onRemoveFromSelectedMapping: () => onRemoveFromSelectedMapping(field),
          canStartMapping: canStartMapping(field),
          onStartMapping: () => onStartMapping(field),
        }),
        <Tooltip
          key={"edit"}
          position={"top"}
          enableFlip={true}
          content={<div>Edit property</div>}
        >
          <Button
            variant="plain"
            onClick={() => onEditProperty(field.name, isSource)}
            aria-label={"Edit property"}
            tabIndex={0}
            data-testid={`edit-property-${field.name}-button`}
          >
            <EditIcon />
          </Button>
        </Tooltip>,
        <Tooltip
          key={"delete"}
          position={"top"}
          enableFlip={true}
          content={<div>Remove property</div>}
        >
          <Button
            variant="plain"
            onClick={() => onDeleteProperty(field.name, isSource)}
            aria-label={"Remove property"}
            tabIndex={0}
            data-testid={`remove-property-${field.name}-button`}
          >
            <TrashIcon />
          </Button>
        </Tooltip>,
      ]}
      renderPreview={renderPreview}
    />
  </Tree>
);
