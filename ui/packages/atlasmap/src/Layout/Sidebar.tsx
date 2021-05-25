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
import { TopologySideBar } from '@patternfly/react-topology';
import styles from './Sidebar.module.css';

export interface ISidebarProps {
  show: boolean;
  children: () => ReactElement;
}

export const Sidebar: FunctionComponent<ISidebarProps> = ({
  show,
  children,
}) => {
  return (
    <TopologySideBar show={show} className={styles.sidebar}>
      {show && children()}
    </TopologySideBar>
  );
};
