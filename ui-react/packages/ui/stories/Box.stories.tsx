import { text } from '@storybook/addon-knobs';
import * as React from 'react';
import { storiesOf } from '@storybook/react';
import { Box } from '@src';

const stories = storiesOf('Components', module);

stories.add('Box', () => (
  <Box>
    {text('Sample content', 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia officiis quis reiciendis repellendus reprehenderit sapiente sint sunt totam vitae!')}
  </Box>
));
