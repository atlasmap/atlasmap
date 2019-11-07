import { Box } from '@src/Box';
import { Canvas } from '@src/Canvas';
import { CanvasLink, CanvasLinkCoord } from '@src/CanvasLink';
import { CanvasObject } from '@src/CanvasObject';
import { useMappingDetails } from '@src/MapperContext';
import React, {
  ReactElement,
  useCallback,
  useEffect,
  useRef,
  useState,
} from 'react';
import { scaleSequential } from 'd3-scale';
import { interpolateMagma } from 'd3-scale-chromatic';

type FieldId = string;

export interface Field {
  id: FieldId;
  element: ReactElement;
}

export interface FieldsGroup {
  title: string;
  fields: Field[];
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
  const aBoxRef = useRef<HTMLDivElement | null>(null);
  const bBoxRef = useRef<HTMLDivElement | null>(null);

  const colors = scaleSequential(interpolateMagma).domain([0, mappings.length]);

  const gutter = 50;
  const boxWidth = Math.max(200, width / 2 - gutter * 2);
  const boxHeight = Math.max(300, height - gutter * 3);
  const startY = gutter;
  const boxAstartX = gutter;
  const boxBstartX = Math.max(width / 2, boxWidth + gutter) + gutter;

  const [lines, setLines] = useState<SourceTargetLine[]>([]);

  const fieldsRef = useRef<{ [id: string]: HTMLDivElement }>({});
  const addRef = (ref: HTMLDivElement, id: FieldId) => {
    fieldsRef.current[id] = ref;
  };

  const showMappingDetails = useMappingDetails();

  const calcLines = useCallback(() => {
    const aBox = aBoxRef.current;
    const bBox = bBoxRef.current;
    if (aBox && bBox) {
      const aParentRect = aBox.getBoundingClientRect();
      const bParentRect = bBox.getBoundingClientRect();

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
            ...mappingLines
              .map(([a, b]): SourceTargetLine | null => {
                const aRef = fieldsRef.current[a];
                const bRef = fieldsRef.current[b];
                if (aRef && bRef) {
                  const aRect = aRef.getBoundingClientRect();
                  const bRect = bRef.getBoundingClientRect();
                  const isSourceOnTheLeft =
                    aRect.left < bRect.left + bRect.width;
                  return {
                    start: {
                      x:
                        aRect.left +
                        (isSourceOnTheLeft ? aBox.offsetWidth : 0) -
                        svgOffset.current.offsetLeft,
                      y: Math.min(
                        Math.max(
                          aRect.top -
                            svgOffset.current.offsetTop +
                            aRect.height / 2,
                          aParentRect.top - svgOffset.current.offsetTop
                        ),
                        aBox.clientHeight +
                          aParentRect.top -
                          svgOffset.current.offsetTop
                      ),
                    },
                    end: {
                      x:
                        bRect.left +
                        (isSourceOnTheLeft ? 0 : bBox.offsetWidth) -
                        svgOffset.current.offsetLeft,
                      y: Math.min(
                        Math.max(
                          bRect.top -
                            svgOffset.current.offsetTop +
                            bRect.height / 2,
                          bParentRect.top - svgOffset.current.offsetTop
                        ),
                        bBox.clientHeight +
                          bParentRect.top -
                          svgOffset.current.offsetTop
                      ),
                    },
                    color,
                  };
                }
                return null;
              })
              .filter(a => a) as SourceTargetLine[],
          ];
        },
        []
      );
      setLines(newLines);
    }
  }, [mappings, fieldsRef, svgOffset, aBoxRef, bBoxRef]);

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
          header={<h1>Source</h1>}
          footer={<p>{sources.length} fields</p>}
          ref={aBoxRef}
          onChanges={calcLines}
        >
          {sources.map(({ title, fields }, idx) => (
            <div key={idx}>
              <h2>{title}</h2>
              {fields.map((f) =>
                <div
                  onClick={() => showMappingDetails(f.id)}
                  style={{
                    padding: '0.3rem',
                    borderBottom: '1px solid #eee',
                    fontSize: `${zoom}rem`,
                  }}
                  key={f.id}
                  ref={el => el && addRef(el, f.id)}
                >
                  {f.element}
                </div>
              )}
            </div>
          ))}
        </Box>
      </CanvasObject>

      <CanvasObject
        width={boxWidth}
        height={boxHeight}
        x={boxBstartX}
        y={startY}
      >
        <Box
          header={<h1>Target</h1>}
          footer={<p>{targets.length} fields</p>}
          ref={bBoxRef}
          onChanges={calcLines}
        >
          {targets.map(({ title, fields }, idx) => (
            <div key={idx}>
              <h2>{title}</h2>
              {fields.map((f) =>
                <div
                  onClick={() => showMappingDetails(f.id)}
                  style={{
                    padding: '0.3rem',
                    borderBottom: '1px solid #eee',
                    fontSize: `${zoom}rem`,
                  }}
                  key={f.id}
                  ref={el => el && addRef(el, f.id)}
                >
                  {f.element}
                </div>
              )}
            </div>
          ))}
        </Box>
      </CanvasObject>
    </Canvas>
  );
};
