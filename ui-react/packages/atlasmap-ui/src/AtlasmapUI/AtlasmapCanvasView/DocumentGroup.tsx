import React, { FunctionComponent } from 'react';


export interface IDocumentGroupProps {
  name: string;
  type: string;
  showType: boolean;
}

export const DocumentGroup: FunctionComponent<IDocumentGroupProps> = ({
  name,
  type,
  showType,
}) => {
  return (
    <>
      {name}
      {showType && ` (${type})`}
    </>
  );
};
