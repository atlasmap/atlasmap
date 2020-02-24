import React, { FunctionComponent, useCallback } from 'react';
import { useCanvas } from './CanvasContext';
import { useAtlasmapUI } from '../AtlasmapUI/AtlasmapUIProvider';

export const CanvasTransforms: FunctionComponent = ({ children }) => {
  const { panX, panY, zoom /* width, height, xDomain, yDomain*/ } = useCanvas();
  // const originX = width / 2;
  // const originY = height / 2;
  // const x = xDomain(zoom * (panX + originX) - originX);
  // const y = yDomain(zoom * (panY + originY) - originY);
  const {
    deselectMapping,
    closeMappingDetails,
    selectedMapping,
  } = useAtlasmapUI();

  const handleSelect = useCallback(() => {
    if (selectedMapping) {
      closeMappingDetails();
      deselectMapping();
    }
  }, [selectedMapping, closeMappingDetails, deselectMapping]);

  return (
    <g
      transform={`
        translate(${panX} ${panY})
        scale(${zoom})
      `}
      onClick={handleSelect}
    >
      {children}
    </g>
  );
};
