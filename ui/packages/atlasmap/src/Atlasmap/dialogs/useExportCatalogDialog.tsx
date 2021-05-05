import React, { ReactElement, useCallback } from "react";

import { ExportCatalogDialog } from "../../UI";
import { useAtlasmap } from "../AtlasmapProvider";
import { useToggle } from "../../Atlasmap/utils";

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
