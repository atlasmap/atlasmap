import { PlusIcon } from '@patternfly/react-icons';
import React, { FunctionComponent } from 'react';
import {
  Button,
  TextInput,
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
              <PlusIcon />
            </Button>
          </Tooltip>
        </ToolbarItem>
        <ToolbarItem>
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
        </ToolbarItem>
      </ToolbarGroup>
    </Toolbar>
  );
};
