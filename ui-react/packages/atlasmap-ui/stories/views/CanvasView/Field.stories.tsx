import React from 'react';
import { text } from '@storybook/addon-knobs';
import { Field } from '../../../src/views/CanvasView';

export default {
  title: 'Views/Source Target Mapper/Components/Field',
  component: Field,
};

export const interactiveExample = () => (
  <Field>
    {text(
      'Sample content',
      'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia officiis quis reiciendis repellendus reprehenderit sapiente sint sunt totam vitae!'
    )}
  </Field>
);
