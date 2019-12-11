import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import React from 'react';
import { CanvasLinksProvider, CanvasProvider } from '../../../src/canvas';
import { Document } from '../../../src/views/CanvasView/components';
import { sources } from '../../sampleData';

const s = sources[0];

export default {
  title: 'CanvasView/Document',
};

export const interactive = () => (
  <CanvasProvider
    width={200}
    height={200}
    zoom={1}
    offsetTop={0}
    offsetLeft={0}
    panX={0}
    panY={0}
  >
    <CanvasLinksProvider>
      <Document
        key={s.id}
        title={s.name}
        footer={text('footer', 'Source document')}
        lineConnectionSide={'right'}
        fields={s}
        renderGroup={node => node.id}
        renderNode={node => node.id}
        onDelete={action('onDelete Source')}
      />
    </CanvasLinksProvider>
  </CanvasProvider>
);