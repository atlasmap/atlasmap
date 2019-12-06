import { Form, FormGroup, TextInput } from '@patternfly/react-core';
import React, { FunctionComponent } from 'react';

export interface IDocumentFieldPreviewResultsProps {
  id: string;
  value: string;
}

export const DocumentFieldPreviewResults: FunctionComponent<IDocumentFieldPreviewResultsProps> = ({ id, value }) => {
  return (
    <Form style={{ marginTop: '0.5rem' }}>
      <FormGroup label='Preview results' helperText='Results will be displayed here' fieldId={id}>
        <TextInput
          id={id}
          type='text'
          value={value}
          aria-label='Mapping results preview'
        />
      </FormGroup>
    </Form>
  )
};
