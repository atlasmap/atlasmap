import React, { FunctionComponent, useEffect, useRef } from "react";
import { useFilePicker } from "react-sage";

import {
  Button,
  ToolbarItem,
  Tooltip,
  Dropdown,
  DropdownToggle,
  DropdownItem,
  DropdownSeparator,
  DropdownItemIcon,
} from "@patternfly/react-core";
import {
  BezierCurveIcon,
  CodeIcon,
  ExportIcon,
  EyeIcon,
  ImportIcon,
  InfoIcon,
  MapIcon,
  MapMarkedIcon,
  PficonDragdropIcon,
  TableIcon,
  ColumnsIcon,
  TrashIcon,
  CaretDownIcon,
} from "@patternfly/react-icons";
import { css, StyleSheet } from "@patternfly/react-styles";
import { useToggle } from "../UI";

const styles = StyleSheet.create({
  toggled: { color: "var(--pf-global--primary-color--100) !important" },
});

export interface IAtlasmapToolbarItemProps {
  showImportAtlasFileToolbarItem: boolean;
  showImportJarFileToolbarItem: boolean;
  showExportAtlasFileToolbarItem: boolean;
  showResetToolbarItem: boolean;
  onImportAtlasFile: (file: File) => void;
  onImportJarFile: (file: File) => void;
  onExportAtlasFile: () => void;
  onResetAtlasmap: () => void;
}
export const AtlasmapToolbarItem: FunctionComponent<IAtlasmapToolbarItemProps> = ({
  showImportAtlasFileToolbarItem,
  showImportJarFileToolbarItem,
  showExportAtlasFileToolbarItem,
  showResetToolbarItem,
  onImportAtlasFile,
  onImportJarFile,
  onExportAtlasFile,
  onResetAtlasmap,
}) => {
  const { state: isOpen, toggle: onToggle, toggleOff } = useToggle(false);
  const runAndClose = (cb: (...args: any[]) => any) => {
    return (...args: any[]) => {
      cb(...args);
      toggleOff();
    };
  };
  const dropdownItems = [
    showImportAtlasFileToolbarItem && (
      <ImportAtlasFileToolbarItem
        onFile={runAndClose(onImportAtlasFile)}
        key="import-catalog"
      />
    ),
    showImportJarFileToolbarItem && (
      <ImportJarFileToolbarItem
        onFile={runAndClose(onImportJarFile)}
        key="import-java-archive"
      />
    ),
    (showImportAtlasFileToolbarItem || showImportJarFileToolbarItem) && (
      <DropdownSeparator key="import-separator" />
    ),
    showExportAtlasFileToolbarItem && (
      <ExportAtlasFileToolbarItem
        onClick={runAndClose(onExportAtlasFile)}
        key={"export-catalog"}
      />
    ),
    showExportAtlasFileToolbarItem && (
      <DropdownSeparator key="export-separator" />
    ),
    showResetToolbarItem && (
      <ResetToolbarItem
        onClick={runAndClose(onResetAtlasmap)}
        key="reset-catalog"
      />
    ),
  ].filter((f) => f);
  return (
    <ToolbarItem>
      <Dropdown
        toggle={
          <DropdownToggle
            id="atlasmap-toggle"
            onToggle={onToggle}
            iconComponent={CaretDownIcon}
            data-testid="atlasmap-menu-button"
          >
            AtlasMap
          </DropdownToggle>
        }
        isOpen={isOpen}
        dropdownItems={dropdownItems}
        isPlain={true}
      />
    </ToolbarItem>
  );
};

export const ImportAtlasFileToolbarItem: FunctionComponent<{
  onFile: (file: File) => void;
}> = ({ onFile }) => {
  const { files, onClick, HiddenFileInput } = useFilePicker({
    maxFileSize: 1,
  });
  const previouslyUploadedFiles = useRef<File[] | null>(null);

  useEffect(() => {
    if (previouslyUploadedFiles.current !== files) {
      previouslyUploadedFiles.current = files;
      if (files?.length === 1) {
        previouslyUploadedFiles.current = null;
        onFile(files[0]);
      }
    }
  }, [files, onFile]);

  return (
    <DropdownItem onClick={onClick} data-testid="import-mappings-button">
      <DropdownItemIcon>
        <ImportIcon />
      </DropdownItemIcon>
      Import a catalog (.adm)
      <HiddenFileInput accept=".adm" multiple={false} />
    </DropdownItem>
  );
};

export const ImportJarFileToolbarItem: FunctionComponent<{
  onFile: (file: File) => void;
}> = ({ onFile }) => {
  const { files, onClick, HiddenFileInput } = useFilePicker({
    maxFileSize: 1,
  });
  const previouslyUploadedFiles = useRef<File[] | null>(null);

  useEffect(() => {
    if (previouslyUploadedFiles.current !== files) {
      previouslyUploadedFiles.current = files;
      if (files?.length === 1) {
        previouslyUploadedFiles.current = null;
        onFile(files[0]);
      }
    }
  }, [files, onFile]);

  return (
    <DropdownItem onClick={onClick} data-testid="import-archive-button">
      <DropdownItemIcon>
        <ImportIcon />
      </DropdownItemIcon>
      Import a Java archive (.jar)
      <HiddenFileInput accept=".jar" multiple={false} />
    </DropdownItem>
  );
};

export const ExportAtlasFileToolbarItem: FunctionComponent<{
  onClick: () => void;
}> = ({ onClick }) => (
  <DropdownItem onClick={onClick} data-testid="export-mappings-button">
    <DropdownItemIcon>
      <ExportIcon />
    </DropdownItemIcon>
    Export the current mappings and support files into a catalog (.adm)
  </DropdownItem>
);

export const ResetToolbarItem: FunctionComponent<{
  onClick: () => void;
}> = ({ onClick }) => (
  <DropdownItem onClick={onClick} data-testid="reset-all-button">
    <DropdownItemIcon>
      <TrashIcon />
    </DropdownItemIcon>
    Reset all mappings and clear all imported documents
  </DropdownItem>
);

export const ToggleMappingColumnToolbarItem: FunctionComponent<{
  toggled: boolean;
  disabled?: boolean;
  onClick: () => void;
}> = ({ toggled, disabled = false, onClick }) => (
  <ToolbarItem>
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={<div>Show mapping column</div>}
    >
      <Button
        variant={"plain"}
        aria-label="Show/hide mapping column"
        onClick={onClick}
        isDisabled={disabled}
        className={css(toggled && styles.toggled)}
        data-testid="show-hide-mapping-column-button"
      >
        <BezierCurveIcon />
      </Button>
    </Tooltip>
  </ToolbarItem>
);

export const ToggleColumnMapperViewToolbarItem: FunctionComponent<{
  toggled: boolean;
  disabled?: boolean;
  onClick: () => void;
}> = ({ toggled, disabled = false, onClick }) => (
  <ToolbarItem>
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={<div>Show column mapper</div>}
    >
      <Button
        variant={"plain"}
        aria-label="Show column mapper"
        onClick={onClick}
        isDisabled={disabled}
        className={css(toggled && styles.toggled)}
        data-testid="show-column-mapper-button"
      >
        <ColumnsIcon />
      </Button>
    </Tooltip>
  </ToolbarItem>
);

export const ToggleMappingTableViewToolbarItem: FunctionComponent<{
  toggled: boolean;
  disabled?: boolean;
  onClick: () => void;
}> = ({ toggled, disabled = false, onClick }) => (
  <ToolbarItem>
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={<div>Show mapping table</div>}
    >
      <Button
        variant={"plain"}
        aria-label="Show/hide mapping table"
        onClick={onClick}
        isDisabled={disabled}
        className={css(toggled && styles.toggled)}
        data-testid="show-hide-mapping-table-button"
      >
        <TableIcon />
      </Button>
    </Tooltip>
  </ToolbarItem>
);

export const ToggleNamespaceTableViewToolbarItem: FunctionComponent<{
  toggled: boolean;
  disabled?: boolean;
  onClick: () => void;
}> = ({ toggled, disabled = false, onClick }) => (
  <ToolbarItem>
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={<div>Show namespace table</div>}
    >
      <Button
        variant={"plain"}
        aria-label="Show/hide namespace table"
        onClick={onClick}
        isDisabled={disabled}
        className={css(toggled && styles.toggled)}
        data-testid="show-hide-namespace-table-button"
      >
        <CodeIcon />
      </Button>
    </Tooltip>
  </ToolbarItem>
);

export const ToggleFreeViewToolbarItem: FunctionComponent<{
  toggled: boolean;
  disabled?: boolean;
  onClick: () => void;
}> = ({ toggled, disabled = false, onClick }) => (
  <ToolbarItem>
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={<div>Show free view</div>}
    >
      <Button
        variant={"plain"}
        aria-label="Show/hide free view"
        onClick={onClick}
        isDisabled={disabled}
        className={css(toggled && styles.toggled)}
        data-testid="show-hide-free-view-button"
      >
        <PficonDragdropIcon />
      </Button>
    </Tooltip>
  </ToolbarItem>
);

export const ToggleMappingPreviewToolbarItem: FunctionComponent<{
  toggled: boolean;
  disabled?: boolean;
  onClick: () => void;
}> = ({ toggled, disabled = false, onClick }) => (
  <ToolbarItem>
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={<div>Show/hide mapping preview</div>}
    >
      <Button
        variant={"plain"}
        aria-label="Show/hide mapping preview"
        onClick={onClick}
        isDisabled={disabled}
        className={css(toggled && styles.toggled)}
        data-testid="show-hide-mapping-preview-button"
      >
        <EyeIcon />
      </Button>
    </Tooltip>
  </ToolbarItem>
);

export const ToggleTypesToolbarItem: FunctionComponent<{
  toggled: boolean;
  disabled?: boolean;
  onClick: () => void;
}> = ({ toggled, disabled = false, onClick }) => (
  <ToolbarItem>
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={<div>Show/hide types</div>}
    >
      <Button
        variant={"plain"}
        aria-label="Show/hide types"
        onClick={onClick}
        isDisabled={disabled}
        className={css(toggled && styles.toggled)}
        data-testid="show-hide-types-button"
      >
        <InfoIcon />
      </Button>
    </Tooltip>
  </ToolbarItem>
);

export const ToggleMappedFieldsToolbarItem: FunctionComponent<{
  toggled: boolean;
  disabled?: boolean;
  onClick: () => void;
}> = ({ toggled, disabled = false, onClick }) => (
  <ToolbarItem>
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={<div>Show/hide mapped fields</div>}
    >
      <Button
        variant={"plain"}
        aria-label="Show/hide mapped fields"
        onClick={onClick}
        isDisabled={disabled}
        className={css(toggled && styles.toggled)}
        data-testid="show-hide-mapped-fields-button"
      >
        <MapMarkedIcon />
      </Button>
    </Tooltip>
  </ToolbarItem>
);

export const ToggleUnmappedFieldsToolbarItem: FunctionComponent<{
  toggled: boolean;
  disabled?: boolean;
  onClick: () => void;
}> = ({ toggled, disabled = false, onClick }) => (
  <ToolbarItem>
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={<div>Show/hide unmapped fields</div>}
    >
      <Button
        variant={"plain"}
        aria-label="Show/hide unmapped fields"
        onClick={onClick}
        isDisabled={disabled}
        className={css(toggled && styles.toggled)}
        data-testid="show-hide-unmapped-fields-button"
      >
        <MapIcon />
      </Button>
    </Tooltip>
  </ToolbarItem>
);
