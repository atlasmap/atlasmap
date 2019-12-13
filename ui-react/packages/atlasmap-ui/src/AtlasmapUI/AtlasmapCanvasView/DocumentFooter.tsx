import React, { FunctionComponent } from 'react';

export interface IDocumentFooterProps {
  title: string;
  type: string;
  showType: boolean;
}

export const DocumentFooter: FunctionComponent<IDocumentFooterProps> = ({
  title,
  type,
  showType,
}) => {
  return (
    <>
      {title} {showType && `(${type})`}
    </>
  );
};
