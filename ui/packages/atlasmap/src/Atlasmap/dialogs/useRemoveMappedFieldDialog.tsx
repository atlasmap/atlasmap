import { ReactElement, useCallback } from "react";

import { useConfirmationDialog } from "./useConfirmationDialog";

export function useRemoveMappedFieldDialog(): [
  ReactElement,
  (cb: () => void) => void,
] {
  const [
    removeMappedFieldDialog,
    openRemoveMappedFieldDialog,
  ] = useConfirmationDialog(
    "Remove field?",
    "Are you sure you want to remove this field?",
  );
  const onRemoveMappedField = useCallback(
    (removeMappedField: () => void) =>
      openRemoveMappedFieldDialog(removeMappedField),
    [openRemoveMappedFieldDialog],
  );
  return [removeMappedFieldDialog, onRemoveMappedField];
}
