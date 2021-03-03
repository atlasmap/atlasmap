import React, { FunctionComponent } from "react";

import { Alert, Button, Modal } from "@patternfly/react-core";

export type CloseCallback = () => void;

export interface IAboutDialogProps {
  title: string;
  onClose: CloseCallback;
  isOpen: boolean;
  uiVersion: string;
  runtimeVersion: string;
}

export const AboutDialog: FunctionComponent<IAboutDialogProps> = ({
  title,
  onClose,
  isOpen,
  uiVersion,
  runtimeVersion,
}) => {
  return (
    <Modal
      isSmall
      title={title}
      isOpen={isOpen}
      onClose={onClose}
      actions={[
        <Button
          key={"close"}
          variant={"primary"}
          onClick={onClose}
          aria-label="Close"
          data-testid={"about-dialog-close-button"}
          isDisabled={!onClose}
        >
          Close
        </Button>,
      ]}
      isFooterLeftAligned={true}
    >
      {runtimeVersion !== uiVersion && (
        <div>
          <Alert variant="warning" isInline title="WARNING">
            Different version of UI and Runtime are not supported to work
            together.
          </Alert>
          <div>&nbsp;</div>
        </div>
      )}
      <div>UI Version: {uiVersion}</div>
      <div>Runtime Version: {runtimeVersion}</div>
    </Modal>
  );
};
