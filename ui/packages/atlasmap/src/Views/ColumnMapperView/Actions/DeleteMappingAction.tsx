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
import { Button, Tooltip } from '@patternfly/react-core';
import React, { FunctionComponent } from 'react';

import { TrashIcon } from '@patternfly/react-icons';

export interface IDeleteMappingActionProps {
  id: string;
  onClick: () => void;
}
export const DeleteMappingAction: FunctionComponent<
  IDeleteMappingActionProps
> = ({ id, onClick }) => (
  <Tooltip
    position={'auto'}
    enableFlip={true}
    content={<div>Remove the mapping</div>}
  >
    <Button
      variant="plain"
      onClick={onClick}
      aria-label="Remove the mapping"
      data-testid={`remove-${id}-mapping-button`}
    >
      <TrashIcon />
    </Button>
  </Tooltip>
);
