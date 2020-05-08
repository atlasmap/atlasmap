import { Modal, Button, TextInput, Checkbox } from "@patternfly/react-core";
import React, {
  FormEvent,
  ReactPortal,
  useCallback,
  useRef,
  useState,
} from "react";
import { createPortal } from "react-dom";
import { css, StyleSheet } from "@patternfly/react-styles";
import { CancelCallback } from "./useSingleInputDialog";

export type ConfirmNamespaceCallback = (
  doc: string,
  alias: string,
  uri: string,
  locationUri: string,
  isTarget: boolean,
) => void;

export interface IUseNamespaceDialogArgs {
  docName: string;
  initAlias: React.MutableRefObject<string>;
  initUri: React.MutableRefObject<string>;
  initLocationUri: React.MutableRefObject<string>;
  initIsTarget: React.MutableRefObject<boolean>;
  modalContainer: HTMLElement;
}

export function useNamespaceDialog({
  docName,
  initAlias,
  initUri,
  initLocationUri,
  initIsTarget,
  modalContainer,
}: IUseNamespaceDialogArgs): [
  ReactPortal,
  (onConfirm?: ConfirmNamespaceCallback, onCancel?: CancelCallback) => void,
] {
  const onConfirm = useRef<ConfirmNamespaceCallback | undefined>();
  const onCancel = useRef<CancelCallback | undefined>();
  const [isOpen, setIsOpen] = useState(false);
  const [isValid, setIsValid] = useState(true);
  const [alias, setAlias] = useState(initAlias.current);
  const [uri, setUri] = useState(initUri.current);
  const [locationUri, setLocationUri] = useState(initLocationUri.current);
  const [isTarget, setIsTarget] = useState(initIsTarget.current);
  const openModal = (
    onConfirmCb?: ConfirmNamespaceCallback,
    onCancelCb?: CancelCallback,
  ) => {
    onConfirm.current = onConfirmCb;
    onCancel.current = onCancelCb;
    setIsOpen(true);
    setAlias(initAlias.current);
    setUri(initUri.current);
    setLocationUri(initLocationUri.current);
    setIsTarget(initIsTarget.current);
  };

  const closeModal = () => setIsOpen(false);

  const handleAliasChange = (
    alias: string,
    event: FormEvent<HTMLInputElement>,
  ) => {
    setAlias(alias);
    setIsValid((event.target as HTMLInputElement).reportValidity());
  };

  const handleUriChange = (uri: string, event: FormEvent<HTMLInputElement>) => {
    setUri(uri);
    setIsValid((event.target as HTMLInputElement).reportValidity());
  };

  const handleLocationUriChange = (
    locationUri: string,
    event: FormEvent<HTMLInputElement>,
  ) => {
    setLocationUri(locationUri);
    setIsValid((event.target as HTMLInputElement).reportValidity());
  };

  const handleIsTargetChange = (
    isTarget: boolean,
    event: FormEvent<HTMLInputElement>,
  ) => {
    setIsTarget(isTarget);
    setIsValid((event.target as HTMLInputElement).reportValidity());
  };

  const handleConfirm = useCallback(() => {
    if (isValid && onConfirm.current) {
      onConfirm.current(docName, alias, uri, locationUri, isTarget);
    }
    closeModal();
  }, [isValid, docName, alias, uri, locationUri, isTarget]);

  const handleCancel = useCallback(() => {
    if (onCancel.current) {
      onCancel.current();
    }
    closeModal();
  }, [onCancel]);

  const styles = StyleSheet.create({
    iGroupTextLabel: {
      marginTop: "1.0rem",
      width: 425,
    },
  });

  const title = "Create namespace for " + docName;
  const modal = createPortal(
    <Modal
      isSmall
      title={title}
      isOpen={isOpen}
      onClose={closeModal}
      actions={[
        <Button
          key={"confirm"}
          variant={"primary"}
          onClick={handleConfirm}
          isDisabled={!isValid}
          aria-label="Confirm"
          data-testid={"text-select-dialog-confirm-button"}
        >
          Confirm
        </Button>,
        <Button
          key={"cancel"}
          variant={"link"}
          onClick={handleCancel}
          aria-label="Cancel"
          data-testid={"text-select-dialog-cancel-button"}
        >
          Cancel
        </Button>,
      ]}
      isFooterLeftAligned={false}
    >
      <>
        <TextInput
          className={css(styles.iGroupTextLabel)}
          value={alias}
          type="text"
          onChange={handleAliasChange}
          aria-label={title}
          isRequired={true}
          isValid={isValid}
          placeholder={"Alias"}
          data-testid={"itsd-alias-input"}
          autoFocus
        />
      </>
      <>
        <TextInput
          className={css(styles.iGroupTextLabel)}
          value={uri}
          type="text"
          onChange={handleUriChange}
          aria-label={title}
          isRequired={true}
          isValid={isValid}
          placeholder={"URI"}
          data-testid={"itsd-uri-input"}
        />
      </>
      <>
        <TextInput
          className={css(styles.iGroupTextLabel)}
          value={locationUri}
          type="text"
          onChange={handleLocationUriChange}
          aria-label={title}
          isRequired={false}
          isValid={isValid}
          placeholder={"Location URI"}
          data-testid={"itsd-location-uri-input"}
        />
      </>
      <>
        <Checkbox
          label="Target namespace"
          isChecked={isTarget}
          onChange={handleIsTargetChange}
          aria-label="Target namespace"
          id="isTarget"
          data-testid={"itsd-form-select"}
        />
      </>
    </Modal>,
    modalContainer,
  );

  return [modal, openModal];
}
