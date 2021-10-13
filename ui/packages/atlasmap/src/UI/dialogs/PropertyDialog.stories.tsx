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
import { boolean, select, text } from '@storybook/addon-knobs';

import { PropertyDialog } from './PropertyDialog';
import React from 'react';
import { action } from '@storybook/addon-actions';

const obj = {
  title: 'UI|Dialogs',
  component: PropertyDialog,
};
export default obj;

const options = [
  { label: 'Foo', value: 'foo' },
  { label: 'Bar', value: 'bar' },
  { label: 'Baz', value: 'baz' },
];

const valueTypeOptions = options.map((o) => o.value);
const scopeOptions = options.map((o) => o.value);

export const sourcePropertyDialog = () => (
  <PropertyDialog
    title={text('Title', 'Property dialog title')}
    isOpen={boolean('Is open', true)}
    onCancel={action('onCancel')}
    onConfirm={action('onConfirm')}
    name={text('Initial name', '')}
    valueType={select(
      'Initial valueType',
      valueTypeOptions,
      valueTypeOptions[0],
    )}
    valueTypeOptions={options}
    scope={select('Initial scope', scopeOptions, scopeOptions[0])}
    scopeOptions={options}
    onValidation={() => boolean('Confirm', true)}
  />
);

export const targetPropertyDialog = () => (
  <PropertyDialog
    title={text('Title', 'Property dialog title')}
    isOpen={boolean('Is open', true)}
    onCancel={action('onCancel')}
    onConfirm={action('onConfirm')}
    name={text('Initial name', '')}
    valueType={select(
      'Initial valueType',
      valueTypeOptions,
      valueTypeOptions[0],
    )}
    valueTypeOptions={options}
    scope={select('Initial scope', scopeOptions, scopeOptions[0])}
    scopeOptions={options}
    onValidation={() => boolean('Confirm', true)}
  />
);
