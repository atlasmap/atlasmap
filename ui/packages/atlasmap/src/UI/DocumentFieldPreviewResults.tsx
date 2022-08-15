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
import styles from './DocumentFieldPreviewResults.module.css';

export interface IDocumentFieldPreviewResultsProps {
  id: string;
  value: string;
}

export const DocumentFieldPreviewResults: FunctionComponent<
  IDocumentFieldPreviewResultsProps
> = ({ id, value }) => {
  return (
    <Form className={styles.form}>
      <FormGroup label="Preview results" fieldId={id}>
        <TextInput
          id={id}
          type="text"
          value={value === undefined ? '' : value}
          aria-label="Mapping results preview"
          placeholder="Results will be displayed here"
          isDisabled={true}
          data-testid={`results-document-mapping-preview-${id}-field`}
        />
      </FormGroup>
    </Form>
  );
};
