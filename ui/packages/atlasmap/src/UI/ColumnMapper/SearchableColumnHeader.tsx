import React, { FormEvent, FunctionComponent, useRef } from "react";

import { Button, InputGroup, TextInput } from "@patternfly/react-core";
import { SearchIcon } from "@patternfly/react-icons";

import { ColumnHeader, IColumnHeaderProps } from "./ColumnHeader";

export interface ISearchableColumnHeaderProps extends IColumnHeaderProps {
  onSearch: (content: string) => void;
}

export const SearchableColumnHeader: FunctionComponent<ISearchableColumnHeaderProps> = ({
  title,
  actions = [],
  onSearch,
}) => {
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
              name={"search"}
              id={"search"}
              type="search"
              aria-label="Search fields"
              onChange={onSearch}
              ref={searchRef}
              data-testid={`search-${title}-fields-input-field`}
            />
            <Button
              type={"submit"}
              aria-label="Search"
              data-testid={`run-search-${title}-button`}
              variant={"control"}
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
