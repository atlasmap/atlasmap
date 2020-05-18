import {
  Modal,
  Button,
  TextInput,
  FormSelect,
  FormSelectOption,
  Label,
} from "@patternfly/react-core";
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

export type ConfirmInputTextCallback = (
  value1: string,
  value2: string,
  selectValue: string,
) => void;

export interface IUseInputTextSelectDialogArgs {
  title: string;
  textLabel1: string;
  textValue1: React.MutableRefObject<string>;
  text1ReadOnly: boolean;
  textLabel2: string;
  textValue2: React.MutableRefObject<string>;
  selectLabel: string;
  selectValues: string[][];
  selectDefault: React.MutableRefObject<number>;
  modalContainer: HTMLElement;
}

export function useInputTextSelectDialog({
  title,
  textLabel1,
  textValue1,
  text1ReadOnly,
  textLabel2,
  textValue2,
  selectLabel,
  selectValues,
  selectDefault,
  modalContainer,
}: IUseInputTextSelectDialogArgs): [
  ReactPortal,
  (onConfirm?: ConfirmInputTextCallback, onCancel?: CancelCallback) => void,
] {
  const onConfirm = useRef<ConfirmInputTextCallback | undefined>();
  const onCancel = useRef<CancelCallback | undefined>();
  const [isOpen, setIsOpen] = useState(false);
  const [isValid, setIsValid] = useState(true);
  const [value1, setValue1] = useState(textValue1.current);
  const [value2, setValue2] = useState(textValue2.current);
  const [selectValue, setSelectValue] = useState("");
  const openModal = (
    onConfirmCb?: ConfirmInputTextCallback,
    onCancelCb?: CancelCallback,
  ) => {
    onConfirm.current = onConfirmCb;
    onCancel.current = onCancelCb;
    setIsOpen(true);
    setValue1(textValue1.current);
    setValue2(textValue2.current);
    setSelectValue(selectValues[selectDefault.current][0]);
  };

  const closeModal = () => setIsOpen(false);

  const handleTextInputChange = (
    value: string,
    event: FormEvent<HTMLInputElement>,
  ) => {
    setValue1(value);
    setIsValid((event.target as HTMLInputElement).reportValidity());
  };

  const handleTextInputChange2 = (
    value: string,
    event: FormEvent<HTMLInputElement>,
  ) => {
    setValue2(value);
    setIsValid((event.target as HTMLInputElement).reportValidity());
  };

  const handleSelect = (value: string) => {
    setSelectValue(value);
  };

  const handleConfirm = useCallback(() => {
    if (isValid && onConfirm.current) {
      onConfirm.current(value1, value2, selectValue);
    }
    closeModal();
  }, [onConfirm, value1, value2, selectValue, isValid]);

  const handleCancel = useCallback(() => {
    if (onCancel.current) {
      onCancel.current();
    }
    closeModal();
  }, [onCancel]);

  const styles = StyleSheet.create({
    iGroup: {
      marginBottom: "1.0rem",
      marginLeft: "0.5rem",
    },
    iSelectBody: {
      width: 200,
      marginLeft: "0.5rem",
    },
    iSelectLabel: {
      marginTop: "1.5rem",
      marginLeft: "0.5rem",
      width: 150,
      marginRight: "250",
    },
    iGroupTextLabel: {
      marginTop: "1.0rem",
      width: 425,
    },
  });

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
      {textLabel1 && (
        <>
          <TextInput
            className={css(styles.iGroupTextLabel)}
            value={value1}
            type="text"
            onChange={handleTextInputChange}
            aria-label={title}
            isRequired={true}
            isValid={isValid}
            isDisabled={text1ReadOnly}
            placeholder={textLabel1}
            data-testid={"itsd-value1-text-input"}
            autoFocus
          />
        </>
      )}
      {textLabel2.length > 0 && (
        <>
          <TextInput
            className={css(styles.iGroupTextLabel)}
            value={value2}
            type="text"
            onChange={handleTextInputChange2}
            aria-label={title}
            isRequired={true}
            isValid={isValid}
            placeholder={textLabel2}
            data-testid={"itsd-value2-text-input"}
          />
        </>
      )}
      {selectLabel && (
        <>
          <Label className={css(styles.iSelectLabel)}>{selectLabel}</Label>
          <FormSelect
            className={css(styles.iSelectBody)}
            value={selectValue}
            id={selectValue}
            onChange={handleSelect}
            data-testid={"itsd-form-select"}
          >
            {selectValues.map(
              (selectValue: string[], idx: number | undefined) => (
                <FormSelectOption
                  label={selectValue[1]}
                  value={selectValue[0]}
                  key={idx}
                />
              ),
            )}
          </FormSelect>
        </>
      )}
    </Modal>,
    modalContainer,
  );

  return [modal, openModal];
}
