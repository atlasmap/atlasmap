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
} from "./constants";
import { TraverseFields, ITraverseFieldsProps } from "./TraverseFields";

export interface IPropertiesTreeCallbacks {
  onDrop: (source: IAtlasmapField, target: IDragAndDropField) => void;
  canDrop: (source: IAtlasmapField, target: IDragAndDropField) => boolean;
  onShowMappingDetails: (mapping: IAtlasmapMapping) => void;
  canAddToSelectedMapping: (source: IAtlasmapField) => boolean;
  onAddToSelectedMapping: (source: IAtlasmapField) => void;
  canRemoveFromSelectedMapping: (source: IAtlasmapField) => boolean;
  onRemoveFromSelectedMapping: (source: IAtlasmapField) => void;
  onEditProperty: (name: string) => void;
  onDeleteProperty: (name: string) => void;
  canStartMapping: (field: IAtlasmapField) => boolean;
  onStartMapping: (field: IAtlasmapField) => void;
}

export interface IPropertiesTreeProps extends IPropertiesTreeCallbacks {
  fields: IAtlasmapDocument["fields"];
  showTypes: boolean;
  renderPreview: ITraverseFieldsProps["renderPreview"];
}

export const PropertiesTree: FunctionComponent<IPropertiesTreeProps> = ({
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
      parentId={SOURCES_PROPERTIES_ID}
      boundaryId={SOURCES_HEIGHT_BOUNDARY_ID}
      overrideWidth={SOURCES_WIDTH_BOUNDARY_ID}
      idPrefix={SOURCES_FIELD_ID_PREFIX}
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
            onClick={() => onEditProperty(field.name)}
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
            onClick={() => onDeleteProperty(field.name)}
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
