import React from 'react';
import { text } from "@storybook/addon-knobs";
import { Box } from '@src';

export default {
    title: 'Box',
}

export const interactiveExample = () => (
  <Box>
    {text('Sample content', 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia officiis quis reiciendis repellendus reprehenderit sapiente sint sunt totam vitae!')}
  </Box>
);
