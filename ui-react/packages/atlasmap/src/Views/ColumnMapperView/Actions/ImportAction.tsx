import React, { FunctionComponent } from "react";
import { FilePicker } from "react-file-picker";

import { Tooltip, Button } from "@patternfly/react-core";
import { ImportIcon } from "@patternfly/react-icons";

export interface IImportActionProps {
  id: string;
  onImport: (selectedFile: File) => void;
}
export const ImportAction: FunctionComponent<IImportActionProps> = ({
  id,
  onImport,
}) => (
  <Tooltip
    position={"auto"}
    enableFlip={true}
    content={<div>Import instance or schema file</div>}
  >
    <FilePicker
      extensions={["json", "xml", "xsd", "csv"]}
      onChange={(selectedFile: File) => onImport(selectedFile)}
      onError={(errMsg: any) => console.error(errMsg)}
    >
      <Button
        variant="plain"
        aria-label="Import instance or schema file"
        data-testid={`import-instance-or-schema-file-${id}-button`}
      >
        <ImportIcon />
      </Button>
    </FilePicker>
  </Tooltip>
);
