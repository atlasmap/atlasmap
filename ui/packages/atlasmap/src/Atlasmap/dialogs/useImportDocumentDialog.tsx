import { useCallback, ReactElement } from "react";

import { useAtlasmap } from "../AtlasmapProvider";
import { useConfirmationDialog } from "./useConfirmationDialog";

export function useImportDocumentDialog(): [
  ReactElement,
  (selectedFile: File, isSource: boolean) => void,
] {
  const { documentExists, importAtlasFile } = useAtlasmap();
  const [importDialog, openImportDialog] = useConfirmationDialog(
    "Overwrite selected document?",
    "Are you sure you want to overwrite the selected document and remove any associated mappings?",
  );
  const onImportDocument = useCallback(
    (selectedFile: File, isSource: boolean) => {
      if (documentExists(selectedFile, isSource)) {
        openImportDialog(() => importAtlasFile(selectedFile, isSource));
      } else {
        importAtlasFile(selectedFile, isSource);
      }
    },
    [documentExists, importAtlasFile, openImportDialog],
  );
  return [importDialog, onImportDocument];
}
