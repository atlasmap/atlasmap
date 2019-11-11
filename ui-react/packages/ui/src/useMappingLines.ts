import { useCanvasInfo } from '@src/canvas';
import { FieldId, Mapping, SourceTargetLine } from '@src/models';
import { scaleSequential } from 'd3-scale';
import { interpolateRainbow } from 'd3-scale-chromatic';
import { useCallback, useEffect, useRef, useState } from 'react';

export interface IUseMappingLinesArgs {
  sourcesContainerRect: ClientRect | DOMRect | null;
  targetsContainerRect: ClientRect | DOMRect | null;
  mappings: Mapping[];
}
export function useMappingLines({
  mappings,
  sourcesContainerRect,
  targetsContainerRect,
}: IUseMappingLinesArgs) {
  const { width, height, zoom, offsetLeft, offsetTop } = useCanvasInfo();
  const [lines, setLines] = useState<SourceTargetLine[]>([]);

  const fieldsRef = useRef<{
    [id: string]: { ref: HTMLDivElement; groupId: string };
  }>({});
  const addFieldRef = (
    ref: HTMLDivElement,
    fieldId: FieldId,
    groupId: string
  ) => {
    fieldsRef.current[fieldId] = { ref, groupId };
  };
  const fieldsGroupRef = useRef<{ [id: string]: HTMLElement }>({});
  const addFieldsGroupRef = (ref: HTMLElement, id: string) => {
    fieldsGroupRef.current[id] = ref;
  };

  const colors = scaleSequential(interpolateRainbow).domain([
    0,
    mappings.length,
  ]);

  const calcLines = useCallback(() => {
    if (sourcesContainerRect && targetsContainerRect) {
      const isSourceOnTheLeft = sourcesContainerRect.left < targetsContainerRect.right;

      const makeCoords = (
        connectOnRight: boolean,
        isVisible: boolean,
        boxRect: ClientRect | DOMRect,
        elRect: ClientRect | DOMRect,
        parentRect: ClientRect | DOMRect
      ) => ({
        x: (connectOnRight ? boxRect.right : boxRect.left) - offsetLeft,
        y: Math.min(
          Math.max(
            (isVisible ? elRect.top : parentRect.top) -
              offsetTop +
              (isVisible ? elRect.height : parentRect.height) / 2,
            boxRect.top - offsetTop
          ),
          boxRect.height + boxRect.top - offsetTop
        ),
      });

      const newLines = mappings.reduce<SourceTargetLine[]>(
        (lines, { sourceFields, targetFields }, idx) => {
          const color = colors(idx);
          const mappingLines = sourceFields.reduce(
            (lines, source) => {
              const linesFromSource: [string, string][] = targetFields.map(
                target => {
                  return [source, target];
                }
              );
              return [...lines, ...linesFromSource];
            },
            [] as [string, string][]
          );
          return [
            ...lines,
            ...(mappingLines
              .map(([a, b]): SourceTargetLine | null => {
                const { ref: aRef, groupId: aGroupId } = fieldsRef.current[a] || {};
                const { ref: bRef, groupId: bGroupId } = fieldsRef.current[b] || {};
                const aParentRef = fieldsGroupRef.current[aGroupId];
                const bParentRef = fieldsGroupRef.current[bGroupId];
                if (aRef && bRef && aParentRef && bParentRef) {
                  const aRect = aRef.getBoundingClientRect();
                  const bRect = bRef.getBoundingClientRect();
                  const aParentRect = aParentRef.getBoundingClientRect();
                  const bParentRect = bParentRef.getBoundingClientRect();
                  const isAVisible =
                    (aRef.parentNode as HTMLElement).clientHeight > 0;
                  const isBVisible =
                    (bRef.parentNode as HTMLElement).clientHeight > 0;

                  return {
                    start: makeCoords(
                      isSourceOnTheLeft,
                      isAVisible,
                      sourcesContainerRect,
                      aRect,
                      aParentRect
                    ),
                    end: makeCoords(
                      !isSourceOnTheLeft,
                      isBVisible,
                      targetsContainerRect,
                      bRect,
                      bParentRect
                    ),
                    color,
                  };
                }
                return null;
              })
              .filter(a => a) as SourceTargetLine[]),
          ];
        },
        []
      );
      setLines(newLines);
    }
  }, [
    mappings,
    fieldsRef,
    fieldsGroupRef,
    offsetLeft,
    offsetTop,
    sourcesContainerRect,
    targetsContainerRect,
  ]);

  useEffect(() => {
    calcLines();
  }, [offsetTop, offsetLeft]);

  useEffect(() => {
    const requestId = requestAnimationFrame(() => {
      calcLines();
    });
    return () => {
      cancelAnimationFrame(requestId);
    };
  }, [calcLines, width, height, zoom, offsetTop, offsetLeft]);

  return {
    lines,
    calcLines,
    addFieldRef,
    addFieldsGroupRef,
  };
}
