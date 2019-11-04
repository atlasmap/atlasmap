import React, { useCallback, useRef, useState, UIEvent, WheelEvent, forwardRef, PropsWithChildren } from 'react';
import { css, StyleSheet } from '@patternfly/react-styles';

const BoxStyles = StyleSheet.create({
  outer: {
    border: '1px solid black',
    borderRadius: '0.3rem',
    width: '100%',
    height: '100%',
    overflow: 'hidden',
    display: 'flex',
    flexFlow: 'column',
    background: 'white',
  },
  header: {
    flex: '0 1 0',
    padding: '0.5rem'
  },
  body: {
    flex: '1',
    overflowY: 'scroll',
  },
  footer: {
    flex: '0 1 0',
    padding: '0.3rem',
    textAlign: 'right',
    fontSize: '0.9rem'
  },
});

export interface IBoxProps {
  header?: React.ReactElement;
  footer?: React.ReactElement;
  initialWidth: number;
  initialHeight: number;
  initialX: number;
  initialY: number;
  onChanges?: () => void;
}

/**
 * `Box` sample doc
 */
export const Box = forwardRef<HTMLDivElement, PropsWithChildren<IBoxProps>>(({
  header,
  footer,
  children,
  initialWidth,
  initialHeight,
  initialX,
  initialY,
  onChanges,
}, ref) => {
  const dragStartCoords = useRef<any>();
  const [{ x, y }, setCoords] = useState({ x: initialX, y: initialY });

  const startDragging = useCallback(
    (e: React.DragEvent<HTMLDivElement>) => {
      const fo = e.currentTarget.parentNode as SVGForeignObjectElement;
      dragStartCoords.current = {
        fo,
        x: parseInt(fo.getAttribute('x') || '0', 10),
        y: parseInt(fo.getAttribute('y') || '0', 10),
        eventStartCoords: {
          clientX: e.clientX,
          clientY: e.clientY,
        },
      };
    },
    [dragStartCoords]
  );

  const dragBox = useCallback(
    (e: React.DragEvent<HTMLDivElement>) => {
      let { x, y, eventStartCoords } = dragStartCoords.current;
      x = x + (e.clientX - eventStartCoords.clientX);
      y = y + (e.clientY - eventStartCoords.clientY);
      window.requestAnimationFrame(() => {
        setCoords({ x, y });
      });
      onChanges && onChanges();
    },
    [dragStartCoords, setCoords, onChanges]
  );

  const onScroll = useCallback((e: UIEvent<HTMLDivElement>) => {
    e.stopPropagation();
    onChanges && onChanges();
  }, [onChanges]);

  const handleWheel = (e: WheelEvent) => {
    e.stopPropagation()
  };

  return (
    <foreignObject width={initialWidth} height={initialHeight} x={x} y={y}>
      <div
        className={css(BoxStyles.outer)}
        draggable={true}
        onDragStart={startDragging}
        onDrag={dragBox}
        ref={ref}
      >
        <div className={css(BoxStyles.header)}>{header}</div>
        <div className={css(BoxStyles.body)} onScroll={onScroll} onWheel={handleWheel}>
          {children}
        </div>
        <div className={css(BoxStyles.footer)}>{footer}</div>
      </div>
    </foreignObject>
  );
});
