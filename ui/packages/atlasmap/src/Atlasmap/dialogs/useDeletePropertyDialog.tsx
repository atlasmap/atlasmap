import { ReactElement, useCallback } from "react";

import { useAtlasmap } from "../AtlasmapProvider";
import { useConfirmationDialog } from "./useConfirmationDialog";

export function useDeletePropertyDialog(): [
  ReactElement,
  (propName: string, propScope: string, isSource: boolean) => void,
] {
  const { deleteProperty } = useAtlasmap();
  const [deletePropertyDialog, openDeletePropertyDialog] =
    useConfirmationDialog(
      "Delete property?",
      "Are you sure you want to delete the selected property and remove any associated mapping references?",
    );
  const onDeleteProperty = useCallback(
    (propName: string, propScope: string, isSource: boolean) =>
      openDeletePropertyDialog(() =>
        deleteProperty(propName, propScope, isSource),
      ),
    [deleteProperty, openDeletePropertyDialog],
  );
  return [deletePropertyDialog, onDeleteProperty];
}
