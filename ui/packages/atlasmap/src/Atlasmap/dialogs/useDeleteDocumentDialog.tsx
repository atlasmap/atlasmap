/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import { ReactElement, useCallback } from 'react';

import { GroupId } from '../../Views';
import { useAtlasmap } from '../AtlasmapProvider';
import { useConfirmationDialog } from './useConfirmationDialog';

export function useDeleteDocumentDialog(): [
  ReactElement,
  (id: GroupId, isSource: boolean) => void,
] {
  const { deleteAtlasFile } = useAtlasmap();
  const [deleteDialog, openDeleteDialog] = useConfirmationDialog(
    'Delete selected document?',
    'Are you sure you want to delete the selected document and remove any associated mappings?',
  );
  const onDeleteDocument = useCallback(
    (id: GroupId, isSource: boolean) =>
      openDeleteDialog(() => deleteAtlasFile(id, isSource)),
    [deleteAtlasFile, openDeleteDialog],
  );
  return [deleteDialog, onDeleteDocument];
}
