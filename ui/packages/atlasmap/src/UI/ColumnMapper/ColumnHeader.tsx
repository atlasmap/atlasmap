import React, { FunctionComponent, ReactElement } from "react";

import { Actions } from "../Actions";

import { BaseSizes, Title } from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  header: {
    flex: "0 1 0",
  },
  plain: {
    background: "transparent",
    border: "0 none transparent",
  },
  toolbar: {
    background: "var(--pf-global--BackgroundColor--150)",
    border: "1px solid var(--pf-global--BorderColor--100)",
    borderBottom: 0,
    padding: "var(--pf-global--spacer--sm) var(--pf-global--spacer--md)",
    boxSizing: "initial",
    display: "flex",
    flexFlow: "row no-wrap",
  },
  title: {
    display: "flex",
    alignItems: "center",
    flex: "1 0 auto",
    marginRight: "var(--pf-global--spacer--sm)",
    height: "36px",
  },
});

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
          <Title size={BaseSizes.lg}>{title}</Title>
        </div>
        <Actions>{actions?.filter((a) => a)}</Actions>
      </div>
      {children}
    </div>
  );
};
