import { Button, Tooltip } from "@patternfly/react-core";
import React, { FunctionComponent } from "react";

import { TrashIcon } from "@patternfly/react-icons";

export interface IDeleteDocumentActionProps {
  id: string;
  onClick: () => void;
}
export const DeleteDocumentAction: FunctionComponent<IDeleteDocumentActionProps> =
  ({ id, onClick }) => (
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={<div>Remove instance or schema file</div>}
    >
      <Button
        variant="plain"
        onClick={onClick}
        aria-label="Remove instance or schema file"
        data-testid={`remove-${id}-instance-or-schema-file-button`}
      >
        <TrashIcon />
      </Button>
    </Tooltip>
  );
