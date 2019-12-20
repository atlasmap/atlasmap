import { boolean, number, text } from '@storybook/addon-knobs';
import React from 'react';
import { CanvasObject } from '../../../src/Canvas';
import {
  BoxProvider,
  CanvasView,
  CanvasViewProvider,
  FieldsBox,
} from '../../../src/CanvasView';

export default {
  title: 'CanvasView/FieldsBox',
};

export const interactive = () => (
  <CanvasViewProvider>
    <CanvasView>
      <BoxProvider getScrollableAreaRef={() => null}>
        <CanvasObject height={300} width={200} id={'id'} x={10} y={10}>
          <FieldsBox
            id={'sources'}
            initialWidth={number('Width', 200)}
            initialHeight={number('Height', 300)}
            position={{ x: number('X', 10), y: number('Y', 10) }}
            header={text('Header', 'Sample header')}
            visible={boolean('Hidden', false)}
          >
            {text('Children', 'lorem dolor')}
          </FieldsBox>
        </CanvasObject>
      </BoxProvider>
    </CanvasView>
  </CanvasViewProvider>
);
