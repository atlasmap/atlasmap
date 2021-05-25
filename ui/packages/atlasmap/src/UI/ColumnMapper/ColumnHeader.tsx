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
import React, { FunctionComponent, ReactElement } from 'react';

import { Actions } from '../Actions';

import { Title } from '@patternfly/react-core';
import { css } from '@patternfly/react-styles';
import styles from './ColumnHeader.module.css';

export interface IColumnHeaderProps {
  title: string;
  variant?: 'default' | 'plain';
  actions?: (ReactElement | null | undefined)[];
}

export const ColumnHeader: FunctionComponent<IColumnHeaderProps> = ({
  title,
  actions,
  variant,
  children,
}) => {
  return (
    <div className={styles.header}>
      <div className={css(styles.toolbar, variant === 'plain' && styles.plain)}>
        <div className={styles.title}>
          <Title headingLevel="h2" size="lg">
            {title}
          </Title>
        </div>
        <Actions>{actions?.filter((a) => a)}</Actions>
      </div>
      {children}
    </div>
  );
};
