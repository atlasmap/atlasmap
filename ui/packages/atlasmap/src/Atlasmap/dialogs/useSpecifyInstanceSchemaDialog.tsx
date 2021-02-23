import React, { useRef, ReactElement } from "react";
import { Button, Modal, Radio } from "@patternfly/react-core";

import { useToggle } from "../../UI";

export function useSpecifyInstanceSchemaDialog(
  defaultSchema: boolean,
): [ReactElement, (cb: (isSchema: boolean) => void) => void] {
  const dialogState: {
    state: boolean;
    toggleOn: () => void;
    toggleOff: () => void;
  } = useToggle(false);
  const isSchema: {
    state: boolean;
    toggleOn: () => void;
    toggleOff: () => void;
  } = useToggle(defaultSchema);
  const onConfirmCb = useRef<((isSchema: boolean) => void) | null>(null);
  const onConfirm = () => {
    if (onConfirmCb.current) {
      onConfirmCb.current(isSchema.state);
    }
    dialogState.toggleOff();
  };
  const openDialog = (confirmCb: (isSchema: boolean) => void) => {
    onConfirmCb.current = confirmCb;
    if (defaultSchema) {
      isSchema.toggleOn();
    } else {
      isSchema.toggleOff();
    }
    dialogState.toggleOn();
  };
  const handleInstanceChange = () => {
    isSchema.toggleOff();
  };
  const handleSchemaChange = () => {
    isSchema.toggleOn();
  };
  return [
    <Modal
      isSmall
      title={"Specify Instance/ Schema"}
      description={
        "Distinguish between instance and schema imported file formats."
      }
      isOpen={dialogState.state}
      onClose={dialogState.toggleOff}
      actions={[
        <Button
          key={"confirm"}
          variant={"primary"}
          onClick={onConfirm}
          aria-label="Ok"
          data-testid={"specify-instance-schema-dialog-ok-button-test"}
          isDisabled={!onConfirm}
          style={{ display: "flex", marginLeft: "auto" }}
        >
          OK
        </Button>,
      ]}
      isFooterLeftAligned={true}
    >
      {
        <React.Fragment>
          <Radio
            isChecked={!isSchema.state}
            name="instance"
            onChange={handleInstanceChange}
            label="Instance"
            id="instance-radio"
            data-testid={"instance-radio-button-test"}
          />
          <Radio
            isChecked={isSchema.state}
            name="schema"
            onChange={handleSchemaChange}
            label="Schema"
            id="schema-radio"
            data-testid={"schema-radio-button-test"}
          />
        </React.Fragment>
      }
    </Modal>,
    openDialog,
  ];
}
