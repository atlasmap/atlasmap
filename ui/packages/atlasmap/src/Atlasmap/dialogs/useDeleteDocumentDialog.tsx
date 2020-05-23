import { ReactElement, useCallback } from "react";

import { GroupId } from "../../Views";
import { useAtlasmap } from "../AtlasmapProvider";
import { useConfirmationDialog } from "./useConfirmationDialog";

export function useDeleteDocumentDialog(): [
  ReactElement,
  (id: GroupId, isSource: boolean) => void,
] {
  const { deleteAtlasFile } = useAtlasmap();
  const [deleteDialog, openDeleteDialog] = useConfirmationDialog(
    "Delete selected document?",
    "Are you sure you want to delete the selected document and remove any associated mappings?",
  );
  const onDeleteDocument = useCallback(
    (id: GroupId, isSource: boolean) =>
      openDeleteDialog(() => deleteAtlasFile(id, isSource)),
    [deleteAtlasFile, openDeleteDialog],
  );
  return [deleteDialog, onDeleteDocument];
}
