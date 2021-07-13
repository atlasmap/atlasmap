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
import React, { FunctionComponent, SVGAttributes, useMemo } from 'react';
import { linkHorizontal, linkVertical } from 'd3-shape';

import { Coords } from './models';
import { css } from '@patternfly/react-styles';
import styles from './Arc.module.css';
import { useToggle } from '../../impl/utils';

export interface IArcProps extends Omit<SVGAttributes<SVGPathElement>, 'end'> {
  start: Coords;
  end: Coords;
  startSideSize?: number;
  endSideSize?: number;
  type?: 'horizontal' | 'vertical';
  strokeWidth?: number;
  color?: string;
  hoveredColor?: string;
  sideStrokeWidth?: number;
}

export const Arc: FunctionComponent<IArcProps> = ({
  start,
  end,
  type = 'horizontal',
  color = 'grey',
  hoveredColor = color,
  strokeWidth = 2,
  startSideSize = strokeWidth,
  endSideSize = strokeWidth,
  sideStrokeWidth = 5,
  className,
  children,
  ...props
}) => {
  const {
    state: hovered,
    toggleOn: toggleHoverOn,
    toggleOff: toggleHoverOff,
  } = useToggle(false);
  const s = strokeWidth;
  const appliedColor = hovered ? hoveredColor : color;

  const link = useMemo(
    () =>
      (type === 'horizontal' ? linkHorizontal : linkVertical)<
        any,
        { start: Coords; end: Coords },
        Coords
      >()
        .context(null)
        .source((d) => d.start)
        .target((d) => d.end)
        .x((d) => d.x)
        .y((d) => d.y),
    [type],
  );

  const d = link({ start, end });

  startSideSize = startSideSize / 2;
  endSideSize = endSideSize / 2;
  return d ? (
    <g
      onMouseEnter={toggleHoverOn}
      onMouseLeave={toggleHoverOff}
      style={{ pointerEvents: 'all' }}
    >
      <path d={d} stroke={appliedColor} strokeWidth={s} fill={'none'} />
      <path
        d={d}
        stroke={'transparent'}
        strokeWidth={20}
        fill={'none'}
        className={css(props.onClick && styles.clickable)}
        {...props}
      />
      {children}
    </g>
  ) : null;
};
