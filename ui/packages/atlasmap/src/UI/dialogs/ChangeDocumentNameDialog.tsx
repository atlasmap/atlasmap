import React, {
  FunctionComponent,
  useState,
  useCallback,
  useEffect,
} from "react";

import {
  ConfirmationDialog,
  IConfirmationDialogProps,
} from "./ConfirmationDialog";
import { TextInput } from "@patternfly/react-core";

export interface IDocumentName {
  id: string;
  name: string;
  isSource: boolean;
}
export interface IChangeDocumentNameDialogProps {
  id?: string;
  name?: string;
  isSource?: boolean;
  isOpen: IConfirmationDialogProps["isOpen"];
  onCancel: IConfirmationDialogProps["onCancel"];
  onConfirm: (updatedDocNameInfo: IDocumentName) => void;
}
export const ChangeDocumentNameDialog: FunctionComponent<IChangeDocumentNameDialogProps> = ({
  id,
  name: initialName = "",
  isSource,
  isOpen,
  onCancel,
  onConfirm,
}) => {
  const [documentName, setDocumentName] = useState(initialName);

  const reset = useCallback(() => {
    setDocumentName(initialName);
  }, [initialName]);

  const handleOnConfirm = useCallback(() => {
    if (id && isSource !== undefined) {
      onConfirm({ id: id, name: documentName, isSource: isSource });
    }
    reset();
  }, [documentName, id, isSource, onConfirm, reset]);

  const handleOnCancel = useCallback(() => {
    onCancel();
    reset();
  }, [onCancel, reset]);

  function handleOnNameChange(name: string) {
    setDocumentName(name);
  }

  // resync the internal state to the values passed in as props
  useEffect(reset, [reset]);

  return (
    <ConfirmationDialog
      title={"Change selected document name?"}
      onCancel={handleOnCancel}
      onConfirm={handleOnConfirm}
      isOpen={isOpen}
    >
      <TextInput
        value={documentName}
        onChange={(value) => handleOnNameChange(value)}
        id={id}
        name={documentName}
        data-testid={id + "-parameter-text-input"}
      />
    </ConfirmationDialog>
  );
};
