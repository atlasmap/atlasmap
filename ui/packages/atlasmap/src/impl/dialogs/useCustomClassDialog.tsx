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
import { CustomClassDialog, ICustomClass } from '../../UI';
import React, { ReactElement, useCallback, useState } from 'react';

import { collectionTypes } from '@atlasmap/core';
import { getCustomClassNameOptions } from '../utils/document';
import { useToggle } from '../utils';

type CustomClassCallback = (constant: ICustomClass) => void;

export function useCustomClassDialog(
  title: string,
): [ReactElement, (cb: CustomClassCallback, constant?: ICustomClass) => void] {
  const [onCustomClassCb, setOnCustomClassCb] =
    useState<CustomClassCallback | null>(null);

  const [initialCustomClass, setInitialCustomClass] =
    useState<ICustomClass | null>(null);

  const [customClassNames, setCustomClassNames] = useState<string[] | null>([]);

  const { state, toggleOn, toggleOff } = useToggle(false);
  const onConfirm = useCallback(
    (constant: ICustomClass) => {
      if (onCustomClassCb) {
        onCustomClassCb(constant);
        toggleOff();
      }
    },
    [onCustomClassCb, toggleOff],
  );

  const getCustomClassNames = async () => {
    setCustomClassNames(await getCustomClassNameOptions());
  };

  const dialog = (
    <CustomClassDialog
      title={title}
      isOpen={state}
      customClassName={customClassNames ? customClassNames[0] : ''}
      customClassNames={customClassNames}
      collectionTypeOptions={collectionTypes.map(([value, label]) => ({
        value,
        label,
      }))}
      onCancel={toggleOff}
      onConfirm={onConfirm}
      {...(initialCustomClass || { collectionType: 'NONE' })}
    />
  );
  const onOpenCustomClassDialog = useCallback(
    (callback: CustomClassCallback, constant?: ICustomClass) => {
      // we use a closure to set the state here else React will think that callback
      // is the function to retrieve the state and will call it immediately.
      setOnCustomClassCb(() => callback);
      if (constant) {
        setInitialCustomClass(constant);
      }
      getCustomClassNames();
      toggleOn();
    },
    [toggleOn],
  );
  return [dialog, onOpenCustomClassDialog];
}
