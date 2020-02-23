import {
  CodeIcon,
  ExportIcon,
  ImportIcon,
  TableIcon,
  EyeIcon,
  InfoIcon,
  MapMarkedIcon,
  MapIcon,
} from '@patternfly/react-icons';
import React, { FunctionComponent } from 'react';
import { Button, Tooltip, Toolbar, ToolbarItem } from '@patternfly/react-core';

import { FilePicker } from 'react-file-picker';
import { css, StyleSheet } from '@patternfly/react-styles';

const styles = StyleSheet.create({
  toolbar: { borderBottom: '1px solid #ccc' },
});

export interface IAtlasmapContextToolbarProps {
  onExportAtlasFile: (event: any) => void;
  onImportAtlasFile: (selectedFile: File) => void;
  onResetAtlasmap: () => void;
  onToggleShowTypes: (id: any) => void;
  onToggleShowMappingPreview: (id: any) => void;
  onToggleShowMappedFields: (id: any) => void;
  onToggleShowUnmappedFields: (id: any) => void;
}

export const AtlasmapContextToolbar: FunctionComponent<IAtlasmapContextToolbarProps> = ({
  onImportAtlasFile,
  onResetAtlasmap,
  onExportAtlasFile,
  onToggleShowMappingPreview,
  onToggleShowTypes,
  onToggleShowMappedFields,
  onToggleShowUnmappedFields,
}) => {
  return (
    <Toolbar
      id="data-toolbar"
      className={css('view-toolbar pf-u-px-md pf-u-py-md', styles.toolbar)}
    >
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
            onChange={onImportAtlasFile}
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
          <Button
            variant={'plain'}
            aria-label="Export mappings"
            onClick={(event: any) => onExportAtlasFile(event)}
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
            onClick={onResetAtlasmap}
          >
            Reset all
          </Button>
        </Tooltip>
      </ToolbarItem>
      <ToolbarItem>
        <Tooltip
          position={'auto'}
          enableFlip={true}
          content={<div>Show/hide mapping preview</div>}
        >
          <Button
            variant={'plain'}
            aria-label="Show/hide mapping preview"
            onClick={onToggleShowMappingPreview}
          >
            <EyeIcon />
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
          content={<div>Show/hide types</div>}
        >
          <Button
            variant={'plain'}
            aria-label="Show/hide types"
            onClick={onToggleShowTypes}
          >
            <InfoIcon />
          </Button>
        </Tooltip>
      </ToolbarItem>
      <ToolbarItem>
        <Tooltip
          position={'auto'}
          enableFlip={true}
          content={<div>Show/hide mapped fields</div>}
        >
          <Button
            variant={'plain'}
            aria-label="Show/hide mapped fields"
            onClick={onToggleShowMappedFields}
          >
            <MapMarkedIcon />
          </Button>
        </Tooltip>
      </ToolbarItem>
      <ToolbarItem>
        <Tooltip
          position={'auto'}
          enableFlip={true}
          content={<div>Show/hide unmapped fields</div>}
        >
          <Button
            variant={'plain'}
            aria-label="Show/hide unmapped fields"
            onClick={onToggleShowUnmappedFields}
          >
            <MapIcon />
          </Button>
        </Tooltip>
      </ToolbarItem>
    </Toolbar>
  );
};
