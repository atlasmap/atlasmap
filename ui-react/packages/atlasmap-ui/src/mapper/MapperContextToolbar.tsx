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

import { FilePicker } from 'react-file-picker';

export interface IMapperContextToolbarProps {
  exportAtlasFile: (event: any) => void;
  importAtlasFile: (selectedFile: File) => void;
  resetAtlasmap: () => void;
}

export const MapperContextToolbar: FunctionComponent<
  IMapperContextToolbarProps
> = ({ exportAtlasFile, importAtlasFile, resetAtlasmap }) => {
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
          <FilePicker
            extensions={['adm', 'jar']}
            onChange={(selectedFile: File) => importAtlasFile(selectedFile)}
            onError={(errMsg: any) => console.error(errMsg)}
          >
            <Button variant={'plain'} aria-label="Import mappings">
              <ImportIcon />
            </Button>
          </FilePicker>
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
            <Button variant={'plain'} aria-label="Export mappings"
              onClick={(event: any) => exportAtlasFile(event)}
            >
              <ExportIcon />
            </Button>
          </Tooltip>
        </ToolbarItem>
        <ToolbarItem>
          <Tooltip
            position={'auto'}
            enableFlip={true}
            content={
              <div>Reset all mappings and clear all imported documents</div>
            }
          >
            <Button
              variant={'plain'}
              aria-label="Reset all"
              onClick={resetAtlasmap}
            >
              Reset all
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
