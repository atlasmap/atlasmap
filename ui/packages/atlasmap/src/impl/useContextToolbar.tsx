/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import {
  AboutToolbarItem,
  AddMappingToolbarItem,
  AtlasmapToolbarItem,
  ToggleColumnMapperViewToolbarItem,
  ToggleMappedFieldsToolbarItem,
  ToggleMappingPreviewToolbarItem,
  ToggleMappingTableViewToolbarItem,
  ToggleNamespaceTableViewToolbarItem,
  ToggleTypesToolbarItem,
  ToggleUnmappedFieldsToolbarItem,
} from './toolbarItems';
import React, { useMemo, useState } from 'react';

import { ContextToolbar } from '../Layout';
import { ToolbarGroup } from '@patternfly/react-core';
import { useAtlasmap } from './AtlasmapProvider';
import { useToggle } from './utils';

export type Views =
  | 'ColumnMapper'
  | 'MappingTable'
  | 'NamespaceTable'
  | 'FreeView';

export interface IUseContextToolbarHandlers {
  onImportADMArchiveFile: (file: File) => void;
  onImportJarFile: (file: File) => void;
  onExportAtlasFile: () => void;
  onResetAtlasmap: () => void;
  onAbout: () => void;
}

export interface IUseContextToolbarData {
  showImportAtlasFileToolbarItem?: boolean;
  showImportJarFileToolbarItem?: boolean;
  showExportAtlasFileToolbarItem?: boolean;
  showResetToolbarItem?: boolean;

  showColumnMapperViewToolbarItem?: boolean;
  showMappingTableViewToolbarItem?: boolean;
  showNamespaceTableViewToolbarItem?: boolean;

  showToggleMappingPreviewToolbarItem?: boolean;
  showToggleTypesToolbarItem?: boolean;
  showToggleMappedFieldsToolbarItem?: boolean;
  showToggleUnmappedFieldsToolbarItem?: boolean;
  showAddNewMappingToolbarItem?: boolean;
}

export function useContextToolbar({
  showImportAtlasFileToolbarItem = true,
  showImportJarFileToolbarItem = true,
  showExportAtlasFileToolbarItem = true,
  showResetToolbarItem = true,

  showColumnMapperViewToolbarItem = true,
  showMappingTableViewToolbarItem = true,
  showNamespaceTableViewToolbarItem = true,

  showToggleMappingPreviewToolbarItem = true,
  showToggleTypesToolbarItem = true,
  showToggleMappedFieldsToolbarItem = true,
  showToggleUnmappedFieldsToolbarItem = true,
  showAddNewMappingToolbarItem = true,

  onImportADMArchiveFile,
  onImportJarFile,
  onExportAtlasFile,
  onResetAtlasmap,
  onAbout,
}: IUseContextToolbarData & IUseContextToolbarHandlers) {
  const {
    toggleMappingPreview: amToggleMappingPreview,
    toggleShowMappedFields: amToggleShowMappedFields,
    toggleShowUnmappedFields: amToggleShowUnmappedFields,
    newMapping,
  } = useAtlasmap();

  const [activeView, setActiveView] = useState<Views>('ColumnMapper');
  const { state: showMappingPreview, toggle: toggleShowMappingPreview } =
    useToggle(false, amToggleMappingPreview);
  const { state: showTypes, toggle: toggleShowTypes } = useToggle(true);
  const { state: showMappedFields, toggle: toggleShowMappedFields } = useToggle(
    true,
    amToggleShowMappedFields,
  );
  const { state: showUnmappedFields, toggle: toggleShowUnmappedFields } =
    useToggle(true, amToggleShowUnmappedFields);

  const contextToolbar = useMemo(
    () => (
      <ContextToolbar>
        {(showImportAtlasFileToolbarItem ||
          showImportJarFileToolbarItem ||
          showExportAtlasFileToolbarItem ||
          showResetToolbarItem) && (
          <ToolbarGroup variant="button-group" spacer={{ default: 'spacerMd' }}>
            <AtlasmapToolbarItem
              showImportAtlasFileToolbarItem={showImportAtlasFileToolbarItem}
              showImportJarFileToolbarItem={showImportJarFileToolbarItem}
              showExportAtlasFileToolbarItem={showExportAtlasFileToolbarItem}
              showResetToolbarItem={showResetToolbarItem}
              onImportAtlasFile={onImportADMArchiveFile}
              onImportJarFile={onImportJarFile}
              onExportAtlasFile={onExportAtlasFile}
              onResetAtlasmap={onResetAtlasmap}
            />
          </ToolbarGroup>
        )}
        {(showColumnMapperViewToolbarItem ||
          showMappingTableViewToolbarItem ||
          showNamespaceTableViewToolbarItem) && (
          <ToolbarGroup
            variant="icon-button-group"
            spacer={{ default: 'spacerMd' }}
          >
            {showColumnMapperViewToolbarItem && (
              <ToggleColumnMapperViewToolbarItem
                toggled={activeView === 'ColumnMapper'}
                onClick={() => setActiveView('ColumnMapper')}
              />
            )}
            {showMappingTableViewToolbarItem && (
              <ToggleMappingTableViewToolbarItem
                toggled={activeView === 'MappingTable'}
                onClick={() => setActiveView('MappingTable')}
              />
            )}
            {showNamespaceTableViewToolbarItem && (
              <ToggleNamespaceTableViewToolbarItem
                toggled={activeView === 'NamespaceTable'}
                onClick={() => setActiveView('NamespaceTable')}
              />
            )}
          </ToolbarGroup>
        )}
        <ToolbarGroup variant="icon-button-group">
          {showToggleMappingPreviewToolbarItem &&
            (activeView === 'ColumnMapper' ||
              activeView === 'MappingTable') && (
              <ToggleMappingPreviewToolbarItem
                toggled={showMappingPreview}
                onClick={toggleShowMappingPreview}
              />
            )}
          {showToggleTypesToolbarItem &&
            (activeView === 'ColumnMapper' ||
              activeView === 'MappingTable') && (
              <ToggleTypesToolbarItem
                toggled={showTypes}
                onClick={toggleShowTypes}
              />
            )}
          {showToggleMappedFieldsToolbarItem &&
            activeView === 'ColumnMapper' && (
              <ToggleMappedFieldsToolbarItem
                toggled={showMappedFields}
                onClick={toggleShowMappedFields}
              />
            )}
          {showToggleUnmappedFieldsToolbarItem &&
            activeView === 'ColumnMapper' && (
              <ToggleUnmappedFieldsToolbarItem
                toggled={showUnmappedFields}
                onClick={toggleShowUnmappedFields}
              />
            )}
          {showAddNewMappingToolbarItem &&
            (activeView === 'ColumnMapper' ||
              activeView === 'MappingTable') && (
              <AddMappingToolbarItem onClick={newMapping} />
            )}
          {showAddNewMappingToolbarItem &&
            (activeView === 'ColumnMapper' ||
              activeView === 'MappingTable') && (
              <AboutToolbarItem onClick={onAbout} />
            )}
        </ToolbarGroup>
      </ContextToolbar>
    ),
    [
      showImportAtlasFileToolbarItem,
      showImportJarFileToolbarItem,
      showExportAtlasFileToolbarItem,
      showResetToolbarItem,
      onImportADMArchiveFile,
      onImportJarFile,
      onExportAtlasFile,
      onResetAtlasmap,
      onAbout,
      showColumnMapperViewToolbarItem,
      showMappingTableViewToolbarItem,
      showNamespaceTableViewToolbarItem,
      activeView,
      showToggleMappingPreviewToolbarItem,
      showMappingPreview,
      toggleShowMappingPreview,
      showToggleTypesToolbarItem,
      showTypes,
      toggleShowTypes,
      showToggleMappedFieldsToolbarItem,
      showMappedFields,
      toggleShowMappedFields,
      showToggleUnmappedFieldsToolbarItem,
      showUnmappedFields,
      toggleShowUnmappedFields,
      showAddNewMappingToolbarItem,
      newMapping,
    ],
  );

  return {
    activeView,
    showMappingPreview,
    showTypes,
    showMappedFields,
    showUnmappedFields,
    contextToolbar,
  };
}
