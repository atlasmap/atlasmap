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

import { AddCircleOIcon } from '@patternfly/react-icons';

export interface IEnableJavaClassActionProps {
  onCustomClassSearch: () => void;
}

export const EnableJavaClassAction: FunctionComponent<
  IEnableJavaClassActionProps
> = ({ onCustomClassSearch, ...props }) => {
  function onEnableClassSearch(): void {
    onCustomClassSearch();
  }

  return (
    <Tooltip
      position={'auto'}
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
