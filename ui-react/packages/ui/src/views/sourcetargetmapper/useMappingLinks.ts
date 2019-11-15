import { scaleSequential } from 'd3-scale';
import { interpolateRainbow } from 'd3-scale-chromatic';
import { useMemo } from 'react';
import { SourceTargetNodes, useCanvasLinks } from '../../canvas';
import { IMappings } from '../../models';
import { useSourceTargetMapper } from './SourceTargetMapperContext';

export interface IUseMappingsLinksArgs {
  mappings: IMappings[];
}
export function useMappingLinks({ mappings }: IUseMappingsLinksArgs) {
  const { focusedMapping } = useSourceTargetMapper();

  const colors = useMemo(
    () => scaleSequential(interpolateRainbow).domain([0, mappings.length]),
    [mappings]
  );

  const linkedNodes = mappings.reduce<SourceTargetNodes[]>(
    (lines, { id, sourceFields, targetFields }, idx) => {
      const isMappingSelected = id === focusedMapping;
      const color = focusedMapping ? (
        isMappingSelected ? '#06c' : '#ddd'
      ) : colors(idx);
      const sourcesToMappings = sourceFields.map(source => ({
        start: source,
        end: `to-${id}`,
        color
      }));
      const mappingsToTargets = targetFields.map(target => ({
        start: `from-${id}`,
        end: target,
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
    []
  );
  return useCanvasLinks(linkedNodes);
}
