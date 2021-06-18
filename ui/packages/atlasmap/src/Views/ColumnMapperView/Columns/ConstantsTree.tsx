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
import { Button, Tooltip } from '@patternfly/react-core';
import { EditIcon, TrashIcon } from '@patternfly/react-icons';
import {
  IAtlasmapDocument,
  IAtlasmapField,
  IAtlasmapMapping,
} from '../../models';
import { IDragAndDropField, Tree } from '../../../UI';
import { ITraverseFieldsProps, TraverseFields } from './TraverseFields';
import React, { FunctionComponent } from 'react';
import {
  SOURCES_CONSTANTS_ID,
  SOURCES_DRAGGABLE_TYPE,
  SOURCES_FIELD_ID_PREFIX,
  SOURCES_HEIGHT_BOUNDARY_ID,
  SOURCES_WIDTH_BOUNDARY_ID,
  TARGETS_DRAGGABLE_TYPE,
} from './constants';

import { commonActions } from './commonActions';

export interface IConstantsTreeCallbacks {
  onDrop: (source: IAtlasmapField, target: IDragAndDropField | null) => void;
  canDrop: (source: IAtlasmapField, target: IDragAndDropField) => boolean;
  onShowMappingDetails: (mapping: IAtlasmapMapping) => void;
  canAddFieldToSelectedMapping: (source: IAtlasmapField) => boolean;
  onAddToSelectedMapping: (source: IAtlasmapField) => void;
  canRemoveFromSelectedMapping: (source: IAtlasmapField) => boolean;
  onRemoveFromSelectedMapping: (source: IAtlasmapField) => void;
  onEditConstant: (name: string, value: string) => void;
  onDeleteConstant: (name: string) => void;
  canStartMapping: (field: IAtlasmapField) => boolean;
  onStartMapping: (field: IAtlasmapField) => void;
}

export interface IConstantsTreeProps extends IConstantsTreeCallbacks {
  fields: IAtlasmapDocument['fields'];
  renderPreview: ITraverseFieldsProps['renderPreview'];
}

export const ConstantsTree: FunctionComponent<IConstantsTreeProps> = ({
  fields,
  onDrop,
  canDrop,
  onShowMappingDetails,
  canAddFieldToSelectedMapping,
  onAddToSelectedMapping,
  canRemoveFromSelectedMapping,
  onRemoveFromSelectedMapping,
  onEditConstant,
  onDeleteConstant,
  canStartMapping,
  onStartMapping,
  renderPreview,
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
          canAddFieldToSelectedMapping: canAddFieldToSelectedMapping(field),
          onAddToSelectedMapping: () => onAddToSelectedMapping(field),
          canRemoveFromSelectedMapping: canRemoveFromSelectedMapping(field),
          onRemoveFromSelectedMapping: () => onRemoveFromSelectedMapping(field),
          canStartMapping: canStartMapping(field),
          onStartMapping: () => onStartMapping(field),
        }),
        <Tooltip
          key={'edit'}
          position={'top'}
          enableFlip={true}
          content={<div>Edit constant</div>}
          entryDelay={750}
          exitDelay={100}
        >
          <Button
            variant="plain"
            onClick={() => onEditConstant(field.name, field.value)}
            aria-label={'Edit constant'}
            tabIndex={0}
            data-testid={`edit-constant-${field.name}-button`}
          >
            <EditIcon />
          </Button>
        </Tooltip>,
        <Tooltip
          key={'delete'}
          position={'top'}
          enableFlip={true}
          content={<div>Remove constant</div>}
          entryDelay={750}
          exitDelay={100}
        >
          <Button
            variant="plain"
            onClick={() => onDeleteConstant(field.name)}
            aria-label={'Remove constant'}
            tabIndex={0}
            data-testid={`remove-constant-${field.name}-button`}
          >
            <TrashIcon />
          </Button>
        </Tooltip>,
      ]}
      renderPreview={renderPreview}
    />
  </Tree>
);
