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
import React, { FunctionComponent, ReactElement, ReactNode, memo } from 'react';
import { Stack, StackItem } from '@patternfly/react-core';

import { Loading } from '../UI';
import { Sidebar } from './Sidebar';
import styles from './MainLayout.module.css';

export interface IMainLayoutProps {
  loading: boolean;
  showSidebar: boolean;
  contextToolbar?: ReactNode;
  expressionToolbar?: ReactNode;
  controlBar?: ReactNode;
  renderSidebar: () => ReactElement;
}

export const MainLayout: FunctionComponent<IMainLayoutProps> = memo(
  function MainLayout({
    loading,
    showSidebar,
    renderSidebar,
    contextToolbar,
    expressionToolbar,
    controlBar,
    children,
  }) {
    const sideBar = <Sidebar show={showSidebar}>{renderSidebar}</Sidebar>;
    const containerClasses =
      'pf-topology-container' +
      `${sideBar ? ' pf-topology-container__with-sidebar' : ''}` +
      `${showSidebar ? ' pf-topology-container__with-sidebar--open' : ''}`;

    return loading ? (
      <Loading />
    ) : (
      <Stack className={styles.view}>
        {contextToolbar && (
          <StackItem isFilled={false}>{contextToolbar}</StackItem>
        )}
        {expressionToolbar && (
          <StackItem isFilled={false}>{expressionToolbar}</StackItem>
        )}
        <StackItem isFilled className={containerClasses}>
          <div className="pf-topology-content">
            {children}
            {controlBar && (
              <span className="pf-topology-control-bar">{controlBar}</span>
            )}
          </div>
          {sideBar}
        </StackItem>
      </Stack>
    );
  },
);
