import React, { FunctionComponent, useState } from "react";
import {
  Dropdown,
  DropdownItem,
  DropdownItemIcon,
  DropdownSeparator,
  DropdownToggle,
  DropdownToggleAction,
} from "@patternfly/react-core";
import {
  FolderCloseIcon,
  FolderOpenIcon,
  TrashIcon,
} from "@patternfly/react-icons";

export interface IDocumentActions {
  onExpandFields: () => void;
  onCollapseFields: () => void;
  onDelete: () => void;
}

export const DocumentActions: FunctionComponent<IDocumentActions> = ({
  onExpandFields,
  onCollapseFields,
  onDelete,
}) => {
  const [showActions, setShowActions] = useState(false);
  const toggleActions = (open: boolean) => setShowActions(open);

  return (
    <Dropdown
      toggle={
        <DropdownToggle
          splitButtonItems={[
            <DropdownToggleAction key="action" onClick={onExpandFields}>
              <FolderOpenIcon />
            </DropdownToggleAction>,
          ]}
          splitButtonVariant="action"
          onToggle={toggleActions}
        />
      }
      isOpen={showActions}
      position={"right"}
      dropdownItems={[
        <DropdownItem
          variant={"icon"}
          key={"collapse"}
          onClick={onCollapseFields}
        >
          <DropdownItemIcon>
            <FolderCloseIcon />
          </DropdownItemIcon>
          Collapse all
        </DropdownItem>,
        <DropdownSeparator key={"sep-1"} />,
        <DropdownItem variant={"icon"} key={"delete"} onClick={onDelete}>
          <DropdownItemIcon>
            <TrashIcon />
          </DropdownItemIcon>
          Remove instance or schema file
        </DropdownItem>,
      ]}
    />
  );
};
