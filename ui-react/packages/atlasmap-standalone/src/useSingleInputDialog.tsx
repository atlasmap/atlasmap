import { Modal, Button, TextInput } from "@patternfly/react-core";
import React, {
  FormEvent,
  ReactChild,
  ReactPortal,
  useCallback,
  useState
} from "react";
import { createPortal } from "react-dom";

export interface IUseSingleInputDialogArgs {
  title: string;
  content: ReactChild;
  defaultValue: string;
  onConfirm: (closeDialog: () => void, value: string) => void;
  onCancel: (closeDialog: () => void) => void;
}

export function useSingleInputDialog({
  title,
  content,
  defaultValue,
  onConfirm,
  onCancel
}: IUseSingleInputDialogArgs): [ReactPortal, () => void] {
  const [isOpen, setIsOpen] = useState(false);
  const [isValid, setIsValid] = useState(true);
  const [value, setValue] = useState('');
  const openModal = () => setIsOpen(true);
  const closeModal = () => setIsOpen(false);
  const handleTextInputChange = (
    value: string,
    event: FormEvent<HTMLInputElement>
  ) => {
    setValue(value);
    setIsValid((event.target as HTMLInputElement).reportValidity());
  };
  const handleConfirm = useCallback(() => {
    isValid && onConfirm(closeModal, value);
  }, [onConfirm, value, isValid]);
  const handleCancel = useCallback(() => onCancel(closeModal), [onCancel]);

  const modal = createPortal(
    <Modal
      isSmall
      title={title}
      isOpen={isOpen}
      onClose={closeModal}
      actions={[
        <TextInput
          key={"text-input"}
          value={value}
          placeholder={defaultValue}
          type="text"
          onChange={handleTextInputChange}
          aria-label={title}
          isRequired={true}
          isValid={isValid}
        />,
        <Button
          key={"confirm"}
          variant={"primary"}
          onClick={handleConfirm}
          isDisabled={!isValid}
        >
          Confirm
        </Button>,
        <Button key={"cancel"} variant={"link"} onClick={handleCancel}>
          Cancel
        </Button>
      ]}
      isFooterLeftAligned={true}
    >
      {content}
    </Modal>,
    document.getElementById("modals")!
  );

  return [modal, openModal];
}
