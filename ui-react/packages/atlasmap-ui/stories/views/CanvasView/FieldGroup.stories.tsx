import React from 'react';
import { boolean } from '@storybook/addon-knobs';
import { CanvasLinksProvider, CanvasProvider } from '../../../src/canvas';
import { FieldGroup } from '../../../src/views/CanvasView';

export default {
  title: 'CanvasView/FieldGroup',
  component: FieldGroup,
};

export const interactiveExample = () => (
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
      <FieldGroup
        isVisible={true}
        lineConnectionSide={'right'}
        group={{
          fields: [
            {id: 'f1', name: 'f1'},
            {id: 'f2', name: 'f2'},
            {id: 'f3', name: 'f3'},
          ],
          id: 'text-id',
          name: 'Sample'
        }}
        getBoxRef={() => null}
        parentExpanded={boolean('Parent expanded', true)}
        renderNode={node => <>{node.name}</>}
      />
    </CanvasLinksProvider>
  </CanvasProvider>
);
