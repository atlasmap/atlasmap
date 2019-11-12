import { SourceTargetNodes, useCanvasLinks } from '@src/canvas';
import { Mapping } from '@src/models';
import { scaleSequential } from 'd3-scale';
import { interpolateRainbow } from 'd3-scale-chromatic';
import { useMemo } from 'react';

export interface IUseMappingsLinksArgs {
  mappings: Mapping[]
}
export function useMappingLinks({ mappings }: IUseMappingsLinksArgs) {
  const colors = useMemo(() => scaleSequential(interpolateRainbow).domain([
    0,
    mappings.length,
  ]), [mappings]);

  const linkedNodes = mappings.reduce<SourceTargetNodes[]>(
    (lines, { sourceFields, targetFields }, idx) => {
      const color = colors(idx);
      const mappingLines = sourceFields.reduce<SourceTargetNodes[]>(
        (lines, start) => {
          const linesFromSource = targetFields.map(
            end => ({ start, end, color })
          );
          return [...lines, ...linesFromSource];
        },
        []
      );
      return [
        ...lines,
        ...mappingLines
      ];
    },
    []
  );
  return useCanvasLinks(linkedNodes);
}