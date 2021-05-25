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
import { Checkbox, Form, FormGroup, TextInput } from '@patternfly/react-core';
import {
  ConfirmationDialog,
  IConfirmationDialogProps,
} from './ConfirmationDialog';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useState,
} from 'react';

export interface INamespace {
  alias: string;
  uri: string;
  locationUri: string;
  targetNamespace: boolean;
}

export interface INamespaceDialogProps {
  title: string;
  alias?: string;
  uri?: string;
  locationUri?: string;
  targetNamespace?: boolean;
  isOpen: IConfirmationDialogProps['isOpen'];
  onCancel: IConfirmationDialogProps['onCancel'];
  onConfirm: (namespace: INamespace) => void;
}
export const NamespaceDialog: FunctionComponent<INamespaceDialogProps> = ({
  title,
  alias: initialAlias = '',
  uri: initialUri = '',
  locationUri: initialLocationUri = '',
  targetNamespace: initialTargetNamespace = false,
  isOpen,
  onCancel,
  onConfirm,
}) => {
  const [alias, setAlias] = useState(initialAlias);
  const [uri, setUri] = useState(initialUri);
  const [locationUri, setLocationUri] = useState(initialLocationUri);
  const [targetNamespace, setTargetNamespace] = useState(
    initialTargetNamespace,
  );

  const reset = useCallback(() => {
    setAlias(initialAlias);
    setUri(initialUri);
    setLocationUri(initialLocationUri);
    setTargetNamespace(initialTargetNamespace);
  }, [initialAlias, initialLocationUri, initialTargetNamespace, initialUri]);

  const handleOnConfirm = useCallback(() => {
    onConfirm({ alias, uri, locationUri, targetNamespace });
    reset();
  }, [alias, locationUri, onConfirm, reset, targetNamespace, uri]);

  const handleOnCancel = useCallback(() => {
    onCancel();
    reset();
  }, [onCancel, reset]);

  // make sure to resync the internal state to the values passed in as props
  useEffect(reset, [reset]);

  return (
    <ConfirmationDialog
      title={title}
      onCancel={handleOnCancel}
      onConfirm={alias.length > 0 ? handleOnConfirm : undefined}
      isOpen={isOpen}
    >
      <Form>
        <FormGroup label={'Alias'} fieldId={'alias'} isRequired={true}>
          <TextInput
            value={alias}
            onChange={setAlias}
            id={'alias'}
            autoFocus={true}
            isRequired={true}
          />
        </FormGroup>
        <FormGroup label={'URI'} fieldId={'uri'}>
          <TextInput value={uri} onChange={setUri} id={'uri'} />
        </FormGroup>
        <FormGroup label={'Location URI'} fieldId={'locationUri'}>
          <TextInput
            value={locationUri}
            onChange={setLocationUri}
            id={'locationUri'}
          />
        </FormGroup>
        <FormGroup fieldId={'targetNamespace'}>
          <Checkbox
            isChecked={targetNamespace}
            onChange={setTargetNamespace}
            id={'targetNamespace'}
            label={'Target namespace'}
          />
        </FormGroup>
      </Form>
    </ConfirmationDialog>
  );
};
