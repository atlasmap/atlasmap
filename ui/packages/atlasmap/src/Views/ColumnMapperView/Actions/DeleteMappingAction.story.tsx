import React, { FunctionComponent } from "react";

import { Tooltip, Button } from "@patternfly/react-core";
import { TrashIcon } from "@patternfly/react-icons";
import { StyleSheet, css } from "@patternfly/react-styles";

export interface IDeleteMappingActionProps {
  id: string;
  onClick: () => void;
}

const styles = StyleSheet.create({
  button: {
    padding: 0,
    paddingRight: "1rem",
  },
});

export const DeleteMappingAction: FunctionComponent<IDeleteMappingActionProps> = ({
  id,
  onClick,
}) => (
  <Tooltip
    position={"auto"}
    enableFlip={true}
    content={<div>Remove the mapping</div>}
  >
    <Button
      variant="plain"
      onClick={onClick}
      aria-label="Remove the mapping"
      data-testid={`remove-${id}-mapping-button`}
      className={css(styles.button)}
    >
      <TrashIcon />
    </Button>
  </Tooltip>
);
