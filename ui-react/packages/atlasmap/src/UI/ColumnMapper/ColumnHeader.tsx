import React, { FunctionComponent, ReactElement } from "react";

import { Actions } from "../Actions";

import { BaseSizes, Level, LevelItem, Title } from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  header: {
    flex: "0 1 0",
  },
  toolbar: {
    background: "var(--pf-global--BackgroundColor--150)",
    borderBottom: "1px solid var(--pf-global--BorderColor--100)",
    padding: "1rem calc(2rem + 10px) 1rem 2rem",
    boxSizing: "initial",
    "& > *:not(:last-child)": {
      marginBottom: "1rem !important",
    },
  },
  title: {
    display: "flex",
    alignItems: "center",
    height: "36px",
    paddingLeft: "1rem",
  },
  searchRow: {
    margin: "1rem calc(2rem + 10px) 0 2rem",
  },
});

export interface IColumnHeaderProps {
  title: string;
  actions?: (ReactElement | null)[];
}

export const ColumnHeader: FunctionComponent<IColumnHeaderProps> = ({
  title,
  actions,
  children,
}) => {
  return (
    <div className={css(styles.header)}>
      <div className={css(styles.toolbar)}>
        <Level gutter="md">
          <LevelItem className={css(styles.title)}>
            <Title size={BaseSizes.lg}>{title}</Title>
          </LevelItem>
          <LevelItem>
            <Actions>{actions?.filter((a) => a)}</Actions>
          </LevelItem>
        </Level>
      </div>
      {children && <div className={css(styles.searchRow)}>{children}</div>}
    </div>
  );
};
