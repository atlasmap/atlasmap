import React, { FunctionComponent } from "react";

import { CardFooter } from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  footer: {
    paddingTop: "1rem",
  },
});

export const DocumentFooter: FunctionComponent = ({ children }) => {
  return <CardFooter className={css(styles.footer)}>{children}</CardFooter>;
};
