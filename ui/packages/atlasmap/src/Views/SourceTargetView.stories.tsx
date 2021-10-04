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
import { CanvasProvider, FieldsDndProvider } from '../UI';
import { IAtlasmapField, IAtlasmapMapping, SourceTargetView } from '../Views';
import React, { createElement, useState } from 'react';
import {
  constants,
  mappings,
  properties,
  sources,
  targets,
} from '../stories/sampleData';

import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import decorators from '../stories/decorators';

const obj = {
  title: 'AtlasMap|Views',
  decorators,
};
export default obj;

export const sourceTargetView = () =>
  createElement(() => {
    const [selectedMappingId, setSelectedMappingId] = useState<
      string | undefined
    >(undefined);
    const onSelectMapping = (m: IAtlasmapMapping) => setSelectedMappingId(m.id);
    const shouldShowMappingPreviewForField = (f: IAtlasmapField) =>
      boolean('Show mapping preview', false) &&
      !!selectedMappingId &&
      !!f.mappings.find((m) => m.id === selectedMappingId);
    return (
      <FieldsDndProvider>
        <CanvasProvider>
          <SourceTargetView
            sourceEvents={{
              onCaptureDocumentID: action('onCapthreDocumentID'),
              onChangeDocumentName: action('onChangeDocumentName'),
              onCreateConstant: action('onCreateConstant'),
              onEditConstant: action('onEditConstant'),
              onDeleteConstant: action('onDeleteConstant'),
              onCreateProperty: action('onCreateProperty'),
              onEditProperty: action('onEditProperty'),
              onDeleteProperty: action('onDeleteProperty'),
              onDeleteDocument: action('onDeleteDocument'),
              onImportDocument: action('onImportDocument'),
              onCustomClassSearch: action('onCustomClassSearch'),
              onSearch: action('onSearch'),
              onDrop: action('onDrop'),
              canDrop: () => true,
              onAddToSelectedMapping: action('onAddToSelectedMapping'),
              onShowMappingDetails: action('onShowMappingDetails'),
              canAddToSelectedMapping: () => true,
              canAddFieldToSelectedMapping: (item) =>
                !!selectedMappingId &&
                !item.mappings.find((m) => m.id === selectedMappingId),
              onRemoveFromSelectedMapping: action(
                'onRemoveFromSelectedMapping',
              ),
              canRemoveFromSelectedMapping: (item) =>
                !!selectedMappingId &&
                !!item.mappings.find((m) => m.id === selectedMappingId),
              canStartMapping: () => true,
              onStartMapping: action('onStartMapping'),
              shouldShowMappingPreviewForField:
                shouldShowMappingPreviewForField,
              onFieldPreviewChange: action('onFieldPreviewChange'),
              isSource: true,
              acceptDropType: 'target',
              draggableType: 'source',
              onEditCSVParams: action('onEditCSVParams'),
            }}
            targetEvents={{
              onCaptureDocumentID: action('onCapthreDocumentID'),
              onChangeDocumentName: action('onChangeDocumentName'),
              onCreateProperty: action('onCreateProperty'),
              onEditProperty: action('onEditProperty'),
              onDeleteProperty: action('onDeleteProperty'),
              onFieldPreviewChange: action('onFieldPreviewChange'),
              onDeleteDocument: action('onDeleteDocument'),
              onImportDocument: action('onImportDocument'),
              onCustomClassSearch: action('onCustomClassSearch'),
              onSearch: action('onSearch'),
              onDrop: action('onDrop'),
              canDrop: () => true,
              onAddToSelectedMapping: action('onAddToSelectedMapping'),
              onShowMappingDetails: action('onShowMappingDetails'),
              canAddToSelectedMapping: () => true,
              canAddFieldToSelectedMapping: (item) =>
                !!selectedMappingId &&
                !item.mappings.find((m) => m.id === selectedMappingId),
              onRemoveFromSelectedMapping: action(
                'onRemoveFromSelectedMapping',
              ),
              canRemoveFromSelectedMapping: (item) =>
                !!selectedMappingId &&
                !!item.mappings.find((m) => m.id === selectedMappingId),
              canStartMapping: () => true,
              onStartMapping: action('onStartMapping'),
              shouldShowMappingPreviewForField:
                shouldShowMappingPreviewForField,
              isSource: false,
              acceptDropType: 'source',
              draggableType: 'target',
              onEditCSVParams: action('onEditCSVParams'),
            }}
            showTypes={boolean('Show types', false)}
            showMappingPreview={boolean('Show mapping preview', false)}
            sourceProperties={properties}
            targetProperties={properties}
            constants={constants}
            sources={sources}
            mappings={mappings}
            targets={targets}
            selectedMappingId={selectedMappingId}
            onSelectMapping={onSelectMapping}
          />
        </CanvasProvider>
      </FieldsDndProvider>
    );
  });
