import {
  Button,
  ButtonVariant,
  Dropdown,
  DropdownItem,
  DropdownItemIcon,
  DropdownSeparator,
  DropdownToggle,
  Flex,
  FlexItem,
  FlexModifiers,
  InputGroup,
  Stack,
  StackItem,
  TextInput,
  Tooltip,
} from '@patternfly/react-core';
import {
  SearchIcon,
  ImportIcon,
  AddCircleOIcon,
} from '@patternfly/react-icons';
import React, { FunctionComponent, useCallback, useState } from 'react';
import { FilePicker } from 'react-file-picker';

export interface IFieldsBoxHeaderProps {
  title: string;
  onSearch: (content: string) => void;
  onImport: (selectedFile: File) => void;
  onJavaClasses: () => void;
}

export const FieldsBoxHeader: FunctionComponent<IFieldsBoxHeaderProps> = ({
  title,
  onSearch,
  onImport,
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
        <Flex breakpointMods={[{modifier: FlexModifiers['space-items-none']}]}>
          {showSearch ? (
              <FlexItem>{title}</FlexItem>
          ) : (
            <FlexItem breakpointMods={[{modifier: FlexModifiers['flex-1']}]}>{title}</FlexItem>
          )}
          <FlexItem>
            <Button
              variant={ButtonVariant.control}
              aria-label="Search button for search input"
              onClick={toggleSearch}
            >
              <SearchIcon />
            </Button>
          </FlexItem>
          {showSearch && (
            <FlexItem breakpointMods={[{modifier: FlexModifiers['flex-1']}]}>
              <InputGroup>
                <TextInput
                  name={'source-search'}
                  id={'source-search'}
                  type="search"
                  aria-label="Search source fields"
                  autoFocus={true}
                  onChange={onSearch}
                />
              </InputGroup>
            </FlexItem>
          )}
          <FlexItem breakpointMods={[{modifier: FlexModifiers['align-self-flex-end']}]}>
            <Dropdown
              toggle={<DropdownToggle onToggle={toggleActions} />}
              isOpen={showActions}
              position={'right'}
              dropdownItems={[
                <DropdownItem variant={'icon'} key={'import'}>
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
                      onChange={(selectedFile: File) => onImport(selectedFile)}
                      onError={(errMsg: any) => console.error(errMsg)}
                    >
                      <div>Import</div>
                    </FilePicker>
                  </Tooltip>
                </DropdownItem>,
                <DropdownSeparator key={'sep-1'} />,
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
          </FlexItem>
        </Flex>
      </StackItem>
    </Stack>
  );
};
