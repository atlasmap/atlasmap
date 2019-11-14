import { SourceTargetNodes, useCanvasLinks } from '@src/canvas';
import { IMappings } from '@src/models';
import { scaleSequential } from 'd3-scale';
import { interpolateRainbow } from 'd3-scale-chromatic';
import { useMemo } from 'react';

export interface IUseMappingsLinksArgs {
  mappings: IMappings[];
}
export function useMappingLinks({ mappings }: IUseMappingsLinksArgs) {
  const colors = useMemo(
    () => scaleSequential(interpolateRainbow).domain([0, mappings.length]),
    [mappings]
  );

  const linkedNodes = mappings.reduce<SourceTargetNodes[]>(
    (lines, { id, sourceFields, targetFields }, idx) => {
      const color = colors(idx);
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

      return [
        ...lines,
        ...sourcesToMappings,
        ...mappingsToTargets,
      ]
    },
    []
  );
  return useCanvasLinks(linkedNodes);
}
