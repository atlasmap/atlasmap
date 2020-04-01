import React, { FunctionComponent } from 'react';
import { useCanvas } from './CanvasContext';

export const CanvasTransforms: FunctionComponent = ({ children }) => {
  const { panX, panY, zoom /* width, height, xDomain, yDomain*/ } = useCanvas();
  // const originX = width / 2;
  // const originY = height / 2;
  // const x = xDomain(zoom * (panX + originX) - originX);
  // const y = yDomain(zoom * (panY + originY) - originY);

  return (
    <g
      transform={`
        translate(${panX} ${panY})
        scale(${zoom})
      `}
    >
      {children}
    </g>
  );
};
