import { CanvasProvider } from '@src/canvas/CanvasContext';
import React, {
  FunctionComponent,
  useCallback, useEffect,
  useRef,
} from 'react';

export interface ICanvasProps {
  width: number;
  height: number;
  zoom: number;
}

export const Canvas: FunctionComponent<ICanvasProps> = (({ children, width, height, zoom }) => {
  const svgRef = useRef<SVGSVGElement | null>(null);
  const svgOffset = useRef<{ offsetTop: number; offsetLeft: number }>({
    offsetTop: 0,
    offsetLeft: 0,
  });

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
  }, []);

  useEffect(() => {
    const requestId = requestAnimationFrame(() => {
      if (svgRef.current) {
        const { top, left } = svgRef.current.getBoundingClientRect();
        svgOffset.current.offsetTop = top;
        svgOffset.current.offsetLeft = left;
      }
    });
    return () => {
      cancelAnimationFrame(requestId);
    };
  }, [svgRef, svgOffset]);

  return (
    <CanvasProvider
      width={width}
      height={height}
      zoom={zoom}
      offsetLeft={svgOffset.current.offsetLeft}
      offsetTop={svgOffset.current.offsetTop}
    >
      <svg
        onDragOver={handleDragOver}
        ref={svgRef}
        style={{ width: '100%', height: '100%' }}
      >
        {children}
      </svg>
    </CanvasProvider>
  );
});
