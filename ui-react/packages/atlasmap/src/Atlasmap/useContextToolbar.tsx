import React, { useMemo, useState } from "react";

import { ToolbarGroup } from "@patternfly/react-core";

import { ContextToolbar } from "../Layout";
import { useToggle } from "../UI";
import {
  ExportAtlasFileToolbarItem,
  ImportAtlasFileToolbarItem,
  ResetToolbarItem,
  ToggleColumnMapperViewToolbarItem,
  ToggleFreeViewToolbarItem,
  ToggleMappedFieldsToolbarItem,
  ToggleMappingColumnToolbarItem,
  ToggleMappingPreviewToolbarItem,
  ToggleMappingTableViewToolbarItem,
  ToggleNamespaceTableViewToolbarItem,
  ToggleTypesToolbarItem,
  ToggleUnmappedFieldsToolbarItem,
} from "./toolbarItems";
import {
  toggleMappingPreview as amToggleMappingPreview,
  toggleShowMappedFields as amToggleShowMappedFields,
  toggleShowUnmappedFields as amToggleShowUnmappedFields,
} from "./utils";

export type Views =
  | "ColumnMapper"
  | "MappingTable"
  | "NamespaceTable"
  | "FreeView";

export interface IUseContextToolbarHandlers {
  onImportAtlasFile: (file: File) => void;
  onExportAtlasFile: () => void;
  onResetAtlasmap: () => void;
}

export interface IUseContextToolbarData {
  showImportAtlasFileToolbarItem?: boolean;
  showExportAtlasFileToolbarItem?: boolean;
  showResetToolbarItem?: boolean;

  showColumnMapperViewToolbarItem?: boolean;
  showMappingTableViewToolbarItem?: boolean;
  showNamespaceTableViewToolbarItem?: boolean;
  showFreeViewToolbarItem?: boolean;

  showToggleMappingColumnToolbarItem?: boolean;
  showToggleMappingPreviewToolbarItem?: boolean;
  showToggleTypesToolbarItem?: boolean;
  showToggleMappedFieldsToolbarItem?: boolean;
  showToggleUnmappedFieldsToolbarItem?: boolean;
}

export function useContextToolbar({
  showImportAtlasFileToolbarItem = true,
  showExportAtlasFileToolbarItem = true,
  showResetToolbarItem = true,

  showColumnMapperViewToolbarItem = true,
  showMappingTableViewToolbarItem = true,
  showNamespaceTableViewToolbarItem = true,
  showFreeViewToolbarItem = true,

  showToggleMappingColumnToolbarItem = true,
  showToggleMappingPreviewToolbarItem = true,
  showToggleTypesToolbarItem = true,
  showToggleMappedFieldsToolbarItem = true,
  showToggleUnmappedFieldsToolbarItem = true,

  onImportAtlasFile,
  onExportAtlasFile,
  onResetAtlasmap,
}: IUseContextToolbarData & IUseContextToolbarHandlers) {
  const [activeView, setActiveView] = useState<Views>("ColumnMapper");
  const {
    state: showMappingColumn,
    toggle: toggleShowMappingColumn,
  } = useToggle(false);
  const {
    state: showMappingPreview,
    toggle: toggleShowMappingPreview,
  } = useToggle(false, amToggleMappingPreview);
  const { state: showTypes, toggle: toggleShowTypes } = useToggle(false);
  const { state: showMappedFields, toggle: toggleShowMappedFields } = useToggle(
    true,
    amToggleShowMappedFields,
  );
  const {
    state: showUnmappedFields,
    toggle: toggleShowUnmappedFields,
  } = useToggle(true, amToggleShowUnmappedFields);
  const contextToolbar = useMemo(
    () => (
      <ContextToolbar>
        <ToolbarGroup>
          {showImportAtlasFileToolbarItem && (
            <ImportAtlasFileToolbarItem onFile={onImportAtlasFile} />
          )}
          {showExportAtlasFileToolbarItem && (
            <ExportAtlasFileToolbarItem onClick={onExportAtlasFile} />
          )}
          {showResetToolbarItem && (
            <ResetToolbarItem onClick={onResetAtlasmap} />
          )}
        </ToolbarGroup>
        <ToolbarGroup>
          {showColumnMapperViewToolbarItem && (
            <ToggleColumnMapperViewToolbarItem
              toggled={activeView === "ColumnMapper"}
              onClick={() => setActiveView("ColumnMapper")}
            />
          )}
          {showMappingTableViewToolbarItem && (
            <ToggleMappingTableViewToolbarItem
              toggled={activeView === "MappingTable"}
              onClick={() => setActiveView("MappingTable")}
            />
          )}
          {showFreeViewToolbarItem && (
            <ToggleFreeViewToolbarItem
              toggled={activeView === "FreeView"}
              onClick={() => setActiveView("FreeView")}
            />
          )}
          {showNamespaceTableViewToolbarItem && (
            <ToggleNamespaceTableViewToolbarItem
              toggled={activeView === "NamespaceTable"}
              onClick={() => setActiveView("NamespaceTable")}
            />
          )}
        </ToolbarGroup>
        <ToolbarGroup>
          {showToggleMappingColumnToolbarItem &&
            activeView === "ColumnMapper" && (
              <ToggleMappingColumnToolbarItem
                toggled={showMappingColumn}
                onClick={toggleShowMappingColumn}
              />
            )}
          {showToggleMappingPreviewToolbarItem &&
            (activeView === "ColumnMapper" ||
              activeView === "MappingTable") && (
              <ToggleMappingPreviewToolbarItem
                toggled={showMappingPreview}
                onClick={toggleShowMappingPreview}
              />
            )}
          {showToggleTypesToolbarItem &&
            (activeView === "ColumnMapper" ||
              activeView === "MappingTable") && (
              <ToggleTypesToolbarItem
                toggled={showTypes}
                onClick={toggleShowTypes}
              />
            )}
          {showToggleMappedFieldsToolbarItem &&
            activeView === "ColumnMapper" && (
              <ToggleMappedFieldsToolbarItem
                toggled={showMappedFields}
                onClick={toggleShowMappedFields}
              />
            )}
          {showToggleUnmappedFieldsToolbarItem &&
            activeView === "ColumnMapper" && (
              <ToggleUnmappedFieldsToolbarItem
                toggled={showUnmappedFields}
                onClick={toggleShowUnmappedFields}
              />
            )}
        </ToolbarGroup>
      </ContextToolbar>
    ),
    [
      showImportAtlasFileToolbarItem,
      onImportAtlasFile,
      showExportAtlasFileToolbarItem,
      onExportAtlasFile,
      showResetToolbarItem,
      onResetAtlasmap,
      showColumnMapperViewToolbarItem,
      activeView,
      showMappingTableViewToolbarItem,
      showNamespaceTableViewToolbarItem,
      showFreeViewToolbarItem,
      showToggleMappingColumnToolbarItem,
      showMappingColumn,
      toggleShowMappingColumn,
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
    ],
  );

  return {
    activeView,
    showMappingColumn,
    showMappingPreview,
    showTypes,
    showMappedFields,
    showUnmappedFields,
    contextToolbar,
  };
}
