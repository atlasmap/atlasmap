import React, { FunctionComponent } from "react";

import { CardFooter } from "@patternfly/react-core";
import { css } from "@patternfly/react-styles";

import styles from "./DocumentFooter.css";

export const DocumentFooter: FunctionComponent = ({ children }) => {
  return <CardFooter className={css(styles.footer)}>{children}</CardFooter>;
};
