import React, { FunctionComponent } from "react";

import { Tooltip, Button } from "@patternfly/react-core";
import { TrashIcon } from "@patternfly/react-icons";

export interface IDeleteMappingActionProps {
  onClick: () => void;
}
export const DeleteMappingAction: FunctionComponent<IDeleteMappingActionProps> = ({
  onClick,
}) => (
  <Tooltip
    position={"auto"}
    enableFlip={true}
    content={<div>Remove the mapping</div>}
  >
    <Button variant="plain" onClick={onClick} aria-label="Remove the mapping">
      <TrashIcon />
    </Button>
  </Tooltip>
);
