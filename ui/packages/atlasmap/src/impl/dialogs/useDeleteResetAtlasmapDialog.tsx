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

import { useAtlasmap } from '../AtlasmapProvider';
import { useConfirmationDialog } from './useConfirmationDialog';

export function useDelDocsAndMappingsAtlasmapDialog(): [
  ReactElement,
  () => void,
] {
  const { delDocsAndMappingsAtlasmap } = useAtlasmap();
  const [delDocsAndMappingsDialog, openDelDocsAndMappingsDialog] =
    useConfirmationDialog(
      'Delete All Documents and Mappings?',
      'Are you sure you want to delete all mappings and all imported documents?',
    );
  const onDeleteDocsAndMappings = useCallback(
    () => openDelDocsAndMappingsDialog(delDocsAndMappingsAtlasmap),
    [openDelDocsAndMappingsDialog, delDocsAndMappingsAtlasmap],
  );

  return [delDocsAndMappingsDialog, onDeleteDocsAndMappings];
}

export function useDeleteLibrariesAtlasmapDialog(): [ReactElement, () => void] {
  const { deleteLibrariesAtlasmap } = useAtlasmap();
  const [deleteLibrariesDialog, openDeleteLibrariesDialog] =
    useConfirmationDialog(
      'Delete All Java Libraries?',
      'Are you sure you want to delete all imported Java libraries?',
    );
  const onDeleteLibraries = useCallback(
    () => openDeleteLibrariesDialog(deleteLibrariesAtlasmap),
    [openDeleteLibrariesDialog, deleteLibrariesAtlasmap],
  );

  return [deleteLibrariesDialog, onDeleteLibraries];
}

export function useDeleteMappingsAtlasmapDialog(): [ReactElement, () => void] {
  const { deleteMappingsAtlasmap } = useAtlasmap();
  const [deleteMappingsDialog, openDeleteMappingsDialog] =
    useConfirmationDialog(
      'Delete All Mappings?',
      'Are you sure you want to delete all mappings?',
    );
  const onDeleteMappings = useCallback(
    () => openDeleteMappingsDialog(deleteMappingsAtlasmap),
    [openDeleteMappingsDialog, deleteMappingsAtlasmap],
  );

  return [deleteMappingsDialog, onDeleteMappings];
}

export function useDeleteResetAtlasmapDialog(): [ReactElement, () => void] {
  const { resetAtlasmap } = useAtlasmap();
  const [resetDialog, openResetDialog] = useConfirmationDialog(
    'Reset All Mappings and Imports?',
    'Are you sure you want to reset all libraries, mappings and clear all imported documents?',
  );
  const onResetAtlasmap = useCallback(
    () => openResetDialog(resetAtlasmap),
    [openResetDialog, resetAtlasmap],
  );

  return [resetDialog, onResetAtlasmap];
}
