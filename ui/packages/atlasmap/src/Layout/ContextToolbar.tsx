import React, { FunctionComponent } from "react";

import { Toolbar } from "@patternfly/react-core";
import { css } from "@patternfly/react-styles";
import styles from "./ContextToolbar.css";

export const ContextToolbar: FunctionComponent = ({ children }) => (
  <Toolbar
    id="data-toolbar"
    className={css("view-toolbar pf-u-px-md pf-u-py-md", styles.toolbar)}
    role={"complementary"}
  >
    {children}
  </Toolbar>
);
