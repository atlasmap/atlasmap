import React, { useMemo, useState } from "react";

import { ToolbarGroup } from "@patternfly/react-core";

import { ContextToolbar } from "../Layout";
import { useToggle } from "../UI";
import {
  ToggleAllLinksToolbarItem,
  ToggleColumnMapperViewToolbarItem,
  ToggleFreeViewToolbarItem,
  ToggleMappedFieldsToolbarItem,
  ToggleMappingColumnToolbarItem,
  ToggleMappingPreviewToolbarItem,
  ToggleMappingTableViewToolbarItem,
  ToggleNamespaceTableViewToolbarItem,
  ToggleTransformationApproachToolbarItem,
  ToggleTypesToolbarItem,
  ToggleUnmappedFieldsToolbarItem,
  AtlasmapToolbarItem,
} from "./toolbarItems";
import { useAtlasmap } from "./AtlasmapProvider";

export type Views =
  | "ColumnMapper"
  | "MappingTable"
  | "NamespaceTable"
  | "FreeView"
  | "TransformationApproach";

export interface IUseContextToolbarHandlers {
  onImportAtlasFile: (file: File) => void;
  onImportJarFile: (file: File) => void;
  onExportAtlasFile: () => void;
  onResetAtlasmap: () => void;
}

export interface IUseContextToolbarData {
  showImportAtlasFileToolbarItem?: boolean;
  showImportJarFileToolbarItem?: boolean;
  showExportAtlasFileToolbarItem?: boolean;
  showResetToolbarItem?: boolean;

  showColumnMapperViewToolbarItem?: boolean;
  showMappingTableViewToolbarItem?: boolean;
  showNamespaceTableViewToolbarItem?: boolean;
  showTransformationApproachViewToolbarItem?: boolean;
  showFreeViewToolbarItem?: boolean;

  showToggleMappingColumnToolbarItem?: boolean;
  showToggleMappingPreviewToolbarItem?: boolean;
  showToggleTypesToolbarItem?: boolean;
  showToggleMappedFieldsToolbarItem?: boolean;
  showToggleUnmappedFieldsToolbarItem?: boolean;
  showToggleAllLinksToolbarItem?: boolean;
}

export function useContextToolbar({
  showImportAtlasFileToolbarItem = true,
  showImportJarFileToolbarItem = true,
  showExportAtlasFileToolbarItem = true,
  showResetToolbarItem = true,

  showColumnMapperViewToolbarItem = true,
  showMappingTableViewToolbarItem = true,
  showNamespaceTableViewToolbarItem = true,
  showTransformationApproachViewToolbarItem = true,
  showFreeViewToolbarItem = false,

  showToggleMappingColumnToolbarItem = false,
  showToggleMappingPreviewToolbarItem = true,
  showToggleTypesToolbarItem = true,
  showToggleMappedFieldsToolbarItem = true,
  showToggleUnmappedFieldsToolbarItem = true,
  showToggleAllLinksToolbarItem = true,

  onImportAtlasFile,
  onImportJarFile,
  onExportAtlasFile,
  onResetAtlasmap,
}: IUseContextToolbarData & IUseContextToolbarHandlers) {
  const {
    toggleMappingPreview: amToggleMappingPreview,
    toggleShowMappedFields: amToggleShowMappedFields,
    toggleShowUnmappedFields: amToggleShowUnmappedFields,
  } = useAtlasmap();

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
  const { state: showAllLinks, toggle: toggleShowAllLinks } = useToggle(
    false,
    () => !showAllLinks,
  );

  const contextToolbar = useMemo(
    () => (
      <ContextToolbar>
        {(showImportAtlasFileToolbarItem ||
          showImportJarFileToolbarItem ||
          showExportAtlasFileToolbarItem ||
          showResetToolbarItem) && (
          <ToolbarGroup>
            <AtlasmapToolbarItem
              showImportAtlasFileToolbarItem={showImportAtlasFileToolbarItem}
              showImportJarFileToolbarItem={showImportJarFileToolbarItem}
              showExportAtlasFileToolbarItem={showExportAtlasFileToolbarItem}
              showResetToolbarItem={showResetToolbarItem}
              onImportAtlasFile={onImportAtlasFile}
              onImportJarFile={onImportJarFile}
              onExportAtlasFile={onExportAtlasFile}
              onResetAtlasmap={onResetAtlasmap}
            />
          </ToolbarGroup>
        )}
        {(showColumnMapperViewToolbarItem ||
          showMappingTableViewToolbarItem ||
          showTransformationApproachViewToolbarItem ||
          showFreeViewToolbarItem ||
          showNamespaceTableViewToolbarItem) && (
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
            {showTransformationApproachViewToolbarItem && (
              <ToggleTransformationApproachToolbarItem
                toggled={activeView === "TransformationApproach"}
                onClick={() => setActiveView("TransformationApproach")}
              />
            )}
          </ToolbarGroup>
        )}
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
              activeView === "MappingTable" ||
              activeView === "TransformationApproach") && (
              <ToggleMappingPreviewToolbarItem
                toggled={showMappingPreview}
                onClick={toggleShowMappingPreview}
              />
            )}
          {showToggleTypesToolbarItem &&
            (activeView === "ColumnMapper" ||
              activeView === "MappingTable" ||
              activeView === "TransformationApproach") && (
              <ToggleTypesToolbarItem
                toggled={showTypes}
                onClick={toggleShowTypes}
              />
            )}
          {showToggleMappedFieldsToolbarItem &&
            (activeView === "ColumnMapper" ||
              activeView === "TransformationApproach") && (
              <ToggleMappedFieldsToolbarItem
                toggled={showMappedFields}
                onClick={toggleShowMappedFields}
              />
            )}
          {showToggleUnmappedFieldsToolbarItem &&
            (activeView === "ColumnMapper" ||
              activeView === "TransformationApproach") && (
              <ToggleUnmappedFieldsToolbarItem
                toggled={showUnmappedFields}
                onClick={toggleShowUnmappedFields}
              />
            )}
          {showToggleAllLinksToolbarItem &&
            activeView === "TransformationApproach" && (
              <ToggleAllLinksToolbarItem
                toggled={showAllLinks}
                onClick={toggleShowAllLinks}
              />
            )}
        </ToolbarGroup>
      </ContextToolbar>
    ),
    [
      showImportAtlasFileToolbarItem,
      showImportJarFileToolbarItem,
      showExportAtlasFileToolbarItem,
      showResetToolbarItem,
      onImportAtlasFile,
      onImportJarFile,
      onExportAtlasFile,
      onResetAtlasmap,
      showColumnMapperViewToolbarItem,
      showMappingTableViewToolbarItem,
      showTransformationApproachViewToolbarItem,
      showFreeViewToolbarItem,
      showNamespaceTableViewToolbarItem,
      activeView,
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
      showToggleAllLinksToolbarItem,
      showAllLinks,
      toggleShowAllLinks,
    ],
  );

  return {
    activeView,
    showMappingColumn,
    showMappingPreview,
    showTypes,
    showMappedFields,
    showUnmappedFields,
    showAllLinks,
    contextToolbar,
  };
}
