import React from 'react';
import { text } from '@storybook/addon-knobs';
import { FieldGroup, FieldGroupList } from '../../../src/views/sourcetargetmapper';

export default {
  title: 'Views/Source Target Mapper/Components/FieldGroup',
  component: FieldGroup,
};

export const interactiveExample = () => (
  <FieldGroupList>
    <FieldGroup
      isVisible={true}
      type={'source'}
      group={{
        fields: [],
        id: 'text-id',
        title: text('Group title', 'Sample title'),
      }}
    />
  </FieldGroupList>
);
