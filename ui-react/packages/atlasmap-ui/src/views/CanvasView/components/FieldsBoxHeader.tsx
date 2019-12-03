import {
  Button,
  ButtonVariant,
  Dropdown,
  DropdownItem,
  DropdownItemIcon,
  DropdownSeparator,
  DropdownToggle,
  DropdownToggleAction,
  InputGroup,
  Level,
  LevelItem,
  Stack,
  StackItem,
  TextInput,
  Tooltip,
} from '@patternfly/react-core';
import {
  FilterIcon,
  SearchIcon,
  ImportIcon,
  AddCircleOIcon,
} from '@patternfly/react-icons';
import React, { FunctionComponent, useCallback, useState } from 'react';
import { FilePicker } from 'react-file-picker';

export interface IFieldsBoxHeaderProps {
  title: string;
  onSearch: (content: string) => void;
  onImportAtlasFile: (selectedFile: File, isSource: boolean) => void;
  onJavaClasses: () => void;
}

export const FieldsBoxHeader: FunctionComponent<IFieldsBoxHeaderProps> = ({
  title,
  onSearch,
  onImportAtlasFile,
  onJavaClasses,
}) => {
  const [showActions, setShowActions] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const toggleActions = (open: boolean) => setShowActions(open);
  const toggleSearch = useCallback(() => setShowSearch(!showSearch), [
    showSearch,
  ]);

  return (
    <Stack>
      <StackItem>
        <Level>
          <LevelItem>
            {title}
          </LevelItem>
          <LevelItem>
            <Dropdown
              toggle={
                <DropdownToggle
                  splitButtonItems={[
                    <DropdownToggleAction key="action" onClick={toggleSearch}>
                      <FilterIcon />
                    </DropdownToggleAction>,
                  ]}
                  splitButtonVariant={'action'}
                  onToggle={toggleActions}
                />
              }
              isOpen={showActions}
              position={'right'}
              dropdownItems={[
                <DropdownItem
                  variant={'icon'}
                  key={'import'}
                >
                  <DropdownItemIcon>
                    <ImportIcon />
                  </DropdownItemIcon>
                  <Tooltip
                    position={'auto'}
                    enableFlip={true}
                    content={<div>Import instance or schema file</div>}
                  >
                    <FilePicker
                      extensions={['json', 'xml', 'xsd']}
                      onChange={(selectedFile: File) =>
                        onImportAtlasFile(selectedFile, title === 'Source')}
                      onError={(errMsg: any) => console.error(errMsg)}
                    >
                      <Button variant={'plain'} aria-label="Import mappings">
                        <div>Import</div>
                      </Button>
                    </FilePicker>
                  </Tooltip>
                </DropdownItem>,
                <DropdownSeparator />,
                <DropdownItem
                  variant={'icon'}
                  key={'java-classes'}
                  onClick={onJavaClasses}
                >
                  <DropdownItemIcon>
                    <AddCircleOIcon />
                  </DropdownItemIcon>
                  <Tooltip
                    position={'auto'}
                    enableFlip={true}
                    content={
                      <div>
                        Enable specific Java classes from your previously
                        imported Java archive.
                      </div>
                    }
                  >
                    <div>Enable Java classes</div>
                  </Tooltip>
                </DropdownItem>,
              ]}
            />
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
