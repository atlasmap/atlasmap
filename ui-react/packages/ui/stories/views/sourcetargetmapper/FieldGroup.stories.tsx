import React from 'react';
import { text } from "@storybook/addon-knobs";
import { FieldGroup } from '../../../src/views/sourcetargetmapper/FieldGroup';
import { FieldGroupList } from '../../../src/views/sourcetargetmapper/FieldGroupList';

export default {
  title: 'Views/Source Target Mapper/Components/FieldGroup',
  component: FieldGroup
}

export const interactiveExample = () => (
  <FieldGroupList>
    <FieldGroup
      title={text('Group title', 'Sample title')}
      id={'text-id'}
    >
      {text('Sample content', 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia officiis quis reiciendis repellendus reprehenderit sapiente sint sunt totam vitae!')}
    </FieldGroup>
  </FieldGroupList>
);
