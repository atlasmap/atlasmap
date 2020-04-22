import React, { FunctionComponent } from "react";
import { FilePicker } from "react-file-picker";

import { Button, ToolbarItem, Tooltip } from "@patternfly/react-core";
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
} from "@patternfly/react-icons";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  toggled: { color: "var(--pf-global--primary-color--100) !important" },
});

export const ImportAtlasFileToolbarItem: FunctionComponent<{
  disabled?: boolean;
  onFile: (file: File) => void;
}> = ({ disabled = false, onFile }) => (
  <ToolbarItem>
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={
        <div>
          Import an AtlasMap mappings catalog file (.adm) or Java archive
          (.jar).
        </div>
      }
    >
      <FilePicker extensions={["adm", "jar"]} onChange={onFile}>
        <Button
          variant={"plain"}
          aria-label="Import mappings"
          isDisabled={disabled}
        >
          <ImportIcon />
        </Button>
      </FilePicker>
    </Tooltip>
  </ToolbarItem>
);

export const ExportAtlasFileToolbarItem: FunctionComponent<{
  disabled?: boolean;
  onClick: () => void;
}> = ({ disabled = false, onClick }) => (
  <ToolbarItem>
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={
        <div>
          Export the current mappings and support files into a catalog (.adm)
          file.
        </div>
      }
    >
      <Button
        variant={"plain"}
        aria-label="Export mappings"
        onClick={onClick}
        isDisabled={disabled}
      >
        <ExportIcon />
      </Button>
    </Tooltip>
  </ToolbarItem>
);

export const ResetToolbarItem: FunctionComponent<{
  disabled?: boolean;
  onClick: () => void;
}> = ({ disabled = false, onClick }) => (
  <ToolbarItem>
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={<div>Reset all mappings and clear all imported documents</div>}
    >
      <Button
        variant={"plain"}
        aria-label="Reset all"
        onClick={onClick}
        isDisabled={disabled}
      >
        <TrashIcon />
      </Button>
    </Tooltip>
  </ToolbarItem>
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
      >
        <MapIcon />
      </Button>
    </Tooltip>
  </ToolbarItem>
);
