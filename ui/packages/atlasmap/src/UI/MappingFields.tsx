import React, { FunctionComponent } from "react";

import { ExpandableSection } from "@patternfly/react-core";
import { css } from "@patternfly/react-styles";

import { useToggle } from "./useToggle";
import styles from "./MappingFields.css";

export interface IMappingFieldsProps {
  title: string;
}

export const MappingFields: FunctionComponent<IMappingFieldsProps> = ({
  title,
  children,
}) => {
  const { state: expanded, toggle: toggleExpanded } = useToggle(true);
  return (
    <div className={css(styles.wrapper)}>
      <ExpandableSection
        isExpanded={expanded}
        onToggle={toggleExpanded}
        data-testid={`mapping-fields-detail-${title}-toggle`}
        toggleText={title}
      >
        {children}
      </ExpandableSection>
    </div>
  );
};
