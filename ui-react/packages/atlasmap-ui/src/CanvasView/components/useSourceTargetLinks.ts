import { scaleSequential } from 'd3-scale';
import { interpolateRainbow } from 'd3-scale-chromatic';
import { useMemo } from 'react';
import { SourceTargetNodes, useCanvasLinks } from '../../Canvas';
import { IMapping } from '../models';

export interface IUseSourceTargetLinksArgs {
  mappings: IMapping[];
  selectedMapping: string | undefined;
}
export function useSourceTargetLinks({
  selectedMapping,
  mappings,
}: IUseSourceTargetLinksArgs) {
  const colors = useMemo(
    () => scaleSequential(interpolateRainbow).domain([0, mappings.length]),
    [mappings]
  );

  const linkedNodes = mappings.reduce<SourceTargetNodes[]>(
    (lines, { id, sourceFields, targetFields }, idx) => {
      const isMappingSelected = id === selectedMapping;
      const color = selectedMapping
        ? isMappingSelected
          ? 'var(--pf-global--active-color--400)'
          : '#ccc'
        : colors(idx);
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
      return isMappingSelected
        ? [...lines, ...mappingLines]
        : [...mappingLines, ...lines];
    },
    []
  );
  return useCanvasLinks(linkedNodes);
}
