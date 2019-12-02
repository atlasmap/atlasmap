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
  PficonDragdropIcon,
  LinkIcon,
  InfoIcon,
  ConnectedIcon,
  DisconnectedIcon, EyeIcon
} from '@patternfly/react-icons';
import { useCanvasViewContext } from '../CanvasViewProvider';

export const CanvasViewControlBar: FunctionComponent = () => {
  const { updateZoom,
    resetZoom,
    resetPan,
    freeView,
    toggleFreeView,
    toggleMaterializedMappings,
  } = useCanvasViewContext();

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
        zoomIn: freeView,
        zoomInIcon: <SearchPlusIcon />,
        zoomInTip: 'Zoom In',
        zoomInAriaLabel: ' ',
        zoomInCallback: handleZoomIn,
        zoomInDisabled: false,
        zoomInHidden: false,

        zoomOut: freeView,
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

        resetView: freeView,
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

        customButtons: [
          {
            id: 'Free view mode',
            icon: <PficonDragdropIcon />,
            tooltip: 'Free view mode',
            ariaLabel: ' ',
            callback: toggleFreeView
          },
          {
            id: 'Toggle mappings column',
            icon: <LinkIcon />,
            tooltip: 'Toggle mappings column',
            ariaLabel: ' ',
            callback: toggleMaterializedMappings
          },
          {
            id: 'Show types',
            icon: <InfoIcon />,
            tooltip: 'Show types',
            ariaLabel: ' ',
          },
          {
            id: 'Show mapped fields',
            icon: <ConnectedIcon />,
            tooltip: 'Show mapped fields',
            ariaLabel: ' ',
          },
          {
            id: 'Show unmapped fields',
            icon: <DisconnectedIcon />,
            tooltip: 'Show unmapped fields',
            ariaLabel: ' ',
          },
          {
            id: 'Show mapping preview',
            icon: <EyeIcon />,
            tooltip: 'Show mapping preview',
            ariaLabel: ' ',
          },
        ],
      }),
    [handleZoomIn, handleZoomOut, handleViewReset, toggleFreeView, toggleMaterializedMappings]
  );

  return (
    <TopologyControlBar controlButtons={controlButtons} />
  );
};
