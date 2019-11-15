import React, { FunctionComponent } from 'react';
import { CanvasLink } from '../../canvas';
import { IMappings } from '../../models';
import { useMappingLinks } from './useMappingLinks';

export interface ILinksProps {
  mappings: IMappings[];
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
