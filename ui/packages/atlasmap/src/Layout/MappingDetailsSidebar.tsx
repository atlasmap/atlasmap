import React, { FunctionComponent } from "react";

import { Button, Stack, StackItem, Tooltip } from "@patternfly/react-core";
import { CloseIcon, EditIcon, TrashIcon } from "@patternfly/react-icons";
import { css, StyleSheet } from "@patternfly/react-styles";

import { ColumnHeader } from "../UI";

const styles = StyleSheet.create({
  content: {
    boxSizing: "border-box",
    padding: "0 1rem",
    height: "100%",
    overflow: "auto",
  },
});
export interface IMappingDetailsSidebarProps {
  onDelete: () => void;
  onClose: () => void;
  onEditEnum: (cb: any) => void;
  isEnumMapping: () => boolean;
}

export const MappingDetailsSidebar: FunctionComponent<IMappingDetailsSidebarProps> = ({
  onDelete,
  onClose,
  onEditEnum,
  isEnumMapping,
  children,
}) => {
  return (
    <Stack data-testid="column-mapping-details-area">
      <StackItem>
        <ColumnHeader
          title={"Mapping Details"}
          variant={"plain"}
          actions={[
            <Button
              onClick={onClose}
              variant={"plain"}
              aria-label="Close the mapping details panel"
              data-testid={"close-mapping-detail-button"}
              key={"close"}
            >
              <CloseIcon />
            </Button>,
            <Tooltip
              key={"edit-enum"}
              position={"auto"}
              enableFlip={true}
              content={<div>Edit the enumeration mappings</div>}
              entryDelay={750}
              exitDelay={100}
            >
              <Button
                variant={"plain"}
                onClick={onEditEnum}
                aria-label="Edit the enumeration mappings"
                data-testid={"edit-enum-mapping-button"}
                isDisabled={!isEnumMapping()}
              >
                <EditIcon />
              </Button>
            </Tooltip>,
            <Tooltip
              key={"remove"}
              position={"auto"}
              enableFlip={true}
              content={<div>Remove the current mapping</div>}
              entryDelay={750}
              exitDelay={100}
            >
              <Button
                variant={"plain"}
                onClick={onDelete}
                aria-label="Remove the current mapping"
                data-testid={"remove-current-mapping-button"}
              >
                <TrashIcon />
              </Button>
            </Tooltip>,
          ]}
        />
      </StackItem>
      <StackItem isFilled={true} className={css(styles.content)}>
        {children}
      </StackItem>
    </Stack>
  );
};
