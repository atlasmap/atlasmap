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
import { Column, ColumnBody, SearchableColumnHeader } from '.';
import { boolean, number } from '@storybook/addon-knobs';

import React from 'react';
import { action } from '@storybook/addon-actions';

const obj = {
  title: 'ColumnMapper',
  component: Column,
  includeStories: [], // or don't load this file at all
};
export default obj;

export const example = () => (
  <Column
    totalColumns={number('Total number of columns', 1)}
    visible={boolean('Is visible', true)}
  >
    <SearchableColumnHeader title={'Header'} onSearch={action('onSearch')} />
    <ColumnBody>
      <p>I can scroll.</p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>

      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>

      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>

      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>

      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
    </ColumnBody>
  </Column>
);
