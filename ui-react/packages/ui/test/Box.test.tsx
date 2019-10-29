import * as React from 'react';
import { render } from '@test/setup';
import { Box } from '@src';

describe('Box tests', () => {
  test('should rende a todo', async () => {
    const { getByText } = render(<Box />);
    getByText('todo')
  })
})
