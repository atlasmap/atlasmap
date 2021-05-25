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
import { Button, Tooltip } from '@patternfly/react-core';
import React, { FunctionComponent, useEffect, useRef } from 'react';

import { ImportIcon } from '@patternfly/react-icons';
import { useFilePicker } from 'react-sage';

export interface IImportActionProps {
  id: string;
  onImport: (selectedFile: File) => void;
}
export const ImportAction: FunctionComponent<IImportActionProps> = ({
  id,
  onImport,
}) => {
  const { files, onClick, HiddenFileInput } = useFilePicker({
    maxFileSize: 1,
  });
  const previouslyUploadedFiles = useRef<File[] | null>(null);

  useEffect(() => {
    if (previouslyUploadedFiles.current !== files) {
      previouslyUploadedFiles.current = files;
      if (files?.length === 1) {
        onImport(files[0]);
      }
    }
  }, [files, onImport]);

  return (
    <Tooltip
      position={'auto'}
      enableFlip={true}
      content={<div>Import instance or schema file</div>}
    >
      <Button
        variant="plain"
        aria-label="Import instance or schema file"
        data-testid={`import-instance-or-schema-file-${id}-button`}
        onClick={onClick}
      >
        <ImportIcon />
        <HiddenFileInput accept={'.json, .xml, .xsd, .csv'} />
      </Button>
    </Tooltip>
  );
};
