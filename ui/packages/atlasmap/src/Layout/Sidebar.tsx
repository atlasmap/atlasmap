import React, { FunctionComponent, ReactElement } from "react";
import { TopologySideBar } from "@patternfly/react-topology";
import { css } from "@patternfly/react-styles";
import styles from "./Sidebar.css";

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
