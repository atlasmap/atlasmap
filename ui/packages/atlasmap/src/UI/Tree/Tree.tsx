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
import React, { FunctionComponent, useLayoutEffect, useRef } from 'react';

import { Accordion } from '@patternfly/react-core';
import { TreeFocusProvider } from './TreeFocusProvider';
import styles from './Tree.module.css';

export const Tree: FunctionComponent = ({ children }) => {
  const ref = useRef<HTMLDivElement | null>(null);

  useLayoutEffect(() => {
    if (ref.current) {
      const itemInTabSequence = ref.current.querySelector(
        `[role=treeitem][tabindex="0"]`,
      );
      if (!itemInTabSequence) {
        const firstTreeItem = ref.current.querySelector('[role=treeitem]');
        if (firstTreeItem) {
          firstTreeItem.setAttribute('tabindex', '0');
        }
      }
    }
  });
  return (
    <TreeFocusProvider>
      <div ref={ref}>
        <Accordion
          asDefinitionList={false}
          className={styles.accordion}
          role={'tree'}
        >
          {children}
        </Accordion>
      </div>
    </TreeFocusProvider>
  );
};
