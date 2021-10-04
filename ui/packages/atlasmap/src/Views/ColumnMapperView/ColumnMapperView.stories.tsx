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
import { CanvasProvider, Column, FieldsDndProvider } from '../../UI';
import { SourceTargetLinks, SourcesColumn, TargetsColumn } from '../../Views';
import { boolean, text } from '@storybook/addon-knobs';
import {
  constants,
  mappings,
  properties,
  sources,
  targets,
} from '../../stories/sampleData';

import React from 'react';
import { action } from '@storybook/addon-actions';
import decorators from '../../stories/decorators';

const obj = {
  title: 'AtlasMap|Views/ColumnMapperView',
  decorators,
};
export default obj;

export const sourcesColumn = () => (
  <FieldsDndProvider>
    <CanvasProvider>
      <Column data-testid={'column-source-area'} totalColumns={1}>
        <SourcesColumn
          onCaptureDocumentID={action('onCaptureDocumentID')}
          onChangeDocumentName={action('onChangeDocumentName')}
          onCreateConstant={action('onCreateConstant')}
          onEditConstant={action('onEditConstant')}
          onDeleteConstant={action('onDeleteConstant')}
          onCreateProperty={action('onCreateProperty')}
          onEditProperty={action('onEditProperty')}
          onDeleteProperty={action('onDeleteProperty')}
          onDeleteDocument={action('onDeleteDocument')}
          onImportDocument={action('onImportDocument')}
          onCustomClassSearch={action('onCustomClassSearch')}
          onShowMappingDetails={action('onShowMappingDetails')}
          canAddToSelectedMapping={() => true}
          canAddFieldToSelectedMapping={() => true}
          onAddToSelectedMapping={action('onAddToSelectedMapping')}
          canRemoveFromSelectedMapping={() => true}
          onRemoveFromSelectedMapping={action('onRemoveFromSelectedMapping')}
          canStartMapping={() => true}
          onStartMapping={action('onStartMapping')}
          shouldShowMappingPreviewForField={() => true}
          onFieldPreviewChange={action('onFieldPreviewChange')}
          canDrop={() => true}
          onDrop={action('onDrop')}
          onSearch={action('onSearch')}
          showTypes={boolean('Show types', true)}
          sourceProperties={properties}
          constants={constants}
          sources={sources}
          isSource={true}
          acceptDropType={'target'}
          draggableType={'source'}
          onEditCSVParams={action('onEditCSVParams')}
        />
      </Column>
    </CanvasProvider>
  </FieldsDndProvider>
);

export const targetsColumn = () => (
  <FieldsDndProvider>
    <CanvasProvider>
      <Column data-testid={'column-target-area'} totalColumns={1}>
        <TargetsColumn
          onCaptureDocumentID={action('onCaptureDocumentID')}
          onChangeDocumentName={action('onChangeDocumentName')}
          onCreateProperty={action('onCreateProperty')}
          onEditProperty={action('onEditProperty')}
          onDeleteProperty={action('onDeleteProperty')}
          onFieldPreviewChange={action('onFieldPreviewChange')}
          onDeleteDocument={action('onDeleteDocument')}
          onImportDocument={action('onImportDocument')}
          onSearch={action('onSearch')}
          onCustomClassSearch={action('onCustomClassSearch')}
          onDrop={action('onDrop')}
          canDrop={() => true}
          onShowMappingDetails={action('onShowMappingDetails')}
          canAddToSelectedMapping={() => true}
          canAddFieldToSelectedMapping={() => true}
          onAddToSelectedMapping={action('onAddToSelectedMapping')}
          canRemoveFromSelectedMapping={() => true}
          onRemoveFromSelectedMapping={action('onRemoveFromSelectedMapping')}
          canStartMapping={() => true}
          onStartMapping={action('onStartMapping')}
          shouldShowMappingPreviewForField={() => true}
          showMappingPreview={boolean('Show mapping preview', false)}
          showTypes={boolean('Show types', true)}
          targetProperties={properties}
          targets={targets}
          isSource={false}
          acceptDropType={'source'}
          draggableType={'target'}
          onEditCSVParams={action('onEditCSVParams')}
        />
      </Column>
    </CanvasProvider>
  </FieldsDndProvider>
);

export const sourceTargetLinks = () => (
  <CanvasProvider>
    <SourceTargetLinks
      mappings={mappings}
      selectedMappingId={text('Mapping ID', 'a')}
      onSelectMapping={action('onSelectMapping')}
    />
  </CanvasProvider>
);
