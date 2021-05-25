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
import { SearchableColumnHeader } from './SearchableColumnHeader';
import { example } from './SearchableColumnHeader.stories';
import { render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

describe('SearchableColumnHeader tests', () => {
  test('should render', async () => {
    const { getByText } = render(example());
    getByText('Source');
  });

  test('change events are propagated when typing', async () => {
    const searchInputLabel = 'Search fields';
    const onSearchSpy = jest.fn();
    const { findByLabelText } = render(
      <SearchableColumnHeader title={'Source'} onSearch={onSearchSpy} />,
    );

    const inputField = await findByLabelText(searchInputLabel);

    await userEvent.type(inputField, 'ABC');

    // the onChange event should be triggered for every keystoke
    expect(onSearchSpy).toHaveBeenCalledTimes(3);
  });
});
