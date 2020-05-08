import React, { FormEvent, FunctionComponent, useRef } from "react";

import { Button, InputGroup, TextInput } from "@patternfly/react-core";
import { SearchIcon } from "@patternfly/react-icons";

import { ColumnHeader, IColumnHeaderProps } from "./ColumnHeader";
import { useToggle } from "../useToggle";

export interface ISearchableColumnHeaderProps extends IColumnHeaderProps {
  onSearch: (content: string) => void;
  autoFocus?: boolean;
}

export const SearchableColumnHeader: FunctionComponent<ISearchableColumnHeaderProps> = ({
  title,
  actions = [],
  onSearch,
  autoFocus = true,
}) => {
  const cleanSearchOnTogglingSearchOff = (toggled: boolean) => {
    if (!toggled) {
      onSearch("");
    }
    return toggled;
  };
  const { state: showSearch, toggle: toggleSearch } = useToggle(
    false,
    cleanSearchOnTogglingSearchOff,
  );
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
        <Button
          variant={"plain"}
          isInline={true}
          aria-label="Search button for search input"
          key={"search"}
          onClick={toggleSearch}
          data-testid={`search-${title}-button`}
        >
          <SearchIcon />
        </Button>,
        ...actions,
      ]}
    >
      {showSearch && (
        <form onSubmit={onSearchSubmit}>
          <InputGroup>
            <TextInput
              name={"search"}
              id={"search"}
              type="search"
              placeholder="Search fields..."
              aria-label="Search fields"
              autoFocus={autoFocus}
              onChange={onSearch}
              ref={searchRef}
              data-testid={`search-${title}-fields-input-field`}
            />
            <Button
              type={"submit"}
              aria-label="Search"
              data-testid={`run-search-${title}-button`}
            >
              Search
            </Button>
          </InputGroup>
        </form>
      )}
    </ColumnHeader>
  );
};
