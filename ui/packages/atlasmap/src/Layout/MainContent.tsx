import React, { FunctionComponent } from "react";

import { css } from "@patternfly/react-styles";
import styles from "./MainContent.css";

export const MainContent: FunctionComponent = ({ children }) => (
  <div className={css(styles.wrapper)} role={"main"}>
    {children}
  </div>
);
