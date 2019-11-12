import { CanvasLink } from '@src';
import { useMappingLinks } from '@src/mapper/useMappingLinks';
import { Mapping } from '@src/models';
import React, { FunctionComponent } from 'react';

export interface ILinksProps {
  mappings: Mapping[];
}
export const Links: FunctionComponent<ILinksProps> = ({ mappings }) => {
  const { links } = useMappingLinks({ mappings });
  return (
    <g>
      {links.map(({ start, end, color }, idx) => (
        <CanvasLink key={idx} start={start} end={end} color={color} />
      ))}
    </g>
  );
};
