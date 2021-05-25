/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import {
  Dropdown,
  DropdownItem,
  DropdownSeparator,
  DropdownToggle,
  DropdownToggleAction,
} from '@patternfly/react-core';
import {
  FolderCloseIcon,
  FolderOpenIcon,
  TrashIcon,
} from '@patternfly/react-icons';
import React, { FunctionComponent, useState } from 'react';

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
      position={'right'}
      dropdownItems={[
        <DropdownItem
          icon={<FolderCloseIcon />}
          key={'collapse'}
          onClick={onCollapseFields}
        >
          Collapse all
        </DropdownItem>,
        <DropdownSeparator key={'sep-1'} />,
        <DropdownItem icon={<TrashIcon />} key={'delete'} onClick={onDelete}>
          Remove instance or schema file
        </DropdownItem>,
      ]}
    />
  );
};
