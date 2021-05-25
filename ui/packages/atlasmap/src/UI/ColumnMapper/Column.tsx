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
import React, { FunctionComponent, useMemo } from 'react';
import { css } from '@patternfly/react-styles';
import styles from './Column.module.css';

export interface IColumnProps {
  totalColumns?: number;
  visible?: boolean;
}

export const Column: FunctionComponent<IColumnProps> = ({
  totalColumns = 1,
  visible = true,
  children,
  ...props
}) => {
  const style = useMemo(
    () => ({ flex: `0 0 ${100 / totalColumns}%` }),
    [totalColumns],
  );
  return (
    <div
      className={css(styles.column, !visible && styles.hidden)}
      style={style}
      {...props}
    >
      {children}
    </div>
  );
};
