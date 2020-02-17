import * as React from 'react';
import { Box } from '../src/CanvasView';
import { render } from './setup';

describe('Box tests', () => {
  test('should render', async () => {
    const { getByText } = render(<Box>test</Box>);
    getByText('test');
  });
});
