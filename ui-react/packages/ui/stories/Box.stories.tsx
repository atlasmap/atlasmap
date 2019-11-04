import React from 'react';
import { text } from "@storybook/addon-knobs";
import { Box } from '@src';

export default {
    title: 'Box',
}

export const interactiveExample = () => (
  <svg style={{ width: '100%', height: '100%'}}>
      <Box
        initialWidth={200}
        initialHeight={300}
        initialX={0}
        initialY={0}
      >
        {text('Sample content', 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia officiis quis reiciendis repellendus reprehenderit sapiente sint sunt totam vitae!')}
    </Box>
  </svg>
);
