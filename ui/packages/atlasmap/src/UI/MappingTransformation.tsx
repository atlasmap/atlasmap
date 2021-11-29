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
  Button,
  Checkbox,
  FormGroup,
  FormSelect,
  FormSelectOption,
  InputGroup,
  Select,
  SelectOption,
  SelectOptionObject,
  SelectVariant,
  Split,
  SplitItem,
  TextInput,
} from '@patternfly/react-core';
import React, { FunctionComponent, useState } from 'react';

import { FieldType } from '@atlasmap/core';
import { TrashIcon } from '@patternfly/react-icons';
import { css } from '@patternfly/react-styles';
import styles from './MappingTransformation.module.css';

export interface ITransformationSelectOption {
  name: string;
  value: string;
}

export interface ITransformationArgument {
  label: string;
  name: string;
  value: string;
  type?: FieldType;
  options?: ITransformationSelectOption[];
}

export interface IMappingTransformationProps {
  name: string;
  transformationsOptions: ITransformationSelectOption[];
  transformationsArguments?: ITransformationArgument[];
  disableTransformation: boolean;
  onTransformationArgumentChange: (name: string, value: string) => void;
  onTransformationChange: (value: string) => void;
  onRemoveTransformation?: () => void;
  noPaddings?: boolean;
}

const formTransStyle = {
  '--pf-c-form-control--FontSize': 'small',
  '--pf-c-form--GridGap': '0',
  marginTop: '0.4rem',
} as React.CSSProperties;

const formTransGroupStyle = {
  '--pf-c-form-control--FontSize': 'small',
  marginTop: '1.0rem',
} as React.CSSProperties;

const formTransTextInputStyle = {
  '--pf-c-form-control--FontSize': 'small',
  marginTop: '0rem',
} as React.CSSProperties;

export const MappingTransformation: FunctionComponent<
  IMappingTransformationProps
> = ({
  name,
  transformationsOptions,
  transformationsArguments = [],
  disableTransformation,
  onTransformationArgumentChange,
  onTransformationChange,
  onRemoveTransformation,
}) => {
  const RenderTransformationArgumentOptions = (
    argId: string,
    arg: ITransformationArgument,
    disableTransformation: boolean,
    onTransformationArgumentChange: (name: string, value: string) => void,
  ) => {
    /** If the option name is 'User defined', we assume it accepts a user input */
    const isUserDefined = (option?: ITransformationSelectOption) => {
      return option?.name === 'User defined';
    };

    const userDefinedOption = arg.options?.find((option) =>
      isUserDefined(option),
    );

    let initialSelected = arg.options?.find(
      (option) => option.value === arg.value,
    );
    if (!initialSelected) {
      if (arg.value) {
        initialSelected = userDefinedOption;
      } else if (arg.options) {
        initialSelected = arg.options[1];
        arg.value = arg.options[1].value;
      }
    }
    let initialUserDefined = '';
    if (isUserDefined(initialSelected)) {
      initialUserDefined = arg.value;
    }

    const [isOpen, setIsOpen] = useState(false);
    const [selected, setSelected] = useState<
      ITransformationSelectOption | undefined
    >(initialSelected);
    const [userDefinedValue, setUserDefinedValue] =
      useState<string>(initialUserDefined);

    const onToggle = (isOpen: boolean) => {
      setIsOpen(isOpen);
    };

    const onSelect = (
      _event:
        | React.MouseEvent<Element, MouseEvent>
        | React.ChangeEvent<Element>,
      selection: string | SelectOptionObject,
      _isPlaceholder?: boolean,
    ) => {
      setIsOpen(false);
      const selectedOption = arg.options!.find(
        (option) => option.value === selection,
      );
      if (!selectedOption) {
        return;
      }
      setSelected(selectedOption);
      arg.value = isUserDefined(selectedOption)
        ? userDefinedValue
          ? userDefinedValue
          : ''
        : selection.toString();
      onTransformationArgumentChange(arg.name, arg.value);
    };

    const onChangeUserDefinedValue = (value: string) => {
      setUserDefinedValue(value);
      arg.value = value;
      onTransformationArgumentChange(arg.name, value);
    };

    return (
      <Split>
        <SplitItem>
          <Select
            variant={SelectVariant.single}
            aria-label={arg.label}
            onSelect={onSelect}
            selections={selected?.value}
            onToggle={onToggle}
            isOpen={isOpen}
            placeholderText={'[None]'}
            id={argId}
            isDisabled={disableTransformation}
            data-testid={arg.name}
            style={formTransStyle}
          >
            {arg.options?.map((option, optIndx) => {
              return (
                <SelectOption value={option.value} key={optIndx}>
                  {option.name}
                </SelectOption>
              );
            })}
          </Select>
        </SplitItem>
        {isUserDefined(selected) && (
          <SplitItem>
            <TextInput
              id="userDefined"
              type="text"
              name="userDefined"
              value={userDefinedValue}
              onChange={onChangeUserDefinedValue}
              style={formTransTextInputStyle}
              data-testid={`userDefined`}
              autoFocus
            />
          </SplitItem>
        )}
      </Split>
    );
  };

  const renderTransformationArgumentText = (
    argId: string,
    a: ITransformationArgument,
  ) => {
    return (
      <TextInput
        id={argId}
        type="text"
        name={a.name}
        isDisabled={disableTransformation}
        value={a.value}
        onChange={(value) => onTransformationArgumentChange(a.name, value)}
        data-testid={`insert-transformation-parameter-${a.name}-input-field`}
        style={formTransTextInputStyle}
      />
    );
  };

  const renderTransformationArgumentBoolean = (
    argId: string,
    a: ITransformationArgument,
  ) => {
    return (
      <Checkbox
        className={css(styles.transArgs)}
        id={argId}
        data-testid={argId + '-checkbox'}
        key={argId}
        label={a.label}
        aria-label={a.label}
        isChecked={a.value === 'true'}
        onChange={(value) =>
          onTransformationArgumentChange(a.name, value.toString())
        }
      />
    );
  };

  const renderTransformationArgument = (
    a: ITransformationArgument,
    idx: number,
  ) => {
    const argId = `${id}-transformation-${idx}`;
    switch (a.type) {
      case FieldType.BOOLEAN: {
        return renderTransformationArgumentBoolean(argId, a);
      }
      default: {
        return (
          <FormGroup
            className={css(styles.transArgs)}
            fieldId={argId}
            label={a.label}
            key={idx}
            style={formTransGroupStyle}
          >
            {a.options
              ? RenderTransformationArgumentOptions(
                  argId,
                  a,
                  disableTransformation,
                  onTransformationArgumentChange,
                )
              : renderTransformationArgumentText(argId, a)}
          </FormGroup>
        );
      }
    }
  };

  const id = `user-field-action-${name}`;
  return (
    <>
      <FormGroup fieldId={`${id}-transformation`}>
        <InputGroup style={{ background: 'transparent' }}>
          <FormSelect
            value={name}
            id={id}
            isDisabled={disableTransformation}
            onChange={onTransformationChange}
            data-testid={id}
            style={formTransStyle}
          >
            {transformationsOptions.map((a, idx) => (
              <FormSelectOption label={a.name} value={a.value} key={idx} />
            ))}
          </FormSelect>
          {onRemoveTransformation && (
            <Button
              variant={'plain'}
              onClick={onRemoveTransformation}
              data-testid={`close-transformation-${name}-button`}
              aria-label="Remove the transformation"
            >
              <TrashIcon />
            </Button>
          )}
        </InputGroup>
      </FormGroup>
      {transformationsArguments.map(renderTransformationArgument)}
    </>
  );
};
