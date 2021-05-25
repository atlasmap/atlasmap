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
  FormSelect,
  FormSelectOption,
} from '@patternfly/react-core';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useState,
} from 'react';

interface ValueTypeOption {
  label: string;
  value: string;
}

export interface ICustomClass {
  customClassName: string;
  collectionType: string;
}

export interface ICustomClassDialogProps {
  title: string;
  customClassName: string;
  customClassNames: string[] | null;
  collectionType?: string;
  collectionTypeOptions: ValueTypeOption[];
  isOpen: IConfirmationDialogProps['isOpen'];
  onCancel: IConfirmationDialogProps['onCancel'];
  onConfirm: (constant: ICustomClass) => void;
}
export const CustomClassDialog: FunctionComponent<ICustomClassDialogProps> = ({
  title,
  customClassName: initialCustomClassName = '',
  customClassNames,
  collectionType: initialCollectionType = '',
  collectionTypeOptions,
  isOpen,
  onCancel,
  onConfirm,
}) => {
  const [customClassName, setCustomClassName] = useState(
    initialCustomClassName,
  );
  const [collectionType, setCollectionType] = useState(initialCollectionType);

  const reset = useCallback(() => {
    setCustomClassName(initialCustomClassName);
    setCollectionType(initialCollectionType);
  }, [initialCustomClassName, initialCollectionType]);

  const handleOnConfirm = useCallback(() => {
    onConfirm({
      customClassName: customClassName,
      collectionType: collectionType,
    });
    reset();
  }, [onConfirm, reset, customClassName, collectionType]);

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
      onConfirm={customClassName.length > 0 ? handleOnConfirm : undefined}
      isOpen={isOpen}
    >
      <Form>
        <FormGroup
          label={'Custom class name'}
          fieldId={'custom-class-name'}
          isRequired={true}
        >
          <FormSelect
            value={customClassName}
            aria-label={'Select class name'}
            autoFocus={true}
            onChange={setCustomClassName}
            data-testid={'custom-class-name-form-select'}
          >
            {isOpen &&
              customClassNames &&
              customClassNames.map((value, idx) => (
                <FormSelectOption key={idx} value={value} label={value} />
              ))}
          </FormSelect>
        </FormGroup>
        <FormGroup label={'Collection type'} fieldId={'valueType'}>
          <FormSelect
            value={collectionType}
            aria-label={'Select value type'}
            onChange={setCollectionType}
            data-testid={'collection-type-form-select'}
          >
            {collectionTypeOptions.map(({ label, value }, idx) => (
              <FormSelectOption key={idx} value={value} label={label} />
            ))}
          </FormSelect>
        </FormGroup>
      </Form>
    </ConfirmationDialog>
  );
};
