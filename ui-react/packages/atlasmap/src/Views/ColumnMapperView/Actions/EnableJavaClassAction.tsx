import React, { FunctionComponent } from "react";

import { Button, Tooltip } from "@patternfly/react-core";
import { AddCircleOIcon } from "@patternfly/react-icons";

export interface IEnableJavaClassActionProps {
  id: string;
  onClick: () => void;
}

export const EnableJavaClassAction: FunctionComponent<IEnableJavaClassActionProps> = ({
  id,
  onClick,
}) => (
  <Tooltip
    position={"auto"}
    enableFlip={true}
    content={
      <div>
        Enable specific Java classes from your previously imported Java archive.
      </div>
    }
  >
    <Button
      variant="plain"
      onClick={onClick}
      aria-label="Enable specific Java classes from your previously imported Java archive."
      data-testid={`enable-specific-java-classes-${id}-button`}
    >
      <AddCircleOIcon />
    </Button>
  </Tooltip>
);
