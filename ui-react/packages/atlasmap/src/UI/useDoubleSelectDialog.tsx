import {
  Modal,
  Button,
  FormSelect,
  FormSelectOption,
  Label,
} from "@patternfly/react-core";
import React, { ReactPortal, useCallback, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { css, StyleSheet } from "@patternfly/react-styles";
import { CancelCallback } from "./useSingleInputDialog";

interface ISelectValues1 {
  isSelectable: boolean;
  value: string;
}

export type ConfirmDoubleSelectCallback = (
  value1: string,
  value2: string,
) => void;

export interface IUseDoubleSelectDialogArgs {
  title: string;
  selectLabel1: string;
  selectLabel2: string;
  selectValues1: ISelectValues1[];
  selectValues2: string[];
  selectDefault1: React.MutableRefObject<number>;
  selectDefault2: React.MutableRefObject<number>;
  modalContainer: HTMLElement;
}

export function useDoubleSelectDialog({
  title,
  selectLabel1,
  selectLabel2,
  selectValues1,
  selectValues2,
  selectDefault1,
  selectDefault2,
  modalContainer,
}: IUseDoubleSelectDialogArgs): [
  ReactPortal,
  (onConfirm?: ConfirmDoubleSelectCallback, onCancel?: CancelCallback) => void,
] {
  const onConfirm = useRef<ConfirmDoubleSelectCallback | undefined>();
  const onCancel = useRef<CancelCallback | undefined>();
  const [isOpen, setIsOpen] = useState(false);
  const [selectValue1, setSelectValue1] = useState<string>();
  const [selectValue2, setSelectValue2] = useState<string>();

  const openModal = (
    onConfirmCb?: ConfirmDoubleSelectCallback,
    onCancelCb?: CancelCallback,
  ) => {
    onConfirm.current = onConfirmCb;
    onCancel.current = onCancelCb;
    setIsOpen(true);
    setSelectValue1(selectValues1[selectDefault1.current].value);
    setSelectValue2(selectValues2[selectDefault2.current]);
  };

  const closeModal = () => setIsOpen(false);

  const handleSelect1 = (value: string) => {
    setSelectValue1(value);
  };

  const handleSelect2 = (value: string) => {
    setSelectValue2(value);
  };

  const handleConfirm = useCallback(() => {
    if (onConfirm.current) {
      onConfirm.current(selectValue1!, selectValue2!);
    }
    closeModal();
  }, [onConfirm, selectValue1, selectValue2]);

  const handleCancel = useCallback(() => {
    if (onCancel.current) {
      onCancel.current();
    }
    closeModal();
  }, [onCancel]);

  const styles = StyleSheet.create({
    iSelect2Body: {
      width: 100,
      marginLeft: "0.5rem",
    },
    iSelect2Label: {
      marginTop: "1.0rem",
      marginLeft: "0.5rem",
      width: 150,
      marginRight: "250",
    },
    iGroupTextLabel: {
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
          aria-label="Confirm"
          data-testid={"select-double-dialog-confirm-button"}
        >
          Confirm
        </Button>,
        <Button
          key={"cancel"}
          variant={"link"}
          onClick={handleCancel}
          aria-label="Cancel"
          data-testid={"select-double-dialog-cancel-button"}
        >
          Cancel
        </Button>,
      ]}
      isFooterLeftAligned={false}
    >
      {selectLabel1 && (
        <>
          <Label>{selectLabel1}</Label>
          <FormSelect
            value={selectValue1}
            id={selectValue1}
            onChange={handleSelect1}
            data-testid={"type-dropdown-form-select"}
          >
            {selectValues1.map((selectValue: ISelectValues1, idx: number) => (
              <FormSelectOption
                label={selectValue.value}
                value={selectValue.value}
                isDisabled={!selectValue.isSelectable}
                key={idx}
              />
            ))}
          </FormSelect>
        </>
      )}
      {selectLabel2 && (
        <>
          <Label className={css(styles.iSelect2Label)}>{selectLabel2}</Label>
          <FormSelect
            className={css(styles.iSelect2Body)}
            value={selectValue2}
            id={selectValue2}
            onChange={handleSelect2}
            data-testid={"type-dropdown-form-select2"}
          >
            {selectValues2.map(
              (selectValue: string, idx: number | undefined) => (
                <FormSelectOption
                  label={selectValue}
                  value={selectValue}
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
