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
import * as React from 'react';

import { Column } from './Column';
import { example } from './Column.stories';
import { render } from '@testing-library/react';

describe('Column tests', () => {
  test('should render', async () => {
    const { getByText } = render(example());
    getByText('I can scroll.');
  });

  /* the following tests are not really meant to be unit tests since checking
   * the actual rendering of the DOM is better handled in a real browser, like
   * Puppeteer. But I don't like Jest's snapshots, so...
   * */

  test('should respect the parent size', async () => {
    const { getByTestId } = render(
      <Column totalColumns={2} data-testid={'column'}>
        test
      </Column>,
    );
    expect(getByTestId('column').style).toHaveProperty('flex', '0 0 50%');
  });

  test('should respect the visible prop=false', async () => {
    const { getByTestId } = render(
      <Column visible={false} data-testid={'column'}>
        test
      </Column>,
    );
    expect(getByTestId('column').className).toBe('column hidden');
  });

  test('should respect the visible prop=true', async () => {
    const { getByTestId } = render(
      <Column visible={true} data-testid={'column'}>
        test
      </Column>,
    );
    expect(getByTestId('column').className).toBe('column');
  });
});
