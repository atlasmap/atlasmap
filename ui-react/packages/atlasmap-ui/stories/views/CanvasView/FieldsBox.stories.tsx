import { boolean, number, text } from '@storybook/addon-knobs';
import React from 'react';
import { CanvasProvider } from '../../../src/canvas';
import { CanvasView, CanvasViewCanvasProvider, FieldsBox } from '../../../src/views/CanvasView';

export default {
  title: 'CanvasView/FieldsBox',
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
    <CanvasViewCanvasProvider>
      <CanvasView>
        <FieldsBox
          id={'sources'}
          initialWidth={number('Width', 200)}
          initialHeight={number('Height', 300)}
          position={{ x: number('X', 10), y: number('Y', 10)}}
          header={text('Header', 'Sample header')}
          hidden={boolean('Hidden', false)}
        >
          {text('Children', 'lorem dolor')}
        </FieldsBox>
        </CanvasView>
    </CanvasViewCanvasProvider>
  </CanvasProvider>
);
