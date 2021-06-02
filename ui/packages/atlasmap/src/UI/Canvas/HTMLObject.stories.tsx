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
import { Canvas } from './Canvas';
import { CanvasProvider } from './CanvasContext';
import { HTMLObject } from './HTMLObject';
import React from 'react';

const obj = {
  title: 'Canvas',
  includeStories: [], // or don't load this file at all
};
export default obj;

export const example = () => (
  <CanvasProvider initialHeight={300} allowPanning={false}>
    <Canvas>
      <circle cx={280} cy={50} r={20} fill={'purple'} />
      <circle cx={50} cy={100} r={50} fill={'red'} />
      <HTMLObject width={200} height={200} x={50} y={10}>
        <div style={{ border: '1px solid #333', background: '#fff' }}>
          <p>Plain html content inside an SVG.</p>
          <p>
            This is rendered with respect to SVG rules, so this element will
            stay in front of the red circle.
          </p>
          <button>A button</button>
        </div>
      </HTMLObject>
    </Canvas>
  </CanvasProvider>
);
