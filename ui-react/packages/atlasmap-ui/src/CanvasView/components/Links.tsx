import React, { FunctionComponent } from 'react';
import { CanvasLink } from '../../Canvas';
import { useCanvasViewLayoutContext } from '../CanvasViewLayoutProvider';
import { IMapping } from '../models';
import { useMappingLinks } from './useMappingLinks';
import { useSourceTargetLinks } from './useSourceTargetLinks';

export interface ILinksProps {
  mappings: IMapping[];
  selectedMapping: string | undefined;
}
export const Links: FunctionComponent<ILinksProps> = ({
  mappings,
  selectedMapping,
}) => {
  const { showMappingColumn } = useCanvasViewLayoutContext();
  const { links: smtLinks } = useMappingLinks({ mappings, selectedMapping });
  const { links: stLinks } = useSourceTargetLinks({
    mappings,
    selectedMapping,
  });
  return (
    <g>
      {(showMappingColumn ? smtLinks : stLinks).map(
        ({ id, start, end, color }) => (
          <CanvasLink key={id} start={start} end={end} color={color} />
        )
      )}
    </g>
  );
};
