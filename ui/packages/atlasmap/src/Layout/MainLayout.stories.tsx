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
import { MainLayout } from './MainLayout';
import React from 'react';
import { boolean } from '@storybook/addon-knobs';

const obj = {
  title: 'AtlasMap|Layout/MainLayout',
};
export default obj;

export const example = () => (
  <MainLayout
    loading={boolean('loading', false)}
    contextToolbar={<div>context toolbar</div>}
    expressionToolbar={<div>view toolbar</div>}
    controlBar={<div>canvas bar</div>}
    showSidebar={boolean('showSidebar', true)}
    renderSidebar={() => <div>a sidebar</div>}
  >
    <div style={{ minHeight: 300 }}>
      Lorem ipsum dolor sit amet consectetur, adipisicing elit. Molestias
      tenetur veritatis dolore perferendis dicta excepturi illum necessitatibus
      eos accusantium ipsum. Aliquid a doloribus libero nemo et veniam quaerat
      nesciunt repudiandae!
    </div>
  </MainLayout>
);
