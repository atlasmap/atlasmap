import React, { PropsWithChildren, forwardRef } from "react";

import { css } from "@patternfly/react-styles";

import styles from "./ColumnBody.css";

export const ColumnBody = forwardRef<HTMLDivElement, PropsWithChildren<{}>>(
  function ColumnBody({ children }, ref) {
    return (
      <div className={css(styles.body)} ref={ref}>
        {children}
      </div>
    );
  },
);
