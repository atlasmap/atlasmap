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
import { Button, Stack, StackItem, Tooltip } from '@patternfly/react-core';
import { CloseIcon, EditIcon, TrashIcon } from '@patternfly/react-icons';
import React, { FunctionComponent } from 'react';

import { ColumnHeader } from '../UI';
import styles from './MappingDetailsSidebar.module.css';

export interface IMappingDetailsSidebarProps {
  onDelete: () => void;
  onClose: () => void;
  onEditEnum: (cb: any) => void;
  isEnumMapping: () => boolean;
}

export const MappingDetailsSidebar: FunctionComponent<
  IMappingDetailsSidebarProps
> = ({ onDelete, onClose, onEditEnum, isEnumMapping, children }) => {
  return (
    <Stack data-testid="column-mapping-details-area">
      <StackItem>
        <ColumnHeader
          title={'Mapping Details'}
          variant={'plain'}
          actions={[
            <Button
              onClick={onClose}
              variant={'plain'}
              aria-label="Close the mapping details panel"
              data-testid={'close-mapping-detail-button'}
              key={'close'}
            >
              <CloseIcon />
            </Button>,
            <Tooltip
              key={'edit-enum'}
              position={'auto'}
              enableFlip={true}
              content={<div>Edit the enumeration mappings</div>}
              entryDelay={750}
              exitDelay={100}
            >
              <Button
                variant={'plain'}
                onClick={onEditEnum}
                aria-label="Edit the enumeration mappings"
                data-testid={'edit-enum-mapping-button'}
                isDisabled={!isEnumMapping()}
              >
                <EditIcon />
              </Button>
            </Tooltip>,
            <Tooltip
              key={'remove'}
              position={'auto'}
              enableFlip={true}
              content={<div>Remove the current mapping</div>}
              entryDelay={750}
              exitDelay={100}
            >
              <Button
                variant={'plain'}
                onClick={onDelete}
                aria-label="Remove the current mapping"
                data-testid={'remove-current-mapping-button'}
              >
                <TrashIcon />
              </Button>
            </Tooltip>,
          ]}
        />
      </StackItem>
      <StackItem isFilled={true} className={styles.content}>
        {children}
      </StackItem>
    </Stack>
  );
};
