import React, { FunctionComponent } from "react";

import { Expandable } from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";

import { useToggle } from "./useToggle";

export interface IMappingFieldsProps {
  title: string;
}

const styles = StyleSheet.create({
  wrapper: {
    background: "var(--pf-global--BackgroundColor--200)",
    padding: "1rem",
    margin: "1rem 0",
    "& .pf-c-expandable__content": {
      margin: "0 !important",
    },
  },
});

export const MappingFields: FunctionComponent<IMappingFieldsProps> = ({
  title,
  children,
}) => {
  const { state: expanded, toggle: toggleExpanded } = useToggle(true);
  return (
    <div className={css(styles.wrapper)}>
      <Expandable
        isExpanded={expanded}
        onToggle={toggleExpanded}
        data-testid={`mapping-fields-detail-${title}-toggle`}
        toggleText={title}
      >
        {children}
      </Expandable>
    </div>
  );
};
