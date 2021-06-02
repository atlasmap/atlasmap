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
import { ColumnHeader } from '.';
import React from 'react';

const obj = {
  title: 'ColumnMapper',
  component: ColumnHeader,
  includeStories: [], // or don't load this file at all
};
export default obj;

export const example = () => (
  <div style={{ width: 300, minHeight: 300 }}>
    <ColumnHeader
      title={'Source'}
      actions={[
        <div key={'1'}>
          <button>#1</button>
        </div>,
        <div key={'2'}>
          <button>#2</button>
        </div>,
        <div key={'1'}>
          <button>#1</button>
        </div>,
      ]}
    />
  </div>
);
