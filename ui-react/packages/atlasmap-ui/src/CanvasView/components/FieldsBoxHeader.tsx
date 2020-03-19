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
  SplitItem,
  Stack,
  StackItem,
  TextInput,
  Tooltip,
} from '@patternfly/react-core';
import {
  SearchIcon,
  ImportIcon,
  AddCircleOIcon,
  PlusIcon,
} from '@patternfly/react-icons';
import React, { FunctionComponent, useCallback, useState } from 'react';
import { FilePicker } from 'react-file-picker';

export interface IFieldsBoxHeaderProps {
  title: string;
  onSearch: (content: string) => void;
  onImport: (selectedFile: File) => void;
  onJavaClasses: () => void;
  onCreateConstant: () => void;
  onCreateProperty: () => void;
}

export const FieldsBoxHeader: FunctionComponent<IFieldsBoxHeaderProps> = ({
  title,
  onSearch,
  onImport,
  onJavaClasses,
  onCreateConstant,
  onCreateProperty,
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
        <Flex
          breakpointMods={[{ modifier: FlexModifiers['space-items-none'] }]}
        >
          {showSearch ? (
            <FlexItem>{title}</FlexItem>
          ) : (
            <FlexItem breakpointMods={[{ modifier: FlexModifiers['flex-1'] }]}>
              {title}
            </FlexItem>
          )}
          <FlexItem>
            <Button
              variant={ButtonVariant.control}
              aria-label="Search button for search input"
              onClick={toggleSearch}
              data-testid={`search-for-${title}-input-button`}
            >
              <SearchIcon />
            </Button>
          </FlexItem>
          {showSearch && (
            <FlexItem breakpointMods={[{ modifier: FlexModifiers['flex-1'] }]}>
              <InputGroup>
                <TextInput
                  name={'source-search'}
                  id={'source-search'}
                  type="search"
                  aria-label="Search source fields"
                  data-testid={`search-for-${title}-input-field`}
                  autoFocus={true}
                  onChange={onSearch}
                />
              </InputGroup>
            </FlexItem>
          )}
          <FlexItem
            breakpointMods={[
              { modifier: FlexModifiers['align-self-flex-end'] },
            ]}
          >
            <Dropdown
              toggle={<DropdownToggle onToggle={toggleActions} />}
              isOpen={showActions}
              position={'right'}
              data-testid={`dropdown-handle-${title}-button`}
              dropdownItems={[
                <DropdownItem
                  variant={'icon'}
                  key={'import'}
                  data-testid={'import-instance-or-schema-file'}
                >
                  <SplitItem isFilled>
                    <Tooltip
                      position={'auto'}
                      enableFlip={true}
                      content={<div>Import instance or schema file</div>}
                    >
                      <FilePicker
                        extensions={['json', 'xml', 'xsd']}
                        onChange={(selectedFile: File) =>
                          onImport(selectedFile)
                        }
                        onError={(errMsg: any) => console.error(errMsg)}
                      >
                        <div>
                          <DropdownItemIcon>
                            <ImportIcon />
                          </DropdownItemIcon>
                          Import
                        </div>
                      </FilePicker>
                    </Tooltip>
                  </SplitItem>
                </DropdownItem>,
                <DropdownSeparator key={'sep-1'} />,
                <DropdownItem
                  variant={'icon'}
                  key={'java-classes'}
                  onClick={onJavaClasses}
                  data-testid={'enable-java-classes'}
                >
                  <SplitItem isFilled>
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
                      <div>
                        <DropdownItemIcon>
                          <AddCircleOIcon />
                        </DropdownItemIcon>
                        Enable Java classes
                      </div>
                    </Tooltip>
                  </SplitItem>
                </DropdownItem>,
                <DropdownItem
                  variant={'icon'}
                  key={'create-constant'}
                  onClick={onCreateConstant}
                  data-testid={'create-constant'}
                >
                  <SplitItem isFilled>
                    <Tooltip
                      position={'auto'}
                      enableFlip={true}
                      content={<div>Create a constant for use in mapping</div>}
                    >
                      <div>
                        <DropdownItemIcon>
                          <PlusIcon />
                        </DropdownItemIcon>
                        Create a constant
                      </div>
                    </Tooltip>
                  </SplitItem>
                </DropdownItem>,
                <DropdownItem
                  variant={'icon'}
                  key={'create-property'}
                  onClick={onCreateProperty}
                  data-testid={'create-property'}
                >
                  <SplitItem isFilled>
                    <Tooltip
                      position={'auto'}
                      enableFlip={true}
                      content={<div>Create a property for use in mapping</div>}
                    >
                      <div>
                        <DropdownItemIcon>
                          <PlusIcon />
                        </DropdownItemIcon>
                        Create a property
                      </div>
                    </Tooltip>
                  </SplitItem>
                </DropdownItem>,
              ]}
            />
          </FlexItem>
        </Flex>
      </StackItem>
    </Stack>
  );
};
