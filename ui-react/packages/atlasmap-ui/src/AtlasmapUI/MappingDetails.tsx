import { BaseSizes, Title, TitleLevel } from '@patternfly/react-core';
import { FunctionComponent } from 'react';
import React from 'react';
import {IMappings} from "../CanvasView";

export interface IMappingDetailsProps {
  mapping: IMappings;
}

export const MappingDetails: FunctionComponent<IMappingDetailsProps> = ({
  mapping,
}) => {
  return (
    <div>
      <div className="pf-u-m-lg">
        <Title headingLevel={TitleLevel.h1} size={BaseSizes['2xl']}>
          Selected item: {mapping.name}
        </Title>
        <p>Short description of the selected item.</p>
      </div>
      <div className="pf-u-mx-lg">
        <p>{mapping.id}</p>
      </div>
    </div>
  );
};
