import { ReactElement, useCallback } from "react";

import { useAtlasmap } from "../AtlasmapProvider";
import { useConfirmationDialog } from "./useConfirmationDialog";

export function useDeletePropertyDialog(): [
  ReactElement,
  (propName: string, isSource: boolean) => void,
] {
  const { deleteProperty } = useAtlasmap();
  const [
    deletePropertyDialog,
    openDeletePropertyDialog,
  ] = useConfirmationDialog(
    "Delete property?",
    "Are you sure you want to delete the selected property and remove any associated mapping references?",
  );
  const onDeleteProperty = useCallback(
    (propName: string, isSource: boolean) =>
      openDeletePropertyDialog(() => deleteProperty(propName, isSource)),
    [deleteProperty, openDeletePropertyDialog],
  );
  return [deletePropertyDialog, onDeleteProperty];
}
