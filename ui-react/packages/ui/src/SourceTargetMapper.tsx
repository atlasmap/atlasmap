import { Title } from '@patternfly/react-core';
import { Box } from '@src/Box';
import { Canvas } from '@src/Canvas';
import { CanvasLink, CanvasLinkCoord } from '@src/CanvasLink';
import { CanvasObject } from '@src/CanvasObject';
import { FieldGroupList } from '@src/FieldGroupList';
import { FieldGroup } from '@src/FieldsGroup';
import { useMappingDetails } from '@src/MapperContext';
import React, {
  ReactElement,
  useCallback,
  useEffect,
  useRef,
  useState,
} from 'react';
import { scaleSequential } from 'd3-scale';
import { interpolateRainbow } from 'd3-scale-chromatic';

type FieldId = string;
type GroupId = string;

export interface FieldElement {
  id: FieldId;
  element: ReactElement;
}

export interface FieldsGroup {
  id: GroupId;
  title: ReactElement | string;
  fields: (FieldElement | FieldsGroup)[];
}

export interface Mapping {
  id: string;
  sourceFields: FieldId[];
  targetFields: FieldId[];
}

export interface IMappingCanvasProps {
  width: number;
  height: number;
  zoom: number;
  sources: FieldsGroup[];
  targets: FieldsGroup[];
  mappings: Mapping[];
}

export type SourceTargetLine = {
  start: CanvasLinkCoord;
  end: CanvasLinkCoord;
  color: string;
};

export const SourceTargetMapper: React.FunctionComponent<
  IMappingCanvasProps
> = ({ width, height, zoom = 1, sources, targets, mappings }) => {
  const svgRef = useRef<SVGSVGElement | null>(null);
  const svgOffset = useRef<{ offsetTop: number; offsetLeft: number }>({
    offsetTop: 0,
    offsetLeft: 0,
  });

  const fieldsRef = useRef<{ [id: string]: { ref: HTMLDivElement, groupId: string } }>({});
  const addFieldRef = (ref: HTMLDivElement, fieldId: FieldId, groupId: string) => {
    fieldsRef.current[fieldId] = { ref, groupId };
  };
  const fieldsGroupRef = useRef<{ [id: string]: HTMLElement }>({});
  const addFieldsGroupRef = (ref: HTMLElement, id: string) => {
    fieldsGroupRef.current[id] = ref;
  };
  const aBoxRef = useRef<HTMLDivElement | null>(null);
  const bBoxRef = useRef<HTMLDivElement | null>(null);

  const colors = scaleSequential(interpolateRainbow).domain([0, mappings.length]);

  const gutter = 20;
  const boxWidth = Math.max(200, width / 2 - gutter * 3);
  const boxHeight = Math.max(300, height - gutter * 3);
  const startY = gutter;
  const boxAstartX = gutter;
  const boxBstartX = Math.max(width / 2, boxWidth + gutter) + gutter * 2;

  const [lines, setLines] = useState<SourceTargetLine[]>([]);


  const showMappingDetails = useMappingDetails();

  const calcLines = useCallback(() => {
    const aBox = aBoxRef.current;
    const bBox = bBoxRef.current;
    if (aBox && bBox) {
      const aBoxRect = aBox.getBoundingClientRect();
      const bBoxRect = bBox.getBoundingClientRect();
      const isSourceOnTheLeft =
        aBoxRect.left < bBoxRect.right;

      const makeCoords = (
        connectOnRight: boolean,
        isVisible: boolean,
        box: HTMLElement,
        boxRect: ClientRect | DOMRect,
        elRect: ClientRect | DOMRect,
        parentRect: ClientRect | DOMRect,
      ) => ({
        x:
          (connectOnRight ? boxRect.right : boxRect.left) -
          svgOffset.current.offsetLeft,
        y: Math.min(
          Math.max(
            (isVisible ? elRect.top : parentRect.top) -
            svgOffset.current.offsetTop +
            (isVisible ? elRect.height : parentRect.height) / 2,
            boxRect.top - svgOffset.current.offsetTop
          ),
          box.clientHeight +
          boxRect.top -
          svgOffset.current.offsetTop
        ),
      });

      const newLines = mappings.reduce<SourceTargetLine[]>(
        (lines, {sourceFields, targetFields}, idx) => {
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
                const {ref: aRef, groupId: aGroupId} = fieldsRef.current[a] || {};
                const {ref: bRef, groupId: bGroupId} = fieldsRef.current[b] || {};
                const aParentRef = fieldsGroupRef.current[aGroupId];
                const bParentRef = fieldsGroupRef.current[bGroupId];
                if (aRef && bRef && aParentRef && bParentRef) {
                  const aRect = aRef.getBoundingClientRect();
                  const bRect = bRef.getBoundingClientRect();
                  const aParentRect = aParentRef.getBoundingClientRect();
                  const bParentRect = bParentRef.getBoundingClientRect();
                  const isAVisible = (aRef.parentNode as HTMLElement).clientHeight > 0;
                  const isBVisible = (bRef.parentNode as HTMLElement).clientHeight > 0;

                  return {
                    start: makeCoords(isSourceOnTheLeft, isAVisible, aBox, aBoxRect, aRect, aParentRect),
                    end: makeCoords(!isSourceOnTheLeft, isBVisible, bBox, bBoxRect, bRect, bParentRect),
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
  }, [mappings, fieldsRef, fieldsGroupRef, svgOffset, aBoxRef, bBoxRef]);

  useEffect(() => {
    const requestId = requestAnimationFrame(() => {
      calcLines();
    });
    return () => {
      cancelAnimationFrame(requestId);
    };
  }, [calcLines, width, height, zoom]);

  useEffect(() => {
    const requestId = requestAnimationFrame(() => {
      if (svgRef.current) {
        const { top, left } = svgRef.current.getBoundingClientRect();
        svgOffset.current.offsetTop = top;
        svgOffset.current.offsetLeft = left;
      }
      calcLines();
    });
    return () => {
      cancelAnimationFrame(requestId);
    };
  }, [svgRef, svgOffset]);

  const makeFieldGroup = (type: string, { id, title, fields }: FieldsGroup) => (
    <FieldGroup key={id} id={id} title={title} onLayout={calcLines} ref={el => el && addFieldsGroupRef(el, `${type}-${id}`)}>
      {fields.map((f, fdx) => (
        <div
          onClick={() => (f as FieldElement).element && showMappingDetails(f.id)}
          style={{
            padding: `calc(0.3rem * ${zoom}) 0`,
            borderTop: '1px solid #eee',
            borderBottom: '1px solid #eee',
            marginTop: '-1px',
            fontSize: `${zoom}rem`,
          }}
          key={f.id || fdx}
          ref={el => el && f.id && addFieldRef(el, f.id, `${type}-${id}`)}
        >
          {(f as FieldElement).element || makeFieldGroup(type, f as FieldsGroup)}
        </div>
      ))}
    </FieldGroup>
  );

  const makeSourceFieldGroup = (f: FieldsGroup) => makeFieldGroup('source', f);
  const makeTargetFieldGroup = (f: FieldsGroup) => makeFieldGroup('target', f);

  return (
    <Canvas ref={svgRef} width={width} height={height} zoom={zoom}>
      {lines.map(({ start, end, color }, idx) => (
        <CanvasLink key={idx} start={start} end={end} color={color} />
      ))}

      <CanvasObject
        width={boxWidth}
        height={boxHeight}
        x={boxAstartX}
        y={startY}
      >
        <Box
          header={<Title size={'2xl'} headingLevel={'h2'}>Source</Title>}
          footer={<p>{sources.length} fields</p>}
          ref={aBoxRef}
          onLayout={calcLines}
        >
          <FieldGroupList>
            {sources.map(makeSourceFieldGroup)}
          </FieldGroupList>
        </Box>
      </CanvasObject>

      <CanvasObject
        width={boxWidth}
        height={boxHeight}
        x={boxBstartX}
        y={startY}
      >
        <Box
          header={<Title size={'2xl'} headingLevel={'h2'}>Target</Title>}
          footer={<p>{targets.length} fields</p>}
          ref={bBoxRef}
          onLayout={calcLines}
        >
          <FieldGroupList>
            {targets.map(makeTargetFieldGroup)}
          </FieldGroupList>
        </Box>
      </CanvasObject>
    </Canvas>
  );
};
