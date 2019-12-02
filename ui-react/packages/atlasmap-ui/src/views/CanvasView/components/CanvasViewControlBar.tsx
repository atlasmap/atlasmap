import {
  createTopologyControlButtons,
  TopologyControlBar,
} from '@patternfly/react-topology';
import React, { FunctionComponent, useCallback, useMemo } from 'react';
import {
  SearchPlusIcon,
  SearchMinusIcon,
  ExpandArrowsAltIcon,
  ExpandIcon,
} from '@patternfly/react-icons';
import { useCanvasViewContext } from '../CanvasViewProvider';

export const CanvasViewControlBar: FunctionComponent = () => {

  const { updateZoom, resetZoom, resetPan } = useCanvasViewContext();

  const handleZoomIn = useCallback(() => {
    updateZoom(0.2);
  }, [updateZoom]);
  const handleZoomOut = useCallback(() => {
    updateZoom(-0.2);
  }, [updateZoom]);
  const handleViewReset = useCallback(() => {
    resetZoom();
    resetPan();
  }, [resetZoom, resetPan]);

  const controlButtons = useMemo(
    () =>
      createTopologyControlButtons({
        zoomIn: true,
        zoomInIcon: <SearchPlusIcon />,
        zoomInTip: 'Zoom In',
        zoomInAriaLabel: ' ',
        zoomInCallback: handleZoomIn,
        zoomInDisabled: false,
        zoomInHidden: false,

        zoomOut: true,
        zoomOutIcon: <SearchMinusIcon />,
        zoomOutTip: 'Zoom Out',
        zoomOutAriaLabel: ' ',
        zoomOutCallback: handleZoomOut,
        zoomOutDisabled: false,
        zoomOutHidden: false,

        fitToScreen: false,
        fitToScreenIcon: <ExpandArrowsAltIcon />,
        fitToScreenTip: 'Fit to Screen',
        fitToScreenAriaLabel: ' ',
        fitToScreenCallback: () => void 0,
        fitToScreenDisabled: false,
        fitToScreenHidden: false,

        resetView: true,
        resetViewIcon: <ExpandIcon />,
        resetViewTip: 'Reset View',
        resetViewAriaLabel: ' ',
        resetViewCallback: handleViewReset,
        resetViewDisabled: false,
        resetViewHidden: false,

        legend: false,
        legendIcon: 'Legend',
        legendTip: '',
        legendAriaLabel: 'Legend',
        legendCallback: () => void 0,
        legendDisabled: false,
        legendHidden: false,

        customButtons: [],
      }),
    [handleZoomIn, handleZoomOut, handleViewReset]
  );

  return <TopologyControlBar controlButtons={controlButtons} />;
};
