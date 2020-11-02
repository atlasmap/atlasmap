import React, { FunctionComponent } from "react";

import { Tooltip, Button } from "@patternfly/react-core";
import { EditIcon } from "@patternfly/react-icons";

export interface IChangeDocumentNameActionProps {
  id: string;
  onClick: () => void;
}
export const ChangeDocumentNameAction: FunctionComponent<IChangeDocumentNameActionProps> = ({
  id,
  onClick,
}) => (
  <Tooltip
    position={"auto"}
    enableFlip={true}
    content={<div>Change document name</div>}
  >
    <Button
      variant="plain"
      onClick={onClick}
      aria-label="Change document name"
      data-testid={`change-${id}-name-button`}
    >
      <EditIcon />
    </Button>
  </Tooltip>
);
