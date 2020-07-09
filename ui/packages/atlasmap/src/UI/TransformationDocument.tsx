import React, { FunctionComponent } from "react";
import { IAtlasmapMapping } from "src/Views";
import { Card, Title } from "@patternfly/react-core";

export interface ITransformationDocumentProps {
  mapping: IAtlasmapMapping;
  isSelected: boolean;
}

export const TransformationDocument: FunctionComponent<ITransformationDocumentProps> = ({
  mapping,
  isSelected,
}) => {
  return (
    <div>
      <Card
        isCompact
        isSelectable
        isSelected={isSelected}
        aria-label={mapping.name}
      >
        <Title size={"lg"} headingLevel={"h2"} aria-label={mapping.name}>
          {mapping.name}
        </Title>
      </Card>
    </div>
  );
};
