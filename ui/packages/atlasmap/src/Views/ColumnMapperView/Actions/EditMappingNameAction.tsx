import React, { FunctionComponent } from "react";

import { Tooltip, Button } from "@patternfly/react-core";
import { EditIcon } from "@patternfly/react-icons";
import { StyleSheet, css } from "@patternfly/react-styles";

export interface IEditMappingNameActionProps {
  id: string;
  onClick: () => void;
}

const styles = StyleSheet.create({
  button: {
    padding: 0,
  },
});

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
      className={css(styles.button)}
    >
      <EditIcon />
    </Button>
  </Tooltip>
);
