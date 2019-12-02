import React from 'react';
import { text } from '@storybook/addon-knobs';
import { Document } from '../../../src/views/CanvasView';

export default {
  title: 'Views/Source Target Mapper/Components/FieldGroupList',
  component: Document,
};

export const interactiveExample = () => (
  <Document
    title={text('Title', 'Document title')}
    footer={text('Footer', 'Document footer')}
  >
    {() => (
      <>
        {text(
          'Sample content',
          'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia officiis quis reiciendis repellendus reprehenderit sapiente sint sunt totam vitae!'
        )}
      </>
    )}
  </Document>
);
