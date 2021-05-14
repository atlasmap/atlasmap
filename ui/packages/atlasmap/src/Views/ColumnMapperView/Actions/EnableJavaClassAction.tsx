import { Button, Tooltip } from "@patternfly/react-core";
import React, { FunctionComponent } from "react";

import { AddCircleOIcon } from "@patternfly/react-icons";

export interface IEnableJavaClassActionProps {
  onCustomClassSearch: () => void;
}

export const EnableJavaClassAction: FunctionComponent<IEnableJavaClassActionProps> =
  ({ onCustomClassSearch, ...props }) => {
    function onEnableClassSearch(): void {
      onCustomClassSearch();
    }

    return (
      <Tooltip
        position={"auto"}
        enableFlip={true}
        content={
          <div>
            Load a Java document based on classes from your previously imported
            Java archive.
          </div>
        }
      >
        <Button
          variant="plain"
          onClick={onEnableClassSearch}
          aria-label="Load a Java document based on classes from your previously imported Java archive."
          {...props}
        >
          <AddCircleOIcon />
        </Button>
      </Tooltip>
    );
  };
