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

import { CustomClassDialog } from './CustomClassDialog';
import React from 'react';
import { action } from '@storybook/addon-actions';

const obj = {
  title: 'UI|Dialogs',
  component: CustomClassDialog,
};
export default obj;

const options = [
  { label: 'Foo', value: 'foo' },
  { label: 'Bar', value: 'bar' },
  { label: 'Baz', value: 'baz' },
];

const collectionTypeOptions = options.map((o) => o.value);

export const customClassDialog = () => (
  <CustomClassDialog
    title={text('Title', 'CustomClass dialog title')}
    isOpen={boolean('Is open', true)}
    onCancel={action('onCancel')}
    onConfirm={action('onConfirm')}
    customClassName={text('Initial name', '')}
    customClassNames={['className1', 'className2', 'className3']}
    collectionType={select(
      'Initial collectionType',
      collectionTypeOptions,
      collectionTypeOptions[0],
    )}
    collectionTypeOptions={options}
  />
);
