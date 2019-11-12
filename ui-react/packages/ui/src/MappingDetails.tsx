import { BaseSizes, Title, TitleLevel } from '@patternfly/react-core';
import { TopologySideBar } from '@patternfly/react-topology';
import { FunctionComponent } from 'react';
import React from 'react';

export interface IMappingDetailsProps {
  show: boolean;
  onClose: () => void;
}

export const MappingDetails: FunctionComponent<IMappingDetailsProps> = ({
  show,
  onClose,
  children,
}) => {
  const header = (
    <div className="pf-u-m-lg">
      <Title headingLevel={TitleLevel.h1} size={BaseSizes['2xl']}>
        Selected item: {children}
      </Title>
      <p>Short description of the selected item.</p>
    </div>
  );

  return (
    <TopologySideBar show={show} onClose={onClose} header={header}>
      <div className="pf-u-mx-lg">
        <p>{children}</p>
      </div>
    </TopologySideBar>
  );
};
