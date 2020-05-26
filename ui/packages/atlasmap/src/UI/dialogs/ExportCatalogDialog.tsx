import React, { FunctionComponent, useState } from "react";

import {
  Form,
  FormGroup,
  InputGroup,
  InputGroupText,
  TextInput,
} from "@patternfly/react-core";

import {
  ConfirmationDialog,
  IConfirmationDialogProps,
} from "./ConfirmationDialog";

export interface IExportCatalogDialogProps {
  isOpen: IConfirmationDialogProps["isOpen"];
  onCancel: IConfirmationDialogProps["onCancel"];
  onConfirm: (filename: string) => void;
}
export const ExportCatalogDialog: FunctionComponent<IExportCatalogDialogProps> = ({
  isOpen,
  onCancel,
  onConfirm,
}) => {
  const defaultCatalogName = "atlasmap-mapping";
  const [filename, setFilename] = useState(defaultCatalogName);
  const handleOnConfirm =
    filename.length > 0 ? () => onConfirm(filename) : undefined;
  return (
    <ConfirmationDialog
      title={"Export Mappings and Documents."}
      onCancel={onCancel}
      onConfirm={handleOnConfirm}
      isOpen={isOpen}
    >
      <Form>
        <FormGroup
          label={"Please enter a name for your exported catalog file"}
          fieldId={"filename"}
          isRequired={true}
        >
          <InputGroup>
            <TextInput
              value={filename}
              onChange={setFilename}
              id={"filename"}
              isRequired={true}
              autoFocus={true}
              data-testid={"export-catalog-dialog-text-input"}
            />
            <InputGroupText>.adm</InputGroupText>
          </InputGroup>
        </FormGroup>
      </Form>
    </ConfirmationDialog>
  );
};
