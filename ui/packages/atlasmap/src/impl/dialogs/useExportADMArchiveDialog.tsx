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
import React, { ReactElement, useCallback } from 'react';

import { ExportCatalogDialog } from '../../UI';
import { useAtlasmap } from '../AtlasmapProvider';
import { useToggle } from '../utils';

export function useExportADMArchiveDialog(): [ReactElement, () => void] {
  const { state, toggleOn, toggleOff } = useToggle(false);
  const { exportADMArchiveFile } = useAtlasmap();
  const onExportADMArchiveFile = useCallback(
    (filename: string) => {
      exportADMArchiveFile(filename);
      toggleOff();
    },
    [exportADMArchiveFile, toggleOff],
  );
  const dialog = (
    <ExportCatalogDialog
      isOpen={state}
      onCancel={toggleOff}
      onConfirm={onExportADMArchiveFile}
    />
  );
  return [dialog, toggleOn];
}
