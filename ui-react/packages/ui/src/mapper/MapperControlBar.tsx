import { createTopologyControlButtons, TopologyControlBar } from '@patternfly/react-topology';
import React, { FunctionComponent, useMemo } from 'react';
import {
  SearchPlusIcon,
  SearchMinusIcon,
  ExpandArrowsAltIcon,
  ExpandIcon,
} from '@patternfly/react-icons';

export interface IMapperControlBarProps {
  onZoomIn: () => void,
  onZoomOut: () => void,
  onZoomReset: () => void,
}

export const MapperControlBar: FunctionComponent<IMapperControlBarProps> = ({
  onZoomIn,
  onZoomOut,
  onZoomReset
}) => {
  const controlButtons = useMemo(() => createTopologyControlButtons({
    zoomIn: true,
    zoomInIcon: <SearchPlusIcon />,
    zoomInTip: 'Zoom In',
    zoomInAriaLabel: ' ',
    zoomInCallback: onZoomIn,
    zoomInDisabled: false,
    zoomInHidden: false,

    zoomOut: true,
    zoomOutIcon: <SearchMinusIcon />,
    zoomOutTip: 'Zoom Out',
    zoomOutAriaLabel: ' ',
    zoomOutCallback: onZoomOut,
    zoomOutDisabled: false,
    zoomOutHidden: false,

    fitToScreen: false,
    fitToScreenIcon: <ExpandArrowsAltIcon />,
    fitToScreenTip: 'Fit to Screen',
    fitToScreenAriaLabel: ' ',
    fitToScreenCallback: () => void(0),
    fitToScreenDisabled: false,
    fitToScreenHidden: false,

    resetView: true,
    resetViewIcon: <ExpandIcon />,
    resetViewTip: 'Reset View',
    resetViewAriaLabel: ' ',
    resetViewCallback: onZoomReset,
    resetViewDisabled: false,
    resetViewHidden: false,

    legend: false,
    legendIcon: 'Legend',
    legendTip: '',
    legendAriaLabel: 'Legend',
    legendCallback: () => void(0),
    legendDisabled: false,
    legendHidden: false,

    customButtons: []
  }), []);

  return (
    <TopologyControlBar controlButtons={controlButtons} />
  )
};
