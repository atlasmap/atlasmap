import { ReactElement, useCallback } from "react";

import { useAtlasmap } from "../AtlasmapProvider";
import { useConfirmationDialog } from "./useConfirmationDialog";

export function useDeleteConstantDialog(): [
  ReactElement,
  (constValue: string) => void,
] {
  const { deleteConstant } = useAtlasmap();
  const [
    deleteConstantDialog,
    openDeleteConstantDialog,
  ] = useConfirmationDialog(
    "Delete constant?",
    "Are you sure you want to delete the selected constant and remove any associated mapping references?",
  );
  const onDeleteConstant = useCallback(
    (constValue: string) =>
      openDeleteConstantDialog(() => deleteConstant(constValue)),
    [deleteConstant, openDeleteConstantDialog],
  );
  return [deleteConstantDialog, onDeleteConstant];
}
