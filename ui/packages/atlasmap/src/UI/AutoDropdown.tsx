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
import { Dropdown, DropdownProps } from '@patternfly/react-core';
import React, { FunctionComponent, ReactElement } from 'react';

import { useToggle } from '../impl/utils';

export interface IAutoDropdown
  extends Omit<Omit<DropdownProps, 'css'>, 'toggle'> {
  toggle: (props: { isOpen: boolean; toggleOpen: () => void }) => ReactElement;
}

export const AutoDropdown: FunctionComponent<IAutoDropdown> = ({
  toggle,
  ...props
}) => {
  const { state: isOpen, toggle: toggleOpen } = useToggle(false);

  return (
    <Dropdown
      {...props}
      isOpen={isOpen}
      onSelect={toggleOpen}
      toggle={toggle({ isOpen, toggleOpen })}
    />
  );
};
