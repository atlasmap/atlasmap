import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import React from 'react';
import {
  CanvasObject,
} from '../../../src/Canvas';
import { BoxProvider, Document } from '../../../src/CanvasView/components';
import { sources } from '../../sampleData';
import { CanvasView, CanvasViewProvider } from '../../../src/CanvasView';

const s = sources[0];

export default {
  title: 'CanvasView/Document',
};

export const interactive = () => (
  <CanvasViewProvider>
    <CanvasView>
      <BoxProvider getScrollableAreaRef={() => null}>
        <CanvasObject height={300} width={200} id={'id'} x={10} y={10}>
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
        </CanvasObject>
      </BoxProvider>
    </CanvasView>
  </CanvasViewProvider>
);
