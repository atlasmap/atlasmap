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
import React from 'react';
import { Sidebar } from './Sidebar';
import { boolean } from '@storybook/addon-knobs';

const obj = {
  title: 'AtlasMap|Layout/Sidebar',
};
export default obj;

export const example = () => (
  <div style={{ minHeight: 300 }}>
    <Sidebar show={boolean('Show sidebar', true)}>
      {() => (
        <div>
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Dignissimos
          incidunt, fugiat sequi obcaecati sapiente debitis fuga perspiciatis
          possimus minima recusandae dolor minus unde nesciunt in aspernatur
          accusantium laborum sit cumque?
        </div>
      )}
    </Sidebar>
  </div>
);
