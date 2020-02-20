import { useMemo } from 'react';
import { SourceTargetNodes, useCanvasLinks } from '../../Canvas';
import { IMapping } from '../models';

export interface IUseMappingsLinksArgs {
  mappings: IMapping[];
  selectedMapping: string | undefined;
}
export function useMappingLinks({
  mappings,
  selectedMapping,
}: IUseMappingsLinksArgs) {
  const linkedNodes = useMemo(
    () =>
      mappings.reduce<SourceTargetNodes[]>(
        (lines, { id, sourceFields, targetFields }) => {
          const isMappingSelected = id === selectedMapping;
          const color = selectedMapping
            ? isMappingSelected
              ? 'var(--pf-global--active-color--400)'
              : '#ccc'
            : 'grey';
          const sourcesToMappings = sourceFields.map(source => ({
            start: source.id,
            end: `to-${id}`,
            color,
          }));
          const mappingsToTargets = targetFields.map(target => ({
            start: `from-${id}`,
            end: target.id,
            color,
          }));

          return isMappingSelected
            ? [...lines, ...mappingsToTargets, ...sourcesToMappings]
            : [...sourcesToMappings, ...mappingsToTargets, ...lines];
        },
        [
          {
            start: 'dragsource',
            end: 'dragtarget',
            color: 'var(--pf-global--active-color--400)',
          },
        ]
      ),
    [mappings, selectedMapping]
  );
  return useCanvasLinks(linkedNodes);
}
