import {
  Button,
  ButtonVariant,
  InputGroup,
  Level,
  LevelItem,
  Stack,
  StackItem,
  TextInput,
  Tooltip,
} from '@patternfly/react-core';
import {
  AddCircleOIcon,
  FilterIcon,
  ImportIcon,
  SearchIcon,
} from '@patternfly/react-icons';
import React, { FunctionComponent, useState } from 'react';

export interface IFieldsBoxHeaderProps {
  title: string;
  onSearch: (content: string) => void;
  onImport: () => void;
  onJavaClasses: () => void;
}

export const FieldsBoxHeader: FunctionComponent<IFieldsBoxHeaderProps> = ({
  title,
  onSearch,
  onImport,
  onJavaClasses,
}) => {
  const [showSearch, setShowSearch] = useState(false);
  const toggleSearch = () => setShowSearch(!showSearch);

  return (
    <Stack>
      <StackItem>
        <Level>
          <LevelItem>
            {title}
          </LevelItem>
          <LevelItem>
            <Tooltip
              position={'auto'}
              enableFlip={true}
              content={<div>Import instance or schema file</div>}
            >
              <Button variant="plain" aria-label="Action" onClick={onImport}>
                <ImportIcon />
              </Button>
            </Tooltip>

            <Tooltip
              position={'auto'}
              enableFlip={true}
              content={
                <div>
                  Enable specific Java classes from your previously imported
                  Java archive.
                </div>
              }
            >
              <Button
                variant="plain"
                aria-label="Enable Java classes"
                onClick={onJavaClasses}
              >
                <AddCircleOIcon />
              </Button>
            </Tooltip>

            <Tooltip
              position={'auto'}
              enableFlip={true}
              content={<div>Toggle field search window</div>}
            >
              <Button
                variant="plain"
                aria-label="Toggle field search"
                onClick={toggleSearch}
                isActive={showSearch}
              >
                <FilterIcon />
              </Button>
            </Tooltip>
          </LevelItem>
        </Level>
      </StackItem>
      {showSearch && (
        <StackItem>
          <InputGroup>
            <TextInput
              name={'source-search'}
              id={'source-search'}
              type="search"
              aria-label="Search source fields"
              autoFocus={true}
              onChange={onSearch}
            />
            <Button
              variant={ButtonVariant.control}
              aria-label="Search button for search input"
            >
              <SearchIcon />
            </Button>
          </InputGroup>
        </StackItem>
      )}
    </Stack>
  );
};
