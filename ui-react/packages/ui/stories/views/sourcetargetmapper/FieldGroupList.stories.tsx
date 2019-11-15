import React from 'react';
import { text } from '@storybook/addon-knobs';
import { FieldGroupList } from '../../../src/views/sourcetargetmapper';

export default {
  title: 'Views/Source Target Mapper/Components/FieldGroupList',
  component: FieldGroupList,
};

export const interactiveExample = () => (
  <FieldGroupList>
    {text(
      'Sample content',
      'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia officiis quis reiciendis repellendus reprehenderit sapiente sint sunt totam vitae!'
    )}
  </FieldGroupList>
);
