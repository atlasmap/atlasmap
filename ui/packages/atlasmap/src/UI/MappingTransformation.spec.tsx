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
  example,
  nonRemovable,
  stacked,
} from './MappingTransformation.stories';
import { act } from 'react-dom/test-utils';
import { render } from '@testing-library/react';

describe('MappingTransformation', () => {
  test('should render example', async () => {
    const { getByTestId } = render(example());
    await act(async () => {
      expect(
        getByTestId(`user-field-action-Sample transformation`).tagName,
      ).toBe('SELECT');
      const checkbox = getByTestId(
        `user-field-action-Sample transformation-transformation-3-checkbox`,
      );
      expect(checkbox.tagName).toBe('INPUT');
      expect(checkbox.getAttribute('type')).toBe('checkbox');
    });
  });

  test('should render nonRemovable', async () => {
    const { getByTestId } = render(nonRemovable());
    await act(async () => {
      expect(
        getByTestId(`user-field-action-Sample transformation`).tagName,
      ).toBe('SELECT');
      const checkbox = getByTestId(
        `user-field-action-Sample transformation-transformation-3-checkbox`,
      );
      expect(checkbox.tagName).toBe('INPUT');
      expect(checkbox.getAttribute('type')).toBe('checkbox');
    });
  });

  test('should render stacked', async () => {
    const { getAllByTestId } = render(stacked());
    await act(async () => {
      getAllByTestId(`user-field-action-Sample transformation`).forEach(
        (element) => expect(element.tagName).toBe('SELECT'),
      );
      getAllByTestId(
        `user-field-action-Sample transformation-transformation-3-checkbox`,
      ).forEach((element) => {
        expect(element.tagName).toBe('INPUT');
        expect(element.getAttribute('type')).toBe('checkbox');
      });
    });
  });
});
