import { ReactElement, useCallback } from "react";

import { useAtlasmap } from "../AtlasmapProvider";
import { useConfirmationDialog } from "./useConfirmationDialog";

export function useResetAtlasmapDialog(): [ReactElement, () => void] {
  const { resetAtlasmap } = useAtlasmap();
  const [resetDialog, openResetDialog] = useConfirmationDialog(
    "Reset All Mappings and Imports?",
    "Are you sure you want to reset all mappings and clear all imported documents?",
  );
  const onResetAtlasmap = useCallback(() => openResetDialog(resetAtlasmap), [
    openResetDialog,
    resetAtlasmap,
  ]);

  return [resetDialog, onResetAtlasmap];
}
