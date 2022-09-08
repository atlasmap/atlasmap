/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import { Form, FormGroup, TextInput } from '@patternfly/react-core';
import React, { FunctionComponent } from 'react';

import styles from './DocumentFieldPreview.module.css';
import { useDebouncedCallback } from 'use-debounce';

export interface IDocumentFieldPreviewProps {
  id: string;
  value: string;
  onChange: (value: string) => void;
}

export const DocumentFieldPreview: FunctionComponent<
  IDocumentFieldPreviewProps
> = ({ id, value, onChange }) => {
  const debouncedOnChange = useDebouncedCallback(onChange, 200);
  return (
    <Form className={styles.form} onClick={(event) => event.stopPropagation()}>
      <FormGroup label="Mapping preview" fieldId={id}>
        <TextInput
          id={id}
          type="text"
          onChange={debouncedOnChange}
          defaultValue={value || ''}
          aria-label="Type sample data here"
          placeholder="Type sample data here"
          data-testid={`input-document-mapping-preview-${id}-field`}
        />
      </FormGroup>
    </Form>
  );
};
