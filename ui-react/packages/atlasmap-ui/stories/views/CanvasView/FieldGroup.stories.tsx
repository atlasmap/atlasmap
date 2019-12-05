import React from 'react';
import { boolean } from '@storybook/addon-knobs';
import { FieldGroup } from '../../../src/views/CanvasView';

export default {
  title: 'Views/Source Target Mapper/Components/FieldGroup',
  component: FieldGroup,
};

export const interactiveExample = () => (
  <FieldGroup
    isVisible={true}
    lineConnectionSide={'right'}
    group={{
      fields: [],
      id: 'text-id'
    }}
    getBoxRef={() => null}
    parentExpanded={boolean('Parent expanded', true)}
    renderNode={_ => <>test</>}
  />
);
