import React from 'react';
import { boolean, text } from '@storybook/addon-knobs';
import { FieldGroup, Document } from '../../../src/views/CanvasView';

export default {
  title: 'Views/Source Target Mapper/Components/FieldGroup',
  component: FieldGroup,
};

export const interactiveExample = () => (
  <Document
    title={text('Title', 'Document title')}
    footer={text('Footer', 'Document footer')}
  >
    {({ getRef }) => <FieldGroup
      isVisible={true}
      type={'source'}
      group={{
        fields: [],
        id: 'text-id',
        title: text('Group title', 'Sample title'),
      }}
      getBoxRef={getRef}
      parentExpanded={boolean('Parent expanded', true)}
    />}
  </Document>
);
