import { ReactElement, useCallback } from "react";

import { IAtlasmapMapping } from "../../Views";
import { useAtlasmap } from "../AtlasmapProvider";
import { useConfirmationDialog } from "./useConfirmationDialog";

export function useDeleteMappingDialog(): [
  ReactElement,
  (mapping: IAtlasmapMapping) => void,
] {
  const { removeMapping, deselectMapping } = useAtlasmap();
  const [deleteMappingDialog, openDeleteMappingDialog] = useConfirmationDialog(
    "Remove Mapping?",
    "Are you sure you want to remove the current mapping?",
  );
  const onDeleteMapping = useCallback(
    (mapping: IAtlasmapMapping) => {
      openDeleteMappingDialog(() => {
        removeMapping(mapping.mapping);
        deselectMapping();
      });
    },
    [deselectMapping, openDeleteMappingDialog, removeMapping],
  );

  return [deleteMappingDialog, onDeleteMapping];
}
