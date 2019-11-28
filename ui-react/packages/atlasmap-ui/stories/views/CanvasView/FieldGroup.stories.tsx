import React from 'react';
import { boolean, text } from '@storybook/addon-knobs';
import { FieldGroup, FieldGroupList } from '../../../src/views/CanvasView';

export default {
  title: 'Views/Source Target Mapper/Components/FieldGroup',
  component: FieldGroup,
};

export const interactiveExample = () => (
  <FieldGroupList title={text('Title', 'Document title')}>
    {() => <FieldGroup
      isVisible={true}
      type={'source'}
      group={{
        fields: [],
        id: 'text-id',
        title: text('Group title', 'Sample title'),
      }}
      boxRef={null}
      parentExpanded={boolean('Parent expanded', true)}
    />}
  </FieldGroupList>
);
