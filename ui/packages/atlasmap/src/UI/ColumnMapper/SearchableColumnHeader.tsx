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
import { Button, InputGroup, TextInput } from '@patternfly/react-core';
import { ColumnHeader, IColumnHeaderProps } from './ColumnHeader';
import React, { FormEvent, FunctionComponent, useRef } from 'react';

import { SearchIcon } from '@patternfly/react-icons';

export interface ISearchableColumnHeaderProps extends IColumnHeaderProps {
  onSearch: (content: string) => void;
}

export const SearchableColumnHeader: FunctionComponent<
  ISearchableColumnHeaderProps
> = ({ title, actions = [], onSearch }) => {
  const searchRef = useRef<HTMLInputElement | null>(null);
  const onSearchSubmit = (event: FormEvent<HTMLFormElement>) => {
    if (searchRef.current) {
      onSearch(searchRef.current.value);
    }
    event.preventDefault();
    return false;
  };
  return (
    <ColumnHeader
      title={title}
      actions={[
        <form onSubmit={onSearchSubmit} key="search-form">
          <InputGroup>
            <TextInput
              name={'search'}
              id={`search-${title}`}
              type="search"
              aria-label="Search fields"
              onChange={onSearch}
              ref={searchRef}
              data-testid={`search-${title}-fields-input-field`}
            />
            <Button
              type={'submit'}
              aria-label="Search"
              data-testid={`run-search-${title}-button`}
              variant={'control'}
            >
              <SearchIcon />
            </Button>
          </InputGroup>
        </form>,
        ...actions,
      ]}
    />
  );
};
