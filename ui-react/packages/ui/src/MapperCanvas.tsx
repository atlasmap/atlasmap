import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Box } from '@src/Box';
import { linkHorizontal } from 'd3-shape';
import { scaleLinear } from 'd3-scale';

export interface IMappingCanvasProps {
  width: number;
  height: number;
  zoom: number;
}

const as = [
  'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f', 'G', 'g', 'H', 'h', 'I', 'i', 'J', 'j', 'K', 'k', 'L', 'l', 'M', 'm', 'N', 'n', 'O', 'o', 'P', 'p', 'Q', 'q', 'R', 'r', 'S', 's', 'T', 't', 'U', 'u', 'V', 'v', 'W', 'w', 'X', 'x', 'Y', 'y', 'Z', 'z',
];
const bs = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'];
const mappings = [['A', '10'], ['B', '1'], ['e', '5'], ['Z', '7'], ['X', '3']];

export const MapperCanvas: React.FunctionComponent<IMappingCanvasProps> = ({
  width,
  height,
  zoom
}) => {
  const svgRef = useRef<SVGSVGElement | null>(null);
  const svgOffset = useRef<{ offsetTop: number, offsetLeft: number }>({
    offsetTop: 0,
    offsetLeft: 0
  });
  const aBoxRef = useRef<HTMLDivElement | null>(null);
  const bBoxRef = useRef<HTMLDivElement | null>(null);

  const x = scaleLinear()
    .range([0, width])
    .domain([0, width * zoom]);
  const y = scaleLinear()
    .range([height, 0])
    .domain([height * zoom, 0]);

  const link = linkHorizontal()
    .context(null)
    .x(d => x.invert(d[0]))
    .y(d => y.invert(d[1]));

  const startY = y.invert(height / 2 - 150);
  const boxAstartX = x.invert(width / 4 - 100);
  const boxBstartX = x.invert(width / 2 + 100);

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const [lines, setLines] = useState<string[]>([]);

  const fieldsRef = useRef<{ [id: string]: HTMLDivElement }>({});
  const addRef = (ref: HTMLDivElement, id: string) => {
    fieldsRef.current[id] = ref;
  };

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
            if (
              (aRect.top - aBox.scrollTop) < (aBox.clientHeight + aParentRect.top) &&
              (aRect.bottom - aBox.scrollTop) > aParentRect.top &&
              (bRect.top - bBox.scrollTop) < (bBox.clientHeight + bParentRect.top) &&
              (bRect.bottom - aBox.scrollTop) > bParentRect.top
            ) {
              const isSourceOnTheLeft = (aRect.left) < (bRect.left + bRect.width);
              return link({
                source: [
                  aRect.left + (isSourceOnTheLeft ? aBox.offsetWidth : 0) - svgOffset.current.offsetLeft,
                  aRect.top - svgOffset.current.offsetTop + aRect.height / 2
                ],
                target: [
                  bRect.left + (isSourceOnTheLeft ? 0 : bBox.offsetWidth) - svgOffset.current.offsetLeft,
                  bRect.top - svgOffset.current.offsetTop + bRect.height / 2
                ]
              });
            } else {
              return null;
            }
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
  }, [svgRef, svgOffset])

  return (
    <svg viewBox={`0 0 ${width} ${height}`} onDragOver={handleDragOver} ref={svgRef}>
      <g transform={'translate(0,0)'}>
        {lines.map((d, idx) => (
          <path key={idx} d={d} stroke={'steelblue'} strokeWidth={3} fill={'none'} />
        ))}

        <Box
          header={<h1>Title A</h1>}
          initialWidth={x.invert(200)}
          initialHeight={y.invert(300)}
          initialX={boxAstartX}
          initialY={startY}
          onChanges={calcLines}
          ref={aBoxRef}
        >
          {as.map(id =>
            <div style={{ padding: '0.3rem', borderBottom: '1px solid #eee'}} key={id} ref={el => el && addRef(el, id)}>{id}</div>
          )}
        </Box>
        <Box
          header={<h1>Title B</h1>}
          footer={<p>footer</p>}
          initialWidth={x.invert(200)}
          initialHeight={y.invert(300)}
          initialX={boxBstartX}
          initialY={startY}
          onChanges={calcLines}
          ref={bBoxRef}
        >
          {bs.map(id =>
            <div style={{ padding: '0.3rem', borderBottom: '1px solid #eee'}} key={id} ref={el => el && addRef(el, id)}>{id}</div>
          )}
        </Box>
      </g>
    </svg>
  );
};
