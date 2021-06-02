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
import { MappingTableView } from '../Views';
import React from 'react';
import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import decorators from '../stories/decorators';
import { mappings } from '../stories/sampleData';

const obj = {
  title: 'AtlasMap|Views',
  decorators,
};
export default obj;

export const mappingTableView = () => (
  <MappingTableView
    mappings={mappings}
    onSelectMapping={action('onSelectMapping')}
    shouldShowMappingPreview={() => boolean('shouldShowMappingPreview', false)}
    onFieldPreviewChange={action('onFieldPreviewChange')}
  />
);

export const mappingTableViewNoMappings = () => (
  <MappingTableView
    mappings={[]}
    onSelectMapping={action('onSelectMapping')}
    shouldShowMappingPreview={() => boolean('shouldShowMappingPreview', false)}
    onFieldPreviewChange={action('onFieldPreviewChange')}
  />
);
