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
import { Canvas, CanvasProvider } from '.';
import React from 'react';

const obj = {
  title: 'Canvas',
  includeStories: [], // or don't load this file at all
};
export default obj;

export const example = () => (
  <CanvasProvider initialHeight={300} allowPanning={true}>
    <Canvas>
      <circle cx={90} cy={50} r={20} fill={'green'} />
      <circle cx={150} cy={150} r={50} fill={'purple'} />
      <circle cx={20} cy={70} r={20} fill={'red'} />
    </Canvas>
  </CanvasProvider>
);

export const panZoomDisabled = () => (
  <CanvasProvider initialHeight={300} allowPanning={false}>
    <Canvas>
      <circle cx={90} cy={50} r={20} fill={'green'} />
      <circle cx={150} cy={150} r={50} fill={'purple'} />
      <circle cx={20} cy={70} r={20} fill={'red'} />
    </Canvas>
  </CanvasProvider>
);

export const panZoomEnabled = () => (
  <CanvasProvider initialHeight={300} allowPanning={true}>
    <Canvas>
      <circle cx={90} cy={50} r={20} fill={'green'} />
      <circle cx={150} cy={150} r={50} fill={'purple'} />
      <circle cx={20} cy={70} r={20} fill={'red'} />
    </Canvas>
  </CanvasProvider>
);

export const initialZoomAndPan = () => (
  <CanvasProvider
    initialHeight={300}
    initialZoom={2}
    initialPanX={250}
    initialPanY={-150}
    allowPanning={true}
  >
    <Canvas>
      <circle cx={90} cy={50} r={20} fill={'green'} />
      <circle cx={150} cy={150} r={50} fill={'purple'} />
      <circle cx={20} cy={70} r={20} fill={'red'} />
    </Canvas>
  </CanvasProvider>
);

export const automaticallySized = () => (
  <div style={{ width: 500, height: 500, background: 'red' }}>
    <CanvasProvider allowPanning={false}>
      <Canvas>
        <circle cx={480} cy={480} r={20} fill={'white'} />
      </Canvas>
    </CanvasProvider>
  </div>
);
