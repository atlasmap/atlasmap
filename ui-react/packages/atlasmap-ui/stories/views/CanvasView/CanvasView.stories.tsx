import { action } from '@storybook/addon-actions';
import { number } from '@storybook/addon-knobs';
import React from 'react';
import { CanvasView, CanvasViewProvider } from '../../../src/CanvasView';

export default {
  title: 'CanvasView/CanvasView',
};

export const interactive = () => (
  <CanvasViewProvider>
    <CanvasView onSelection={action('onSelection')}>
      <circle cx={number('cx', 50)} cy={number('cy', 50)} r={number('r', 20)} />
    </CanvasView>
  </CanvasViewProvider>
);
