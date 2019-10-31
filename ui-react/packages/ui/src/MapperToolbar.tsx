import React, { FunctionComponent, useCallback, useState } from 'react';
import {
  Button,
  Select,
  SelectOption,
  SelectVariant, TextInput,
  Toolbar,
  ToolbarGroup,
  ToolbarItem,
  Tooltip,
} from '@patternfly/react-core';
import {
  ImportIcon,
  ExportIcon,
  PlusIcon,
  TableIcon,
  CodeIcon,
  ExchangeAltIcon,
  CogIcon,
} from '@patternfly/react-icons';

export interface IMapperToolbarProps {}

export const MapperToolbar: FunctionComponent<
  IMapperToolbarProps
> = () => {
  const [isSettingsExpanded, setIsSettingsExpanded] = useState(false);
  const toggleIsSettingsExpanded = useCallback(
    () => setIsSettingsExpanded(!isSettingsExpanded),
    [isSettingsExpanded, setIsSettingsExpanded]
  );
  return (
    <Toolbar
      className="view-toolbar pf-u-px-md pf-u-py-md"
      style={{ borderBottom: '1px solid #ccc' }}
    >
      <ToolbarGroup style={{ flex: 1 }}>
        <ToolbarItem>
          <Button variant={'plain'} aria-label="Enable/ Disable conditional mapping expression" disabled={true}>
            <i>f<sub>(x)</sub></i>
          </Button>
        </ToolbarItem>
        <ToolbarItem style={{ flex: 1 }}>
          <TextInput />
        </ToolbarItem>
      </ToolbarGroup>
      <ToolbarGroup>
        <ToolbarItem>
          <Tooltip
            position={'auto'}
            enableFlip={true}
            content={
              <div>
                Import an AtlasMap mappings catalog file (.adm) or Java archive
                (.jar).
              </div>
            }
          >
            <Button variant={'plain'} aria-label="Import mappings">
              <ImportIcon />
            </Button>
          </Tooltip>
        </ToolbarItem>
        <ToolbarItem>
          <Tooltip
            position={'auto'}
            enableFlip={true}
            content={
              <div>
                Export the current mappings and support files into a catalog
                (.adm) file.
              </div>
            }
          >
            <Button variant={'plain'} aria-label="Export mappings">
              <ExportIcon />
            </Button>
          </Tooltip>
        </ToolbarItem>
      </ToolbarGroup>
      <ToolbarGroup>
        <ToolbarItem>
          <Tooltip
            position={'auto'}
            enableFlip={true}
            content={<div>Add new mapping</div>}
          >
            <Button variant={'plain'} aria-label="Add new mapping">
              <PlusIcon />
            </Button>
          </Tooltip>
        </ToolbarItem>
        <ToolbarItem>
          <Tooltip
            position={'auto'}
            enableFlip={true}
            content={<div>Show/hide mapping table</div>}
          >
            <Button variant={'plain'} aria-label="Show/hide mapping table">
              <TableIcon />
            </Button>
          </Tooltip>
        </ToolbarItem>
        <ToolbarItem>
          <Tooltip
            position={'auto'}
            enableFlip={true}
            content={<div>Show/hide namespace table</div>}
          >
            <Button variant={'plain'} aria-label="Show/hide namespace table">
              <CodeIcon />
            </Button>
          </Tooltip>
        </ToolbarItem>
        <ToolbarItem>
          <Tooltip
            position={'auto'}
            enableFlip={true}
            content={<div>Show/hide mapping details</div>}
          >
            <Button variant={'plain'} aria-label="Show/hide mapping details">
              <ExchangeAltIcon />
            </Button>
          </Tooltip>
        </ToolbarItem>
        <ToolbarItem>
          <Select
            variant={SelectVariant.checkbox}
            aria-label={'Settings'}
            onToggle={toggleIsSettingsExpanded}
            onSelect={() => false}
            selections={''}
            isExpanded={isSettingsExpanded}
            placeholderText={'Settings'}
            toggleIcon={<CogIcon />}
          >
            <SelectOption key={'Show Types'} value={'Show Types'} />
            <SelectOption key={'Show Lines'} value={'Show Lines'} />
            <SelectOption
              key={'Show Mapped Fields'}
              value={'Show Mapped Fields'}
            />
            <SelectOption
              key={'Show Unmapped Fields'}
              value={'Show Unmapped Fields'}
            />
            <SelectOption
              key={'Show Mapping Preview'}
              value={'Show Mapping Preview'}
            />
          </Select>
        </ToolbarItem>
      </ToolbarGroup>
    </Toolbar>
  );
};
