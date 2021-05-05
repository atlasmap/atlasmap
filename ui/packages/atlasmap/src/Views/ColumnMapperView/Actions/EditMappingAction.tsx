import { Button, Tooltip } from "@patternfly/react-core";
import React, { FunctionComponent } from "react";

import { ExchangeAltIcon } from "@patternfly/react-icons";

export interface IEditMappingActionProps {
  id: string;
  onClick: () => void;
}
export const EditMappingAction: FunctionComponent<IEditMappingActionProps> = ({
  id,
  onClick,
}) => (
  <Tooltip
    position={"auto"}
    enableFlip={true}
    content={<div>Modify the mapping</div>}
  >
    <Button
      variant="plain"
      onClick={onClick}
      aria-label="Modify the mapping"
      data-testid={`modify-the-mapping-${id}-button`}
    >
      <ExchangeAltIcon />
    </Button>
  </Tooltip>
);
