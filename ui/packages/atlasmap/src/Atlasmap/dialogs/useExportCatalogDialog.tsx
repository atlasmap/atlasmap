import React, { useCallback, ReactElement } from "react";

import { useToggle, ExportCatalogDialog } from "../../UI";
import { useAtlasmap } from "../AtlasmapProvider";

export function useExportCatalogDialog(): [ReactElement, () => void] {
  const { state, toggleOn, toggleOff } = useToggle(false);
  const { exportAtlasFile } = useAtlasmap();
  const onExportAtlasFile = useCallback(
    (filename: string) => {
      exportAtlasFile(filename);
      toggleOff();
    },
    [exportAtlasFile, toggleOff],
  );
  const dialog = (
    <ExportCatalogDialog
      isOpen={state}
      onCancel={toggleOff}
      onConfirm={onExportAtlasFile}
    />
  );
  return [dialog, toggleOn];
}
