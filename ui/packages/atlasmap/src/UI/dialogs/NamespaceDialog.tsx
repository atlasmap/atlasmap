import React, {
  FunctionComponent,
  useState,
  useEffect,
  useCallback,
} from "react";

import { Form, FormGroup, TextInput, Checkbox } from "@patternfly/react-core";

import {
  ConfirmationDialog,
  IConfirmationDialogProps,
} from "./ConfirmationDialog";

export interface INamespace {
  alias: string;
  uri: string;
  locationUri: string;
  targetNamespace: boolean;
}

export interface INamespaceDialogProps {
  title: string;
  alias?: string;
  uri?: string;
  locationUri?: string;
  targetNamespace?: boolean;
  isOpen: IConfirmationDialogProps["isOpen"];
  onCancel: IConfirmationDialogProps["onCancel"];
  onConfirm: (namespace: INamespace) => void;
}
export const NamespaceDialog: FunctionComponent<INamespaceDialogProps> = ({
  title,
  alias: initialAlias = "",
  uri: initialUri = "",
  locationUri: initialLocationUri = "",
  targetNamespace: initialTargetNamespace = false,
  isOpen,
  onCancel,
  onConfirm,
}) => {
  const [alias, setAlias] = useState(initialAlias);
  const [uri, setUri] = useState(initialUri);
  const [locationUri, setLocationUri] = useState(initialLocationUri);
  const [targetNamespace, setTargetNamespace] = useState(
    initialTargetNamespace,
  );

  const reset = useCallback(() => {
    setAlias(initialAlias);
    setUri(initialUri);
    setLocationUri(initialLocationUri);
    setTargetNamespace(initialTargetNamespace);
  }, [initialAlias, initialLocationUri, initialTargetNamespace, initialUri]);

  const handleOnConfirm = useCallback(() => {
    onConfirm({ alias, uri, locationUri, targetNamespace });
    reset();
  }, [alias, locationUri, onConfirm, reset, targetNamespace, uri]);

  const handleOnCancel = useCallback(() => {
    onCancel();
    reset();
  }, [onCancel, reset]);

  // make sure to resync the internal state to the values passed in as props
  useEffect(reset, [reset]);

  return (
    <ConfirmationDialog
      title={title}
      onCancel={handleOnCancel}
      onConfirm={alias.length > 0 ? handleOnConfirm : undefined}
      isOpen={isOpen}
    >
      <Form>
        <FormGroup label={"Alias"} fieldId={"alias"} isRequired={true}>
          <TextInput
            value={alias}
            onChange={setAlias}
            id={"alias"}
            autoFocus={true}
            isRequired={true}
          />
        </FormGroup>
        <FormGroup label={"URI"} fieldId={"uri"}>
          <TextInput value={uri} onChange={setUri} id={"uri"} />
        </FormGroup>
        <FormGroup label={"Location URI"} fieldId={"locationUri"}>
          <TextInput
            value={locationUri}
            onChange={setLocationUri}
            id={"locationUri"}
          />
        </FormGroup>
        <FormGroup fieldId={"targetNamespace"}>
          <Checkbox
            isChecked={targetNamespace}
            onChange={setTargetNamespace}
            id={"targetNamespace"}
            label={"Target namespace"}
          />
        </FormGroup>
      </Form>
    </ConfirmationDialog>
  );
};
