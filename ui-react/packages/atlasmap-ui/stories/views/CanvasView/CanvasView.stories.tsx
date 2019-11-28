import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import React from 'react';
import { CanvasView } from '../../../src/views/CanvasView';
import { mappings, sources, targets } from '../../sampleData';

export default {
  title: 'Views/Source Target Mapper',
};

export const sample = () => (
  <CanvasView
    sources={sources}
    targets={targets}
    mappings={mappings}
    selectedMapping={text('Selected mapping', '')}
    selectMapping={action('selectMapping')}
    deselectMapping={action('deselectMapping')}
    editMapping={action('editMapping')}
    addToMapping={action('addToMapping')}
  />
);
