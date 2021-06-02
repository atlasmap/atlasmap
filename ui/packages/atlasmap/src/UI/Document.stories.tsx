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
import { Button } from '@patternfly/react-core';
import { Document } from './Document';
import React from 'react';
import { text } from '@storybook/addon-knobs';

const obj = {
  title: 'Document',
  includeStories: [], // or don't load this file at all
};
export default obj;

export const document = () => (
  <div style={{ width: 300 }}>
    <Document
      title={text('Title', 'Some title that can be extra long')}
      footer={text('footer', 'Source document')}
      actions={[<Button key={'1'}>some action</Button>]}
    >
      Lorem ipsum dolor sit amet, consectetur adipisicing elit. Accusantium
      assumenda atque consequuntur cupiditate doloremque eius eligendi et, ex,
      harum impedit ipsum magnam minus, officia provident qui quidem veniam.
      Facere, quo.
    </Document>
  </div>
);

export const states = () => (
  <div>
    <Document
      style={{ width: 250, marginRight: 10, display: 'inline-block' }}
      title={'Selected'}
      selected={true}
    />
    <Document
      style={{ width: 250, marginRight: 10, display: 'inline-block' }}
      title={'DropTarget'}
      dropTarget={true}
    />
    <Document
      style={{ width: 250, marginRight: 10, display: 'inline-block' }}
      title={'DropAccepted'}
      dropAccepted={true}
    />
  </div>
);

export const noActions = () => (
  <div style={{ width: 300 }}>
    <Document title={'Lorem dolor'} footer={'Lorem dolor'}>
      Lorem ipsum dolor sit amet, consectetur adipisicing elit. Accusantium
      assumenda atque consequuntur cupiditate doloremque eius eligendi et, ex,
      harum impedit ipsum magnam minus, officia provident qui quidem veniam.
      Facere, quo.
    </Document>
  </div>
);

export const stacked = () => (
  <div style={{ width: 300 }}>
    <Document title={'Lorem dolor'} footer={'Lorem dolor'} stacked={true} />
    <Document title={'Lorem dolor'} footer={'Lorem dolor'} stacked={true} />
    <Document title={'Lorem dolor'} footer={'Lorem dolor'} stacked={true} />
  </div>
);
