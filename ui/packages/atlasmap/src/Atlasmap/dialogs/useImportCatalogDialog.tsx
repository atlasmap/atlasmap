import { ReactElement, useCallback } from "react";

import { useAtlasmap } from "../AtlasmapProvider";
import { useConfirmationDialog } from "./useConfirmationDialog";

export function useImportCatalogDialog(): [ReactElement, (file: File) => void] {
  const { importAtlasFile } = useAtlasmap();
  const [ImportCatalogDialog, openImportCatalogDialog] = useConfirmationDialog(
    "Import catalog?",
    "Importing a new catalog will discard all unsaved changes. To save the current catalog, use the Export feature.",
  );
  const onImportCatalog = useCallback(
    (file: File) => openImportCatalogDialog(() => importAtlasFile(file, false)),
    [importAtlasFile, openImportCatalogDialog],
  );
  return [ImportCatalogDialog, onImportCatalog];
}
