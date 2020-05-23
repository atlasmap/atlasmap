import React, { FunctionComponent, useRef, useEffect } from "react";
import { useFilePicker } from "react-sage";

import { Tooltip, Button } from "@patternfly/react-core";
import { ImportIcon } from "@patternfly/react-icons";

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
      position={"auto"}
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
        <HiddenFileInput accept={".json, .xml, .xsd, .csv"} />
      </Button>
    </Tooltip>
  );
};
