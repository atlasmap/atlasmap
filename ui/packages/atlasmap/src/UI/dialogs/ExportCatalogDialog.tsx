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
import {
  ConfirmationDialog,
  IConfirmationDialogProps,
} from './ConfirmationDialog';
import {
  Form,
  FormGroup,
  InputGroup,
  InputGroupText,
  TextInput,
} from '@patternfly/react-core';
import React, { FunctionComponent, useState } from 'react';

export interface IExportCatalogDialogProps {
  isOpen: IConfirmationDialogProps['isOpen'];
  onCancel: IConfirmationDialogProps['onCancel'];
  onConfirm: (filename: string) => void;
}
export const ExportCatalogDialog: FunctionComponent<
  IExportCatalogDialogProps
> = ({ isOpen, onCancel, onConfirm }) => {
  const defaultCatalogName = 'atlasmap-mapping';
  const [filename, setFilename] = useState(defaultCatalogName);
  const handleOnConfirm =
    filename.length > 0 ? () => onConfirm(filename) : undefined;
  return (
    <ConfirmationDialog
      title={'Export Mappings and Documents.'}
      onCancel={onCancel}
      onConfirm={handleOnConfirm}
      isOpen={isOpen}
      dataTestid={'export-catalog-dialog'}
    >
      <Form>
        <FormGroup
          label={'Please enter a name for your exported catalog file'}
          fieldId={'filename'}
          isRequired={true}
        >
          <InputGroup>
            <TextInput
              value={filename}
              onChange={setFilename}
              id={'filename'}
              isRequired={true}
              autoFocus={true}
              data-testid={'export-catalog-dialog-text-input'}
            />
            <InputGroupText>.adm</InputGroupText>
          </InputGroup>
        </FormGroup>
      </Form>
    </ConfirmationDialog>
  );
};
