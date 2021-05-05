import {
  EmptyState,
  EmptyStateIcon,
  EmptyStateVariant,
  Title,
} from "@patternfly/react-core";
import React, { FunctionComponent } from "react";
import { Spinner } from "@patternfly/react-core";

export const Loading: FunctionComponent = () => (
  <EmptyState variant={EmptyStateVariant.full}>
    <EmptyStateIcon variant="container" component={Spinner} />
    <Title headingLevel="h2" size="lg">
      Loading
    </Title>
  </EmptyState>
);
