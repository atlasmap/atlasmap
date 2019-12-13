import { Form, FormGroup, TextInput } from '@patternfly/react-core';
import React, { FunctionComponent } from 'react';

export interface IDocumentFieldPreviewProps {
  id: string;
  onChange: (value: string) => void;
}

export const DocumentFieldPreview: FunctionComponent<
  IDocumentFieldPreviewProps
> = ({ id, onChange }) => {
  return (
    <Form style={{ marginTop: '0.5rem' }}>
      <FormGroup
        label="Mapping preview"
        helperText="Type sample data here"
        fieldId={id}
      >
        <TextInput
          id={id}
          type="text"
          onChange={onChange}
          aria-label="Type sample data here"
        />
      </FormGroup>
    </Form>
  );
};
