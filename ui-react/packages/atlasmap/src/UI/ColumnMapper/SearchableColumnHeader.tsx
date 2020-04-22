import React, {
  FormEvent,
  FunctionComponent,
  useRef,
  useState,
  useCallback,
} from "react";

import { Button, InputGroup, TextInput } from "@patternfly/react-core";
import { SearchIcon } from "@patternfly/react-icons";

import { ColumnHeader, IColumnHeaderProps } from "./ColumnHeader";

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
  const [showSearch, setShowSearch] = useState(false);
  const toggleSearch = useCallback(() => setShowSearch(!showSearch), [
    showSearch,
  ]);
  const searchRef = useRef<HTMLInputElement | null>(null);
  const onSearchSubmit = (event: FormEvent<HTMLFormElement>) => {
    if (searchRef.current) {
      onSearch(searchRef.current.value);
    }
    event.preventDefault();
    return false;
  };
  const searchIfEmpty = (value: string) => {
    console.log("onSearch", value);
    if (value === "") {
      onSearch("");
    }
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
              onChange={searchIfEmpty}
              ref={searchRef}
            />
            <Button type={"submit"} aria-label="Search">
              Search
            </Button>
          </InputGroup>
        </form>
      )}
    </ColumnHeader>
  );
};
