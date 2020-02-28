import {
  createTopologyControlButtons,
  TopologyControlBar,
  TopologyControlButton,
} from '@patternfly/react-topology';
import React, { FunctionComponent, useCallback, useMemo } from 'react';
import {
  SearchPlusIcon,
  SearchMinusIcon,
  ExpandArrowsAltIcon,
  ExpandIcon,
} from '@patternfly/react-icons';
import { useCanvasViewOptionsContext } from '../CanvasViewOptionsProvider';
import { useCanvasViewContext } from '../CanvasViewCanvasProvider';

export function getToolbarIconStyle(active: boolean) {
  return { color: active ? 'var(--pf-global--primary-color--100)' : undefined };
}

export interface ICanvasViewControlBarProps {
  extraButtons?: TopologyControlButton[];
}

export const CanvasViewControlBar: FunctionComponent<ICanvasViewControlBarProps> = ({
  extraButtons = [],
}) => {
  const { updateZoom, resetZoom, resetPan } = useCanvasViewContext();

  const { freeView } = useCanvasViewOptionsContext();

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

        customButtons: [...extraButtons],
      }),
    [freeView, handleZoomIn, handleZoomOut, handleViewReset, extraButtons]
  );

  return <TopologyControlBar controlButtons={controlButtons} />;
};
