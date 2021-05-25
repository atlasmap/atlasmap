/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import React, {
  CSSProperties,
  FunctionComponent,
  SVGAttributes,
  useEffect,
  useMemo,
} from 'react';
import { CanvasTransforms } from './CanvasTransforms';
import { css } from '@patternfly/react-styles';
import styles from './Canvas.module.css';
import { useCanvas } from './CanvasContext';
import { useDimensions } from '../useDimensions';

export interface ICanvasProps extends SVGAttributes<SVGSVGElement> {
  isFilled?: boolean;
}

export const Canvas: FunctionComponent<ICanvasProps> = ({
  children,
  className,
  style,
  isFilled = true,
  ...props
}) => {
  const {
    initialWidth,
    initialHeight,
    setDimension,
    addRedrawListener,
    removeRedrawListener,
  } = useCanvas();
  const [dimensionsRef, dimensions, measure] = useDimensions();
  useEffect(() => {
    setDimension({
      width: dimensions.width,
      height: dimensions.height,
      offsetLeft: dimensions.left,
      offsetTop: dimensions.top,
    });
  }, [dimensions, setDimension]);

  useEffect(() => {
    addRedrawListener(measure);
    return () => removeRedrawListener(measure);
  }, [addRedrawListener, measure, removeRedrawListener]);

  const { allowPanning, isPanning, zoom, bindCanvas } = useCanvas();
  const svgStyle = useMemo(() => {
    return {
      cursor: allowPanning ? (isPanning ? 'grabbing' : 'grab') : undefined,
      userSelect: allowPanning && isPanning ? 'none' : 'auto',
      backgroundSize: `${30 * zoom}px ${30 * zoom}px`,
      width: initialWidth || '100%',
      height: initialHeight || '100%',
      ...style,
    } as CSSProperties;
  }, [allowPanning, initialHeight, initialWidth, isPanning, style, zoom]);
  return (
    <div ref={dimensionsRef} className={css(isFilled && styles.canvasWrapper)}>
      <svg
        {...props}
        className={css(styles.svg, allowPanning && styles.panning, className)}
        style={svgStyle}
        {...bindCanvas()}
      >
        <CanvasTransforms>{children}</CanvasTransforms>
      </svg>
    </div>
  );
};
