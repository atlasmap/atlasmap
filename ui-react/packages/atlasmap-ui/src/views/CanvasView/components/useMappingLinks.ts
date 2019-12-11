import { scaleSequential } from 'd3-scale';
import { interpolateRainbow } from 'd3-scale-chromatic';
import { useMemo } from 'react';
import { SourceTargetNodes, useCanvasLinks } from '../../../canvas';
import { IMappings } from '../models';

export interface IUseMappingsLinksArgs {
  mappings: IMappings[];
  selectedMapping: string | undefined;
}
export function useMappingLinks({ mappings, selectedMapping }: IUseMappingsLinksArgs) {
  const colors = useMemo(
    () => scaleSequential(interpolateRainbow).domain([0, mappings.length]),
    [mappings]
  );

  const linkedNodes = useMemo(() =>
    mappings.reduce<SourceTargetNodes[]>(
    (lines, { id, sourceFields, targetFields }, idx) => {
      const isMappingSelected = id === selectedMapping;
      const color = selectedMapping ? (
        isMappingSelected ? 'var(--pf-global--active-color--400)' : '#ccc'
      ) : colors(idx);
      const sourcesToMappings = sourceFields.map(source => ({
        start: source.id,
        end: `to-${id}`,
        color
      }));
      const mappingsToTargets = targetFields.map(target => ({
        start: `from-${id}`,
        end: target.id,
        color
      }));

      return isMappingSelected ? [
        ...lines,
        ...mappingsToTargets,
        ...sourcesToMappings,
      ]: [
        ...sourcesToMappings,
        ...mappingsToTargets,
        ...lines,
      ]
    },
    [
      {
        start: 'dragsource',
        end: 'dragtarget',
        color: 'var(--pf-global--active-color--400)'
      }
    ]
  ),
    [colors, mappings, selectedMapping]
  );
  return useCanvasLinks(linkedNodes);
}
