import {
  CodeIcon,
  ExchangeAltIcon,
  ExportIcon,
  ImportIcon,
  TableIcon,
} from '@patternfly/react-icons';
import React, { FunctionComponent } from 'react';
import {
  Button,
  Toolbar,
  ToolbarGroup,
  ToolbarItem,
  Tooltip,
} from '@patternfly/react-core';

export interface IMapperContextToolbarProps {}

export const MapperContextToolbar: FunctionComponent<
  IMapperContextToolbarProps
> = () => {
  return (
    <Toolbar
      className="view-toolbar pf-u-px-md pf-u-py-md"
      style={{ borderBottom: '1px solid #ccc' }}
    >
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
      </ToolbarGroup>
    </Toolbar>
  );
};
