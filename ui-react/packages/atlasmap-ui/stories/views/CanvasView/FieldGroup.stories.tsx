import React from 'react';
import { boolean, text } from '@storybook/addon-knobs';
import { FieldGroup } from '../../../src/views/CanvasView';

export default {
  title: 'Views/Source Target Mapper/Components/FieldGroup',
  component: FieldGroup,
};

export const interactiveExample = () => (
  <FieldGroup
    isVisible={true}
    documentType={'source'}
    lineConnectionSide={'right'}
    group={{
      fields: [],
      id: 'text-id',
      title: text('Group title', 'Sample title'),
    }}
    getBoxRef={() => null}
    parentExpanded={boolean('Parent expanded', true)}
  />
);
