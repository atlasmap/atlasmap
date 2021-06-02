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
import { Arc } from './Arc';
import { Canvas } from './Canvas';
import { CanvasProvider } from './CanvasContext';
import React from 'react';

const obj = {
  title: 'Canvas',
  includeStories: [], // or don't load this file at all
};
export default obj;

export const example = () => (
  <CanvasProvider initialHeight={200} allowPanning={false}>
    <Canvas>
      <circle cx={20} cy={30} r={20} fill={'purple'} />
      <circle cx={250} cy={150} r={50} fill={'red'} />
      <Arc start={{ x: 40, y: 30 }} end={{ x: 200, y: 150 }} />
    </Canvas>
  </CanvasProvider>
);

export const customWidthAndColor = () => (
  <CanvasProvider initialHeight={300} allowPanning={false}>
    <Canvas>
      <circle cx={90} cy={50} r={20} fill={'green'} />
      <circle cx={20} cy={240} r={20} fill={'red'} />
      <circle cx={350} cy={150} r={50} fill={'purple'} />
      <Arc
        start={{ x: 110, y: 50 }}
        end={{ x: 300, y: 150 }}
        color={'green'}
        strokeWidth={1}
      />
      <Arc
        start={{ x: 40, y: 240 }}
        end={{ x: 300, y: 150 }}
        color={'red'}
        strokeWidth={4}
      />
    </Canvas>
  </CanvasProvider>
);
