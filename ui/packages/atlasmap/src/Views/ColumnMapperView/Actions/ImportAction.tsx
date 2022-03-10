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
import { DocumentType, InspectionType } from '@atlasmap/core';
import {
  Dropdown,
  DropdownItem,
  DropdownToggle,
  Tooltip,
} from '@patternfly/react-core';
import React, { FunctionComponent, useEffect, useRef } from 'react';
import { ImportIcon } from '@patternfly/react-icons';
import { useFilePicker } from 'react-sage';
import { useToggle } from '../../../impl/utils';

export interface IImportActionProps {
  id: string;
  onImport: (
    selectedFile: File,
    docType: DocumentType,
    inspType: InspectionType,
  ) => void;
}

type importDocumentType = {
  documentType: DocumentType;
  inspectionType: InspectionType;
  typeName: string;
  suffix: string;
};

const importCandidate: importDocumentType[] = [
  {
    documentType: DocumentType.JSON,
    inspectionType: InspectionType.SCHEMA,
    typeName: 'JSON schema',
    suffix: '.json',
  },
  {
    documentType: DocumentType.JSON,
    inspectionType: InspectionType.INSTANCE,
    typeName: 'JSON instance',
    suffix: '.json',
  },
  {
    documentType: DocumentType.XML,
    inspectionType: InspectionType.SCHEMA,
    typeName: 'XML schema or AtlasMap XML SchemaSet',
    suffix: '.xml, .xsd',
  },
  {
    documentType: DocumentType.XML,
    inspectionType: InspectionType.INSTANCE,
    typeName: 'XML instance',
    suffix: '.xml',
  },
  {
    documentType: DocumentType.CSV,
    inspectionType: InspectionType.UNKNOWN,
    typeName: 'CSV',
    suffix: '.csv',
  },
  {
    documentType: DocumentType.KAFKA_JSON,
    inspectionType: InspectionType.SCHEMA,
    typeName: 'Kafka Connect JSON schema',
    suffix: '.json, .txt, ""',
  },
  {
    documentType: DocumentType.KAFKA_AVRO,
    inspectionType: InspectionType.SCHEMA,
    typeName: 'Kafka Connect AVRO schema',
    suffix: '.avro, .json, .txt, ""',
  },
];

export const ImportAction: FunctionComponent<IImportActionProps> = ({
  id,
  onImport,
}) => {
  const { state: isOpen, toggle: onToggle, toggleOff } = useToggle(false);
  const {
    files: selectedFiles,
    onClick: triggerFileSelect,
    HiddenFileInput,
  } = useFilePicker({
    maxFileSize: 1,
  });
  const importDocumentTypeIndex = useRef<number | null>(null);

  function setFileTypeAndSelectFile(importDocTypeIndex: number) {
    triggerFileSelect();
    importDocumentTypeIndex.current = importDocTypeIndex;
  }

  let dropdownItems = [];
  let key = '';
  for (let i = 0; i < importCandidate.length; i++) {
    key = 'import-' + i;
    dropdownItems.push(
      <DropdownItem
        icon={<ImportIcon />}
        onClick={() => setFileTypeAndSelectFile(i)}
        data-testid={`import-document-button-` + i}
        key={key}
      >
        {importCandidate[i].typeName}
        <HiddenFileInput
          accept={'.avro, .csv, .json, .txt, .xml, .xsd'}
          multiple={false}
        />
      </DropdownItem>,
    );
  }
  dropdownItems = dropdownItems.filter((f) => f);

  useEffect(() => {
    if (selectedFiles && selectedFiles[0]) {
      let i = importDocumentTypeIndex.current!;
      if (importCandidate[i]) {
        onImport(
          selectedFiles[0],
          importCandidate[i].documentType,
          importCandidate[i].inspectionType,
        );
        importDocumentTypeIndex.current = null;
        toggleOff();
      }
    }
  }, [importDocumentTypeIndex, onImport, selectedFiles, toggleOff]);

  return (
    <Tooltip
      position={'top'}
      enableFlip={true}
      entryDelay={750}
      exitDelay={100}
      content={<div>Import instance or schema file</div>}
    >
      <Dropdown
        toggle={
          <DropdownToggle
            id={id}
            onToggle={onToggle}
            toggleIndicator={ImportIcon}
            data-testid="import-action-toggle"
          ></DropdownToggle>
        }
        position={'left'}
        isOpen={isOpen}
        dropdownItems={dropdownItems}
        isPlain={true}
      />
    </Tooltip>
  );
};
