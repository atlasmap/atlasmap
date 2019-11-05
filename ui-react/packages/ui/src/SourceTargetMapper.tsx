import { Box } from '@src/Box';
import { useMappingDetails } from '@src/MapperContext';
import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { linkHorizontal } from 'd3-shape';
import { scaleLinear, scaleSequential } from 'd3-scale';
import { interpolateMagma } from 'd3-scale-chromatic';

export interface IMappingCanvasProps {
  width: number;
  height: number;
  zoom?: number;
}

const as = [
  'A',
  'a',
  'B',
  'b',
  'C',
  'c',
  'D',
  'd',
  'E',
  'e',
  'F',
  'f',
  'G',
  'g',
  'H',
  'h',
  'I',
  'i',
  'J',
  'j',
  'K',
  'k',
  'L',
  'l',
  'M',
  'm',
  'N',
  'n',
  'O',
  'o',
  'P',
  'p',
  'Q',
  'q',
  'R',
  'r',
  'S',
  's',
  'T',
  't',
  'U',
  'u',
  'V',
  'v',
  'W',
  'w',
  'X',
  'x',
  'Y',
  'y',
  'Z',
  'z',
];
const bs = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'];
const mappings = [['A', '10'], ['B', '1'], ['e', '5'], ['Z', '7'], ['X', '3']];

export const SourceTargetMapper: React.FunctionComponent<
  IMappingCanvasProps
> = ({ width, height, zoom = 1 }) => {
  const svgRef = useRef<SVGSVGElement | null>(null);
  const svgOffset = useRef<{ offsetTop: number; offsetLeft: number }>({
    offsetTop: 0,
    offsetLeft: 0,
  });
  const aBoxRef = useRef<HTMLDivElement | null>(null);
  const bBoxRef = useRef<HTMLDivElement | null>(null);

  const x = useMemo(
    () =>
      scaleLinear()
        .range([0, width])
        .domain([0, width * zoom]),
    [width, zoom]
  );
  const y = useMemo(
    () =>
      scaleLinear()
        .range([height, 0])
        .domain([height * zoom, 0]),
    [width, zoom]
  );

  const colors = scaleSequential(interpolateMagma).domain([0, mappings.length]);

  const link = linkHorizontal()
    .context(null)
    .x(d => x.invert(d[0]))
    .y(d => y.invert(d[1]));

  const gutter = 50;
  const boxWidth = x.invert(width / 2 - gutter * 2);
  const boxHeight = x.invert(height - gutter * 2);
  const startY = y.invert(gutter);
  const boxAstartX = x.invert(gutter);
  const boxBstartX = x.invert(width / 2 + gutter);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
  }, []);

  const [lines, setLines] = useState<string[]>([]);

  const fieldsRef = useRef<{ [id: string]: HTMLDivElement }>({});
  const addRef = (ref: HTMLDivElement, id: string) => {
    fieldsRef.current[id] = ref;
  };

  const showMappingDetails = useMappingDetails();

  const calcLines = useCallback(() => {
    const aBox = aBoxRef.current;
    const bBox = bBoxRef.current;
    if (aBox && bBox) {
      const aParentRect = aBox.getBoundingClientRect();
      const bParentRect = bBox.getBoundingClientRect();

      const newLines = mappings
        .map(([a, b]) => {
          const aRef = fieldsRef.current[a];
          const bRef = fieldsRef.current[b];
          if (aRef && bRef) {
            const aRect = aRef.getBoundingClientRect();
            const bRect = bRef.getBoundingClientRect();
            const isSourceOnTheLeft = aRect.left < bRect.left + bRect.width;
            return link({
              source: [
                aRect.left +
                  (isSourceOnTheLeft ? aBox.offsetWidth : 0) -
                  svgOffset.current.offsetLeft,
                Math.min(
                  Math.max(
                    aRect.top - svgOffset.current.offsetTop + aRect.height / 2,
                    aParentRect.top - svgOffset.current.offsetTop
                  ),
                  aBox.clientHeight +
                    aParentRect.top -
                    svgOffset.current.offsetTop
                ),
              ],
              target: [
                bRect.left +
                  (isSourceOnTheLeft ? 0 : bBox.offsetWidth) -
                  svgOffset.current.offsetLeft,
                Math.min(
                  Math.max(
                    bRect.top - svgOffset.current.offsetTop + bRect.height / 2,
                    bParentRect.top - svgOffset.current.offsetTop
                  ),
                  bBox.clientHeight +
                    bParentRect.top -
                    svgOffset.current.offsetTop
                ),
              ],
            });
          }
          return null;
        })
        .filter(a => a) as Array<string>;
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
    <svg
      viewBox={`0 0 ${width} ${height}`}
      onDragOver={handleDragOver}
      ref={svgRef}
      style={{ width: '100%', height: '100%' }}
    >
      <g transform={'translate(0,0)'}>
        {lines.map((d, idx) => (
          <path
            key={idx}
            d={d}
            stroke={colors(idx)}
            strokeWidth={3}
            fill={'none'}
          />
        ))}

        <foreignObject
          width={boxWidth}
          height={boxHeight}
          x={boxAstartX}
          y={startY}
        >
          <Box
            header={<h1>Source</h1>}
            footer={<p>{as.length} fields</p>}
            ref={aBoxRef}
            onChanges={calcLines}
          >
            {as.map(id => (
              <div
                onClick={() => showMappingDetails(id)}
                style={{ padding: '0.3rem', borderBottom: '1px solid #eee' }}
                key={id}
                ref={el => el && addRef(el, id)}
              >
                {id}
              </div>
            ))}
          </Box>
        </foreignObject>

        <foreignObject
          width={boxWidth}
          height={boxHeight}
          x={boxBstartX}
          y={startY}
        >
          <Box
            header={<h1>Target</h1>}
            footer={<p>{bs.length} fields</p>}
            ref={bBoxRef}
            onChanges={calcLines}
          >
            {bs.map(id => (
              <div
                onClick={() => showMappingDetails(id)}
                style={{ padding: '0.3rem', borderBottom: '1px solid #eee' }}
                key={id}
                ref={el => el && addRef(el, id)}
              >
                {id}
              </div>
            ))}
          </Box>
        </foreignObject>
      </g>
    </svg>
  );
};
