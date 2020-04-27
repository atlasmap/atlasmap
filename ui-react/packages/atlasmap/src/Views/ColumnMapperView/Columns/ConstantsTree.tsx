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
  SOURCES_CONSTANTS_ID,
  SOURCES_DRAGGABLE_TYPE,
  SOURCES_FIELD_ID_PREFIX,
  SOURCES_HEIGHT_BOUNDARY_ID,
  SOURCES_WIDTH_BOUNDARY_ID,
  TARGETS_DRAGGABLE_TYPE,
} from "./constants";
import { TraverseFields } from "./TraverseFields";

export interface IConstantsTreeCallbacks {
  onDrop: (source: IAtlasmapField, target: IDragAndDropField) => void;
  canDrop: (source: IAtlasmapField, target: IDragAndDropField) => boolean;
  onShowMappingDetails: (mapping: IAtlasmapMapping) => void;
  canAddToSelectedMapping: (source: IAtlasmapField) => boolean;
  onAddToSelectedMapping: (source: IAtlasmapField) => void;
  canRemoveFromSelectedMapping: (source: IAtlasmapField) => boolean;
  onRemoveFromSelectedMapping: (source: IAtlasmapField) => void;
  onEditConstant: (value: string) => void;
  onDeleteConstant: (value: string) => void;
}

export interface IConstantsTreeProps extends IConstantsTreeCallbacks {
  fields: IAtlasmapDocument["fields"];
}

export const ConstantsTree: FunctionComponent<IConstantsTreeProps> = ({
  fields,
  onDrop,
  canDrop,
  onShowMappingDetails,
  canAddToSelectedMapping,
  onAddToSelectedMapping,
  canRemoveFromSelectedMapping,
  onRemoveFromSelectedMapping,
  onEditConstant,
  onDeleteConstant,
}) => (
  <Tree>
    <TraverseFields
      fields={fields}
      showTypes={false}
      parentId={SOURCES_CONSTANTS_ID}
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
          content={<div>Edit constant</div>}
        >
          <Button
            variant="plain"
            onClick={() => onEditConstant(field.name)}
            aria-label={"Edit constant"}
            tabIndex={0}
          >
            <EditIcon />
          </Button>
        </Tooltip>,
        <Tooltip
          key={"delete"}
          position={"top"}
          enableFlip={true}
          content={<div>Remove constant</div>}
        >
          <Button
            variant="plain"
            onClick={() => onDeleteConstant(field.name)}
            aria-label={"Remove constant"}
            tabIndex={0}
          >
            <TrashIcon />
          </Button>
        </Tooltip>,
      ]}
    />
  </Tree>
);
