import React, { FunctionComponent } from "react";
import { css } from "@patternfly/react-styles";

import styles from "./TruncatedString.css";

export interface ITruncatedStringProps {
  title?: string;
}

export const TruncatedString: FunctionComponent<ITruncatedStringProps> = ({
  title,
  children,
}) => (
  <span
    className={css(styles.fieldName)}
    title={title || (typeof children === "string" ? children : undefined)}
  >
    {children}
  </span>
);
