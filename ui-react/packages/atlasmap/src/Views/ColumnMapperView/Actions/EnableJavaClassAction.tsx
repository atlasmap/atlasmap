import React, { FunctionComponent } from "react";
import { Button, Tooltip } from "@patternfly/react-core";
import { AddCircleOIcon } from "@patternfly/react-icons";

export interface IEnableJavaClassActionProps {
  isSource: boolean;
  onCustomClassSearch: (isSource: boolean) => void;
}

export const EnableJavaClassAction: FunctionComponent<IEnableJavaClassActionProps> = ({
  isSource,
  onCustomClassSearch,
}) => {
  function onEnableClassSearch(): void {
    onCustomClassSearch(isSource);
  }

  return (
    <Tooltip
      position={"auto"}
      enableFlip={true}
      content={
        <div>
          Enable specific Java classes from your previously imported Java
          archive.
        </div>
      }
    >
      <Button
        variant="plain"
        onClick={onEnableClassSearch}
        aria-label="Enable specific Java classes from your previously imported Java archive."
      >
        <AddCircleOIcon />
      </Button>
    </Tooltip>
  );
};
