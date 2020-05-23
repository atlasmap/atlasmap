import { ReactElement, useCallback } from "react";

import { useAtlasmap } from "../AtlasmapProvider";
import { useConfirmationDialog } from "./useConfirmationDialog";

export function useToggleExpressionModeDialog(): [ReactElement, () => void] {
  const { mappingHasSourceCollection, toggleExpressionMode } = useAtlasmap();
  const [
    toggleExpressionModeDialog,
    openToggleExpressionModeDialog,
  ] = useConfirmationDialog(
    "Disable Expression?",
    "If you disable an expression with a source collection, all source fields will be removed from the mapping.  Proceed with expression disable?",
  );
  const onToggleExpressionMode = useCallback((): void => {
    if (mappingHasSourceCollection()) {
      openToggleExpressionModeDialog(toggleExpressionMode);
    } else {
      toggleExpressionMode();
    }
  }, [
    mappingHasSourceCollection,
    openToggleExpressionModeDialog,
    toggleExpressionMode,
  ]);
  return [toggleExpressionModeDialog, onToggleExpressionMode];
}
