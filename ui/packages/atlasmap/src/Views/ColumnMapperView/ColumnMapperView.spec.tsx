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
import { fireEvent, render } from '@testing-library/react';
import { sourcesColumn, targetsColumn } from './ColumnMapperView.stories';
import { act } from 'react-dom/test-utils';

describe('SourcesColumn', () => {
  test('should render editable preview inputs', async () => {
    const { getByTestId, findByTestId } = render(sourcesColumn());
    await act(async () => {
      const previewInputProperty = getByTestId(
        'input-document-mapping-preview-boston-field',
      );
      expect(previewInputProperty.tagName).toBe('INPUT');
      expect(
        previewInputProperty.attributes.getNamedItem('disabled'),
      ).toBeFalsy();
      const previewInputConstant = getByTestId(
        'input-document-mapping-preview-Boston-field',
      );
      expect(previewInputConstant.tagName).toBe('INPUT');
      expect(
        previewInputConstant.attributes.getNamedItem('disabled'),
      ).toBeFalsy();
      const docButton = getByTestId(
        'sources-field-JSONInstanceSource-source-/order-toggle',
      );
      fireEvent.click(docButton);
      const previewInputField = await findByTestId(
        'input-document-mapping-preview-JSONInstanceSource-source-/order/orderId-field',
      );
      expect(previewInputField.tagName).toBe('INPUT');
      expect(previewInputField.attributes.getNamedItem('disabled')).toBeFalsy();
    });
  });
});

describe('TargetsColumn', () => {
  test('should render readonly preview result', async () => {
    const { getByTestId } = render(targetsColumn());
    await act(async () => {
      const previewInputProperty = getByTestId(
        'results-document-mapping-preview-boston-field',
      );
      expect(previewInputProperty.tagName).toBe('INPUT');
      expect(
        previewInputProperty.attributes.getNamedItem('disabled'),
      ).toBeTruthy();
      const previewInputField = getByTestId(
        'results-document-mapping-preview-io.paul.Bicycle-target-/cadence-field',
      );
      expect(previewInputField.tagName).toBe('INPUT');
      expect(
        previewInputField.attributes.getNamedItem('disabled'),
      ).toBeTruthy();
    });
  });
});
