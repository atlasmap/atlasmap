import React, { FunctionComponent } from "react";

import { Tooltip, Button } from "@patternfly/react-core";
import { PlusIcon } from "@patternfly/react-icons";

export interface IAddMappingActionProps {
  id: string;
  onClick: () => void;
}
export const AddMappingAction: FunctionComponent<IAddMappingActionProps> = ({
  id,
  onClick,
}) => (
  <Tooltip
    position={"auto"}
    enableFlip={true}
    content={<div>Add new mapping</div>}
  >
    <Button
      variant="plain"
      onClick={onClick}
      aria-label="Add new mapping"
      data-testid={`add-${id}-mapping-button`}
    >
      <PlusIcon />
    </Button>
  </Tooltip>
);
