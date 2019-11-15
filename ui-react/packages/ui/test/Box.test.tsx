import * as React from 'react';
import { Box } from '../src/views/sourcetargetmapper';
import { render } from './setup';

describe('Box tests', () => {
  test('should rende a todo', async () => {
    const { getByText } = render(<Box />);
    getByText('todo');
  });
});
