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
import { Form } from '@patternfly/react-core';
import { MappingTransformation } from './MappingTransformation';
import React from 'react';
import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';

const obj = {
  title: 'UI|Mapping Details/Transformation',
};
export default obj;

const transformationsOptions = [
  { name: 'Transformation foo', value: 'foo' },
  { name: 'Transformation bar', value: 'bar' },
];

const transformationsArguments = [
  {
    label: 'Argument foo',
    name: 'foo',
    value: 'foo',
  },
  {
    label: 'Argument bar',
    name: 'bar',
    value: 'bar',
  },
  {
    label: 'Delimiter',
    name: 'baz',
    value: 'baz',
    delimitersOptions: [
      {
        displayName: ',',
        delimiterValue: ',',
      },
      {
        displayName: '|',
        delimiterValue: '|',
      },
    ],
    delimiter: ',',
  },
];

export const example = () => (
  <MappingTransformation
    name={'Sample transformation'}
    transformationsOptions={transformationsOptions}
    transformationsArguments={transformationsArguments}
    onTransformationArgumentChange={action('onActionArgumentChange')}
    onTransformationChange={action('onActionChange')}
    onRemoveTransformation={action('onRemoveTransformation')}
    disableTransformation={boolean('Expression not enabled', false)}
  />
);

export const nonRemovable = () => (
  <MappingTransformation
    name={'Sample transformation'}
    transformationsOptions={transformationsOptions}
    transformationsArguments={transformationsArguments}
    onTransformationArgumentChange={action('onActionArgumentChange')}
    onTransformationChange={action('onActionChange')}
    disableTransformation={boolean('Expression not enabled', false)}
  />
);

export const stacked = () => (
  <Form>
    <MappingTransformation
      name={'Sample transformation'}
      transformationsOptions={transformationsOptions}
      transformationsArguments={transformationsArguments}
      onTransformationArgumentChange={action('onActionArgumentChange')}
      onTransformationChange={action('onActionChange')}
      onRemoveTransformation={action('onRemoveTransformation')}
      disableTransformation={boolean('Expression not enabled', false)}
    />
    <MappingTransformation
      name={'Sample transformation'}
      transformationsOptions={transformationsOptions}
      transformationsArguments={transformationsArguments}
      onTransformationArgumentChange={action('onActionArgumentChange')}
      onTransformationChange={action('onActionChange')}
      onRemoveTransformation={action('onRemoveTransformation')}
      disableTransformation={boolean('Expression not enabled', false)}
    />
    <MappingTransformation
      name={'Sample transformation'}
      transformationsOptions={transformationsOptions}
      transformationsArguments={transformationsArguments}
      onTransformationArgumentChange={action('onActionArgumentChange')}
      onTransformationChange={action('onActionChange')}
      onRemoveTransformation={action('onRemoveTransformation')}
      disableTransformation={boolean('Expression not enabled', false)}
    />
  </Form>
);
