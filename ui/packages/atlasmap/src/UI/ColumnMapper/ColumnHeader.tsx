import React, { FunctionComponent, ReactElement } from "react";

import { Actions } from "../Actions";

import { Title } from "@patternfly/react-core";
import { css } from "@patternfly/react-styles";

import styles from "./ColumnHeader.css";

export interface IColumnHeaderProps {
  title: string;
  variant?: "default" | "plain";
  actions?: (ReactElement | null | undefined)[];
}

export const ColumnHeader: FunctionComponent<IColumnHeaderProps> = ({
  title,
  actions,
  variant,
  children,
}) => {
  return (
    <div className={css(styles.header)}>
      <div className={css(styles.toolbar, variant === "plain" && styles.plain)}>
        <div className={css(styles.title)}>
          <Title headingLevel="h2" size="lg">
            {title}
          </Title>
        </div>
        <Actions>{actions?.filter((a) => a)}</Actions>
      </div>
      {children}
    </div>
  );
};
