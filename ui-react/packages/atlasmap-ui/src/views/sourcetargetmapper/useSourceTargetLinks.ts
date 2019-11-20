import { scaleSequential } from 'd3-scale';
import { interpolateRainbow } from 'd3-scale-chromatic';
import { useMemo } from 'react';
import { SourceTargetNodes, useCanvasLinks } from '../../canvas';
import { IMappings } from '../../models';

export interface IUseSourceTargetLinksArgs {
  mappings: IMappings[];
}
export function useSourceTargetLinks({ mappings }: IUseSourceTargetLinksArgs) {
  const colors = useMemo(
    () => scaleSequential(interpolateRainbow).domain([0, mappings.length]),
    [mappings]
  );

  const linkedNodes = mappings.reduce<SourceTargetNodes[]>(
    (lines, { sourceFields, targetFields }, idx) => {
      const color = colors(idx);
      const mappingLines = sourceFields.reduce<SourceTargetNodes[]>(
        (lines, start) => {
          const linesFromSource = targetFields.map(end => ({
            start: start.id,
            end: end.id,
            color,
          }));
          return [...lines, ...linesFromSource];
        },
        []
      );
      return [...lines, ...mappingLines];
    },
    []
  );
  return useCanvasLinks(linkedNodes);
}
