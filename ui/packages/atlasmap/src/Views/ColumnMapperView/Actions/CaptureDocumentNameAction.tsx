import { Button, Tooltip } from "@patternfly/react-core";
import React, { FunctionComponent } from "react";

import { ClipboardIcon } from "@patternfly/react-icons";

export interface ICaptureDocumentNameActionProps {
  id: string;
  onClick: () => void;
}
export const CaptureDocumentNameAction: FunctionComponent<ICaptureDocumentNameActionProps> =
  ({ id, onClick }) => (
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={<div>Capture extended file name to clipboard</div>}
    >
      <Button
        variant="plain"
        onClick={onClick}
        aria-label="Capture extended file name to clipboard"
        data-testid={`capture-${id}-name-button`}
      >
        <ClipboardIcon />
      </Button>
    </Tooltip>
  );
