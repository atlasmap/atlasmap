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
import React, { ReactElement, useCallback, useState } from 'react';
import { getCsvParameterOptions } from '@atlasmap/core';
import { useAtlasmap } from '../AtlasmapProvider';
import { useConfirmationDialog } from './useConfirmationDialog';
import { useParametersDialog } from './useParametersDialog';
import { useSpecifyInstanceSchemaDialog } from './useSpecifyInstanceSchemaDialog';

export function useImportDocumentDialog(): [
  ReactElement,
  (selectedFile: File, isSource: boolean) => void,
] {
  const { configModel, documentExists, importInstanceSchema } = useAtlasmap();
  const [importDialog, openImportDialog] = useConfirmationDialog(
    'Confirm document import',
    'A document with the selected name has already been imported into the specified panel. It will appear in addition to the pre-existing document.',
  );
  const [parametersDialog, openParametersDialog] = useParametersDialog(
    'Select CSV Processing Parameters',
  );
  const [defaultSchema, setDefaultSchema] = useState(false);
  const [specifyInstanceSchemaDialog, openSpecifyInstanceSchema] =
    useSpecifyInstanceSchemaDialog(defaultSchema);

  const importFile = useCallback(
    (selectedFile: File, isSource: boolean) => {
      if (selectedFile.name) {
        const userFileSplit = selectedFile.name.split('.');
        const userFileSuffix: string =
          userFileSplit[userFileSplit.length - 1].toUpperCase();
        if (userFileSuffix === 'CSV') {
          openParametersDialog((parameters) => {
            const inspectionParameters: { [key: string]: string } = {};
            for (let parameter of parameters) {
              inspectionParameters[parameter.name] = parameter.value;
            }
            importInstanceSchema(
              selectedFile,
              configModel,
              isSource,
              false,
              inspectionParameters,
            );
          }, getCsvParameterOptions());
          return;
        }

        setDefaultSchema(userFileSuffix === 'XSD' ? true : false);
        openSpecifyInstanceSchema((isSchema: boolean) => {
          importInstanceSchema(selectedFile, configModel, isSource, isSchema);
        });
      }
    },
    [
      configModel,
      importInstanceSchema,
      openParametersDialog,
      openSpecifyInstanceSchema,
    ],
  );

  const onImportDocument = useCallback(
    (selectedFile: File, isSource: boolean) => {
      if (documentExists(selectedFile, isSource)) {
        openImportDialog(() => importFile(selectedFile, isSource));
      } else {
        importFile(selectedFile, isSource);
      }
    },
    [documentExists, importFile, openImportDialog],
  );
  return [
    <>
      {importDialog}
      {parametersDialog}
      {specifyInstanceSchemaDialog}
    </>,
    onImportDocument,
  ];
}
