import React, { Children, FunctionComponent } from "react";

import { css } from "@patternfly/react-styles";
import styles from "./Actions.css";

export const Actions: FunctionComponent = ({ children }) => (
  <div className={css(styles.toolbar)}>
    {Children.map(children, (c, idx) => (
      <div className={css(styles.toolbarItem)} key={idx}>
        {c}
      </div>
    ))}
  </div>
);
