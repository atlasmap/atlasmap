import React, { FunctionComponent } from "react";

import { Tooltip, Button } from "@patternfly/react-core";
import { ExchangeAltIcon } from "@patternfly/react-icons";

export interface IEditMappingActionProps {
  onClick: () => void;
}
export const EditMappingAction: FunctionComponent<IEditMappingActionProps> = ({
  onClick,
}) => (
  <Tooltip
    position={"auto"}
    enableFlip={true}
    content={<div>Modify the mapping</div>}
  >
    <Button variant="plain" onClick={onClick} aria-label="Modify the mapping">
      <ExchangeAltIcon />
    </Button>
  </Tooltip>
);
