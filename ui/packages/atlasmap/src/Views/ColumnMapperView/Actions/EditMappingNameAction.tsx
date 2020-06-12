import React, { FunctionComponent } from "react";

import { Tooltip, Button } from "@patternfly/react-core";
import { EditIcon } from "@patternfly/react-icons";

export interface IEditMappingNameActionProps {
  id: string;
  onClick: () => void;
}
export const EditMappingNameAction: FunctionComponent<IEditMappingNameActionProps> = ({
  id,
  onClick,
}) => (
  <Tooltip
    position={"auto"}
    enableFlip={true}
    content={<div>Edit mapping name</div>}
  >
    <Button
      variant="plain"
      onClick={onClick}
      aria-label="Edit mapping name"
      data-testid={`edit-mapping-name-${id}-button`}
    >
      <EditIcon />
    </Button>
  </Tooltip>
);
