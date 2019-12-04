import React, { FunctionComponent } from 'react';

export interface IDocumentFieldProps {
  name: string;
  type: string;
  showType: boolean;
}

export const DocumentField: FunctionComponent<IDocumentFieldProps> = ({ name, type, showType }) => {
  return (
    <>{name} {showType && `(${type})`}</>
  );
};
