import { createTopologyControlButtons, TopologyControlBar } from '@patternfly/react-topology';
import React, { FunctionComponent, useMemo } from 'react';
import {
  SearchPlusIcon,
  SearchMinusIcon,
  ExpandArrowsAltIcon,
  ExpandIcon,
  LineIcon,
  PluggedIcon,
  UnpluggedIcon,
  MapIcon,
  RemoveFormatIcon
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

    customButtons: [
      {
        id: 'showtypes',
        icon: <RemoveFormatIcon />,
        tooltip: 'Show types',
        ariaLabel: ' ',
        callback: () => void(0),
        disabled: false,
        hidden: false
      }, {
        id: 'showlines',
        icon: <LineIcon />,
        tooltip: 'Show types',
        ariaLabel: ' ',
        callback: () => void(0),
        disabled: false,
        hidden: false
      }, {
        id: 'showmapped',
        icon: <PluggedIcon />,
        tooltip: 'Show mapped fields',
        ariaLabel: ' ',
        callback: () => void(0),
        disabled: false,
        hidden: false
      }, {
        id: 'showunmapped',
        icon: <UnpluggedIcon />,
        tooltip: 'Show unmapped fields',
        ariaLabel: ' ',
        callback: () => void(0),
        disabled: false,
        hidden: false
      }, {
        id: 'showpreview',
        icon: <MapIcon />,
        tooltip: 'Show mapping preview',
        ariaLabel: ' ',
        callback: () => void(0),
        disabled: false,
        hidden: false
      },
    ]
  }), []);

  return (
    <TopologyControlBar controlButtons={controlButtons} />
  )
};
