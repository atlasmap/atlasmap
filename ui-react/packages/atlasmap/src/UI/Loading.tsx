import {
  EmptyState,
  EmptyStateIcon,
  EmptyStateVariant,
  Title,
} from "@patternfly/react-core";
import { Spinner } from "@patternfly/react-core/dist/js/experimental";
import React, { FunctionComponent } from "react";

export const Loading: FunctionComponent = () => (
  <EmptyState variant={EmptyStateVariant.full}>
    <EmptyStateIcon variant="container" component={Spinner} />
    <Title size="lg">Loading</Title>
  </EmptyState>
);
