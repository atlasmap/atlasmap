import React, { FunctionComponent } from "react";

import { Toolbar } from "@patternfly/react-core";
import { css } from "@patternfly/react-styles";

import styles from "./ViewToolbar.css";

export const ViewToolbar: FunctionComponent = ({ children }) => {
  return (
    <Toolbar
      className={css("view-toolbar pf-u-px-md pf-u-py-md", styles.toolbar)}
    >
      {children}
    </Toolbar>
  );
};
