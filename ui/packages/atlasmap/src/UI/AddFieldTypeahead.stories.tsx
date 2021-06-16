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
import {
  AddFieldTypeahead,
  IAddFieldTypeaheadField,
} from './AddFieldTypeahead';

import React from 'react';
import { action } from '@storybook/addon-actions';

const obj = {
  title: 'UI|Mapping Details/Add field typeahead',
};
export default obj;

const onAddAction = action('onAdd');

const fields: IAddFieldTypeaheadField[] = [
  { group: 'Foo group', label: 'Aaa', onAdd: onAddAction },
  { group: 'Foo group', label: 'Bbbb', onAdd: onAddAction },
  { group: 'Foo group', label: 'Cccc', onAdd: onAddAction },
  { group: 'Bar group', label: 'Aaa', onAdd: onAddAction },
  { group: 'Bar group', label: 'Bbbb', onAdd: onAddAction },
  { group: 'Bar group', label: 'Cccc', onAdd: onAddAction },
  { group: 'Baz group', label: 'Aaa', onAdd: onAddAction },
  { group: 'Baz group', label: 'Bbbb', onAdd: onAddAction },
  { group: 'Baz group', label: 'Cccc', onAdd: onAddAction },
  {
    group: 'Stretch group',
    label:
      'Lorem ipsum dolor sit amet consectetur adipisicing elit. Reprehenderit expedita animi facere optio sit eaque, aut numquam, asperiores ratione earum natus iste in. Nemo cupiditate praesentium, amet accusamus impedit iure.',
    onAdd: onAddAction,
  },
  {
    group: 'Stretch group',
    label:
      'Lorem ipsum dolor sit amet consectetur adipisicing elit. Reprehenderit expedita animi facere optio sit eaque, aut numquam, asperiores ratione earum natus iste in. Nemo cupiditate praesentium, amet accusamus impedit iure.',
    onAdd: onAddAction,
  },
  {
    group: 'Stretch group',
    label:
      'Lorem ipsum dolor sit amet consectetur adipisicing elit. Reprehenderit expedita animi facere optio sit eaque, aut numquam, asperiores ratione earum natus iste in. Nemo cupiditate praesentium, amet accusamus impedit iure.',
    onAdd: onAddAction,
  },
];

export const example = () => (
  <AddFieldTypeahead
    ariaLabelTypeAhead={'example'}
    fields={fields}
    placeholderText={'Placeholder'}
    isSource={true}
  />
);
