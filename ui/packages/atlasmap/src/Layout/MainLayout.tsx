import React, { FunctionComponent, ReactElement, ReactNode, memo } from "react";

import { css, StyleSheet } from "@patternfly/react-styles";
import { TopologyView } from "@patternfly/react-topology";

import { Loading } from "../UI";
import { Sidebar } from "./Sidebar";

const styles = StyleSheet.create({
  view: {
    "& .pf-topology-container": {
      overflow: "hidden",
    },
    "& .pf-topology-content": {
      overflow: "auto",
    },
  },
});

export interface IMainLayoutProps {
  loading: boolean;
  showSidebar: boolean;
  contextToolbar?: ReactNode;
  viewToolbar?: ReactNode;
  controlBar?: ReactNode;
  renderSidebar: () => ReactElement;
}

export const MainLayout: FunctionComponent<IMainLayoutProps> = memo(
  ({
    loading,
    showSidebar,
    renderSidebar,
    contextToolbar,
    viewToolbar,
    controlBar,
    children,
  }) => {
    return loading ? (
      <Loading />
    ) : (
      <TopologyView
        contextToolbar={contextToolbar}
        viewToolbar={viewToolbar}
        controlBar={controlBar}
        sideBarOpen={showSidebar}
        sideBar={<Sidebar show={showSidebar} children={renderSidebar} />}
        className={css(styles.view)}
        data-testid="datamapper-root-view"
      >
        {children}
      </TopologyView>
    );
  },
);
