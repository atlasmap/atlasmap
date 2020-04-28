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
import { TraverseFields } from "./TraverseFields";

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
}

export interface IPropertiesTreeProps extends IPropertiesTreeCallbacks {
  fields: IAtlasmapDocument["fields"];
  showTypes: boolean;
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
          onStartMapping: () => void 0,
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
          >
            <TrashIcon />
          </Button>
        </Tooltip>,
      ]}
    />
  </Tree>
);
