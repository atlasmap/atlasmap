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
import { Canvas, CanvasProvider } from '../UI';

import { CanvasControlBar } from './CanvasControlBar';
import React from 'react';
import { RedhatIcon } from '@patternfly/react-icons';
import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';

export default {
  title: 'AtlasMap|Layout/CanvasControlBar',
};

export const example = () => {
  const freeView = boolean('Enable canvas panning', true);
  return (
    <div>
      <CanvasProvider initialHeight={300} allowPanning={freeView}>
        <Canvas>
          <circle cx={90} cy={50} r={20} fill={'green'} />
          <circle cx={150} cy={150} r={50} fill={'purple'} />
          <circle cx={20} cy={70} r={20} fill={'red'} />
        </Canvas>
        <CanvasControlBar
          disabled={!freeView}
          extraButtons={[
            {
              id: 'id1',
              icon: <RedhatIcon />,
              callback: action('Custom button clicked'),
            },
          ]}
        />
      </CanvasProvider>
    </div>
  );
};
