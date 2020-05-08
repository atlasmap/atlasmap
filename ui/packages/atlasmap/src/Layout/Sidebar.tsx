import React, { FunctionComponent, ReactElement } from "react";
import { TopologySideBar } from "@patternfly/react-topology";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  sidebar: {
    fontSize: "small",
    height: "100%",
    overflow: "hidden",
    "& > .pf-topology-side-bar__body": {
      height: "100%",
    },
  },
});

export interface ISidebarProps {
  show: boolean;
  children: () => ReactElement;
}

export const Sidebar: FunctionComponent<ISidebarProps> = ({
  show,
  children,
}) => {
  return (
    <TopologySideBar show={show} className={css(styles.sidebar)}>
      {show && children()}
    </TopologySideBar>
  );
};
