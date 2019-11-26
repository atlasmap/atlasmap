import React, { FunctionComponent, useCallback, useState } from 'react';
import {
  Button,
  OptionsMenu,
  OptionsMenuItem,
  OptionsMenuItemGroup,
  OptionsMenuPosition,
  OptionsMenuToggle,
  TextInput,
  Toolbar,
  ToolbarGroup,
  ToolbarItem,
} from '@patternfly/react-core';
import { CaretDownIcon, CaretUpIcon } from '@patternfly/react-icons';
export interface IMapperToolbarProps {
  freeView: boolean;
  toggleFreeView: () => void;
  materializedMappings: boolean;
  toggleMaterializedMappings: () => void;
}

export const ViewToolbar: FunctionComponent<IMapperToolbarProps> = ({
  freeView,
  toggleFreeView,
  materializedMappings,
  toggleMaterializedMappings,
}) => {
  const menuItems = [
    <OptionsMenuItemGroup key="first group" aria-label="Sort Column">
      <OptionsMenuItem
        onSelect={toggleFreeView}
        isSelected={freeView}
        id="free-view"
        key="free-view"
      >
        Free view mode
      </OptionsMenuItem>
      <OptionsMenuItem
        onSelect={toggleMaterializedMappings}
        isSelected={materializedMappings}
        id="materialized-mappings"
        key="materialized-mappings"
      >
        Toggle mappings column
      </OptionsMenuItem>
      <OptionsMenuItem>Show types</OptionsMenuItem>
      <OptionsMenuItem>Show types</OptionsMenuItem>
      <OptionsMenuItem>Show mapped fields</OptionsMenuItem>
      <OptionsMenuItem>Show unmapped fields</OptionsMenuItem>
      <OptionsMenuItem>Show mapping preview</OptionsMenuItem>
    </OptionsMenuItemGroup>,
  ];
  const [isOptionsMenuExpanded, setIsOptionsMenuExpanded] = useState(false);
  const toggleIsOptionsMenuExpanded = useCallback(
    () => setIsOptionsMenuExpanded(!isOptionsMenuExpanded),
    [isOptionsMenuExpanded, setIsOptionsMenuExpanded]
  );
  const toggle = (
    <OptionsMenuToggle
      onToggle={toggleIsOptionsMenuExpanded}
      toggleTemplate={
        <>
          Options&nbsp;
          {isOptionsMenuExpanded ? (
            <CaretUpIcon aria-hidden={true} />
          ) : (
            <CaretDownIcon aria-hidden={true} />
          )}
        </>
      }
    />
  );

  return (
    <Toolbar
      className="view-toolbar pf-u-px-md pf-u-py-md"
      style={{ borderBottom: '1px solid #ccc' }}
    >
      <ToolbarGroup style={{ flex: 1 }}>
        <ToolbarItem>
          <Button
            variant={'plain'}
            aria-label="Enable/ Disable conditional mapping expression"
            disabled={true}
          >
            <i>
              f<sub>(x)</sub>
            </i>
          </Button>
        </ToolbarItem>

        <ToolbarItem style={{ flex: 1 }}>
          <TextInput aria-label={'Conditional mapping expression'} />
        </ToolbarItem>
        <ToolbarItem>
          <OptionsMenu
            id="mapper-options"
            position={OptionsMenuPosition.right}
            menuItems={menuItems}
            isOpen={isOptionsMenuExpanded}
            toggle={toggle}
            isGrouped={true}
            isPlain={true}
          />
        </ToolbarItem>
      </ToolbarGroup>
    </Toolbar>
  );
};
