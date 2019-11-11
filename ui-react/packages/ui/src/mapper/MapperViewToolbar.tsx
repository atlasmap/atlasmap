import { LineIcon, MapIcon, PluggedIcon, RemoveFormatIcon, UnpluggedIcon } from '@patternfly/react-icons';
import React, { FunctionComponent } from 'react';
import {
  Button, Checkbox,
  Toolbar,
  ToolbarGroup,
  ToolbarItem,
  Tooltip,
} from '@patternfly/react-core';
export interface IMapperToolbarProps {}

export const MapperViewToolbar: FunctionComponent<IMapperToolbarProps> = () => {
  return (
    <Toolbar
      className='view-toolbar pf-u-px-md pf-u-py-md'
      style={{ borderBottom: '1px solid #ccc' }}
    >
      <ToolbarGroup style={{ flex: 1 }}>
        <ToolbarItem>
          <Tooltip
            position={'auto'}
            enableFlip={true}
            content={<div>Add new mapping</div>}
          >
            <Button variant={'plain'} aria-label="Add new mapping">
              Add new mapping
            </Button>
          </Tooltip>
        </ToolbarItem>
        {/*<ToolbarItem>
          <Button
            variant={'plain'}
            aria-label='Enable/ Disable conditional mapping expression'
            disabled={true}
          >
            <i>
              f<sub>(x)</sub>
            </i>
          </Button>
        </ToolbarItem>

        <ToolbarItem style={{ flex: 1 }}>
          <TextInput aria-label={'Conditional mapping expression'} />
        </ToolbarItem>*/}
        <ToolbarItem>
          <Button variant={'plain'} aria-label='Show types'>
            <Checkbox
              id={'id'}
              label={
              <RemoveFormatIcon/>
            } />
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <Button variant={'plain'} aria-label='Show types'>
            <Checkbox
              id={'id'}
              label={
              <LineIcon/>
            } />
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <Button variant={'plain'} aria-label='Show mapped fields'>
            <Checkbox
              id={'id'}
              label={
              <PluggedIcon/>
            } />
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <Button variant={'plain'} aria-label='Show unmapped fields'>
            <Checkbox
              id={'id'}
              label={
              <UnpluggedIcon/>
            } />
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <Button variant={'plain'} aria-label='Show mapping preview'>
            <Checkbox
              id={'id'}
              label= {
              <MapIcon/>
              }
            />
          </Button>
        </ToolbarItem>
      </ToolbarGroup>
    </Toolbar>
  );
};
