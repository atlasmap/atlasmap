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
const mappings = [
  ['A', '10'],
  ['B', '1'],
  ['d', '9'],
  ['e', '5'],
  ['f', '5'],
  ['h', '2'],
  ['j', '2'],
  ['m', '7'],
  ['Q', '8'],
  ['X', '0'],
  ['X', '3'],
  ['X', '6'],
  ['Z', '7'],
];

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
        .domain([0, width /* * zoom*/]),
    [width, zoom]
  );
  const y = useMemo(
    () =>
      scaleLinear()
        .range([height, 0])
        .domain([height /* * zoom*/, 0]),
    [width, zoom]
  );

  const colors = scaleSequential(interpolateMagma).domain([0, mappings.length]);

  const link = linkHorizontal()
    .context(null)
    .x(d => x.invert(d[0]))
    .y(d => y.invert(d[1]));

  const gutter = 50;
  const boxWidth = Math.max(200, width / 2 - gutter * 2);
  const boxHeight = Math.max(300, height - gutter * 3);
  const startY = gutter;
  const boxAstartX = gutter;
  const boxBstartX = Math.max(width / 2, boxWidth + gutter) + gutter;

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
  }, []);

  const [lines, setLines] = useState<{ d: string, stroke: string }[]>([]);

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
        .map(([a, b], idx) => {
          const aRef = fieldsRef.current[a];
          const bRef = fieldsRef.current[b];
          if (aRef && bRef) {
            const aRect = aRef.getBoundingClientRect();
            const bRect = bRef.getBoundingClientRect();
            const isSourceOnTheLeft = aRect.left < bRect.left + bRect.width;
            return {
              d: link({
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
                   }),
              stroke: colors(idx)
            };
          }
          return null;
        })
        .filter(a => a);
      setLines(newLines as any);
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
      onDragOver={handleDragOver}
      ref={svgRef}
      style={{ width: '100%', height: '100%' }}
    >
      {lines.map(({d, stroke}, idx) => (
        <path
          key={idx}
          d={d}
          stroke={stroke}
          strokeWidth={3}
          fill={'none'}
        />
      ))}

      <foreignObject
        width={x.invert(boxWidth)}
        height={x.invert(boxHeight)}
        x={x.invert(boxAstartX)}
        y={x.invert(startY)}
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
              style={{
                padding: '0.3rem',
                borderBottom: '1px solid #eee',
                fontSize: `${zoom}rem`,
              }}
              key={id}
              ref={el => el && addRef(el, id)}
            >
              {id} lorem dolor ipsum
            </div>
          ))}
        </Box>
      </foreignObject>

      <foreignObject
        width={x.invert(boxWidth)}
        height={x.invert(boxHeight)}
        x={x.invert(boxBstartX)}
        y={x.invert(startY)}
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
              style={{
                padding: '0.3rem',
                borderBottom: '1px solid #eee',
                fontSize: `${zoom}rem`,
              }}
              key={id}
              ref={el => el && addRef(el, id)}
            >
              {id} lorem dolor ipsum
            </div>
          ))}
        </Box>
      </foreignObject>
    </svg>
  );
};
