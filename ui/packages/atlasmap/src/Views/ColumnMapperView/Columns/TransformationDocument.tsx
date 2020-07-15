import React, { FunctionComponent, useContext } from "react";
import { ViewContext } from "../../ViewProvider";
import { IAtlasmapMapping } from "../../models";

export interface ITransformationDocumentProps {
  mapping: IAtlasmapMapping;
}

export const TransformationDocument: FunctionComponent<ITransformationDocumentProps> = ({
  mapping,
}) => {
  const context = useContext(ViewContext);

  if (!mapping.mapping.transition.expression) {
    context!.initializeActiveMappingExpression();
  }
  const expression = mapping.mapping.transition.expression;
  console.log("expression: ", expression);
  return <div></div>;
};
