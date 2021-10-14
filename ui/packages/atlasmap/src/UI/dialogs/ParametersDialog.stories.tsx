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

import { boolean, text } from '@storybook/addon-knobs';
import { ParametersDialog } from './ParametersDialog';
import React from 'react';
import { action } from '@storybook/addon-actions';
import { getCsvParameterOptions } from '@atlasmap/core';

const obj = {
  title: 'UI|Dialogs',
  component: ParametersDialog,
};
export default obj;

const initialParameters = [
  {
    name: 'format',
    label: 'CSV File Format',
    value: 'Excel',
    options: [
      { label: 'Default', value: 'Default' },
      { label: 'Excel', value: 'Excel' },
      { label: 'InformixUnload', value: 'InformixUnload' },
      { label: 'InformixUnloadCsv', value: 'InformixUnloadCsv' },
      { label: 'MongoDBCsv', value: 'MongoDBCsv' },
      { label: 'MongoDBTsv', value: 'MongoDBTsv' },
      { label: 'MySQL', value: 'MySQL' },
      { label: 'Oracle', value: 'Oracle' },
      { label: 'PostgreSQLCsv', value: 'PostgreSQLCsv' },
      { label: 'PostgreSQLText', value: 'PostgreSQLText' },
      { label: 'RFC4180', value: 'RFC4180' },
      { label: 'TDF', value: 'TDF' },
    ],
    required: true,
    enabled: true,
  },
  {
    name: 'allowDuplicateHeaderNames',
    label: 'Allow Duplicate Header Names',
    value: 'true',
    boolean: true,
    required: false,
    enabled: true,
  },
  {
    name: 'allowMissingColumnNames',
    label: 'Allow Missing Column Names',
    value: 'true',
    boolean: true,
    required: false,
    enabled: true,
  },
  {
    name: 'commentMarker',
    label: 'Comment Marker',
    value: '#',
    required: false,
    enabled: true,
  },
  {
    name: 'delimiter',
    label: 'Delimiter',
    value: ':',
    required: false,
    enabled: true,
  },
  {
    name: 'escape',
    label: 'Escape',
    value: '<esc>',
    required: false,
    enabled: true,
  },
  {
    name: 'firstRecordAsHeader',
    label: 'First Record As Header',
    value: 'false',
    boolean: true,
    required: false,
    enabled: true,
  },
  { name: 'headers', label: 'Headers', value: 'hdr', required: false },
  {
    name: 'ignoreEmptyLines',
    label: 'Ignore Empty Lines',
    value: 'true',
    boolean: true,
    required: false,
    enabled: true,
  },
  {
    name: 'ignoreHeaderCase',
    label: 'Ignore Header Case',
    value: 'false',
    boolean: true,
    required: false,
    enabled: true,
  },
  {
    name: 'ignoreSurroundingSpaces',
    label: 'Ignore Surrounding Spaces',
    value: 'true',
    boolean: true,
    required: false,
    enabled: true,
  },
  {
    name: 'nullString',
    label: 'Null String',
    value: '<nul>',
    required: false,
    enabled: true,
  },
  { name: 'quote', label: 'Quote', value: '', required: false, enabled: false },
];

const parameters = getCsvParameterOptions();
/**
 * The 'Select' parameters dialog doesn't specify predefined parameters, the
 * 'Edit' dialog does.
 *
 * @returns
 */
export const parametersSelectDialog = () => (
  <ParametersDialog
    title={text('Title', 'Select CSV Processing Parameters')}
    isOpen={boolean('Is open', true)}
    onCancel={action('onCancel')}
    onConfirm={action('onConfirm')}
    parameters={parameters}
    initialParameters={[]}
  />
);

export const parametersEditDialog = () => (
  <ParametersDialog
    title={text('Title', 'Edit CSV Processing Parameters')}
    isOpen={boolean('Is open', true)}
    onCancel={action('onCancel')}
    onConfirm={action('onConfirm')}
    parameters={parameters}
    initialParameters={initialParameters}
  />
);
