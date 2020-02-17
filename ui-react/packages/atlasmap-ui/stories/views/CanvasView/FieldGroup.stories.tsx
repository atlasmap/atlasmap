import React from 'react';
import { boolean } from '@storybook/addon-knobs';
import {
  BoxProvider,
  CanvasView,
  CanvasViewProvider,
  FieldGroup,
} from '../../../src/CanvasView';
import { CanvasObject } from '../../../src/Canvas';

export default {
  title: 'CanvasView/FieldGroup',
  component: FieldGroup,
};

export const interactiveExample = () => (
  <CanvasViewProvider>
    <CanvasView>
      <BoxProvider getScrollableAreaRef={() => null}>
        <CanvasObject height={300} width={200} id={'id'} x={10} y={10}>
          <FieldGroup
            isVisible={true}
            lineConnectionSide={'right'}
            group={{
              fields: [{ id: 'f1' }, { id: 'f2' }, { id: 'f3' }],
              id: 'text-id',
              isCollection: false,
            }}
            parentExpanded={boolean('Parent expanded', true)}
            renderGroup={node => node.id}
            renderNode={node => node.id}
          />
        </CanvasObject>
      </BoxProvider>
    </CanvasView>
  </CanvasViewProvider>
);
