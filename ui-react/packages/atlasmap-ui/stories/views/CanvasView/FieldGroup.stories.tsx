import React from 'react';
import { text } from '@storybook/addon-knobs';
import { FieldGroup, FieldGroupList } from '../../../src/views/CanvasView';

export default {
  title: 'Views/Source Target Mapper/Components/FieldGroup',
  component: FieldGroup,
};

export const interactiveExample = () => (
  <FieldGroupList>
    {() => <FieldGroup
      isVisible={true}
      type={'source'}
      group={{
        fields: [],
        id: 'text-id',
        title: text('Group title', 'Sample title'),
      }}
      boxRef={null}
    />}
  </FieldGroupList>
);
