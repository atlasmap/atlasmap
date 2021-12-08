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
import {
  Canvas,
  CanvasProvider,
  HTMLObject,
  NodeRef,
  NodeRefProvider,
  NodesArc,
} from '.';
import React, {
  FunctionComponent,
  createElement,
  useCallback,
  useState,
} from 'react';
import { animated, useSpring } from 'react-spring';

import PrintNodeRef from './PrintNodeRef';

const obj = {
  title: 'UI|Canvas/NodesArc',
  includeStories: [], // or don't load this file at all
};
export default obj;

export const example: FunctionComponent = () =>
  createElement(() => {
    const [reverse, setReverse] = useState<boolean>(false);
    const onRest = useCallback(() => setReverse(!reverse), [reverse]);
    // @ts-ignore
    const props = useSpring({
      from: { x: 30, y: 0 },
      to: [
        { x: 250, y: 0 },
        { x: 250, y: 260 },
        { x: 30, y: 260 },
        { x: 30, y: 0 },
      ],
      config: { duration: 2500 },
      onRest,
    });
    return (
      <CanvasProvider initialHeight={300} allowPanning={false}>
        <Canvas>
          <NodeRefProvider>
            <NodeRef id="a">
              <animated.rect
                style={props}
                width={40}
                height={40}
                fill={'purple'}
              />
            </NodeRef>
            <NodeRef id="b">
              <rect x={150} y={90} width={60} height={60} fill={'red'} />
            </NodeRef>
            <NodeRef id="c">
              <rect x={390} y={160} width={80} height={80} fill={'yellow'} />
            </NodeRef>
            <NodesArc start={'a'} end={'b'} />
            <NodesArc start={'b'} end={'c'} />
          </NodeRefProvider>
        </Canvas>
      </CanvasProvider>
    );
  });

export const customWidthAndColor = () => (
  <CanvasProvider initialHeight={300} allowPanning={false}>
    <Canvas>
      <NodeRefProvider>
        <NodeRef id="a">
          <rect x={90} y={50} width={40} height={40} fill={'green'} />
        </NodeRef>
        <NodeRef id="b">
          <rect x={20} y={240} width={40} height={40} fill={'red'} />
        </NodeRef>
        <NodeRef id="c">
          <rect x={350} y={150} width={100} height={100} fill={'purple'} />
        </NodeRef>
        <NodesArc start={'a'} end={'c'} color={'green'} width={1} />
        <NodesArc start={'b'} end={'c'} color={'red'} width={4} />
      </NodeRefProvider>
    </Canvas>
  </CanvasProvider>
);

export const svgElementCoordinates = () => (
  <CanvasProvider initialHeight={200}>
    <Canvas>
      <NodeRefProvider>
        <NodeRef id="a">
          <rect x={50} y={100} width={40} height={40} fill={'purple'} />
        </NodeRef>
        <HTMLObject id={'id'} width={200} height={200} x={150} y={0}>
          <PrintNodeRef id="a" />
        </HTMLObject>
      </NodeRefProvider>
    </Canvas>
  </CanvasProvider>
);

export const htmlElementCoordinates = () => (
  <CanvasProvider initialHeight={300}>
    <Canvas>
      <NodeRefProvider>
        <HTMLObject width={300} height={200} x={50} y={50}>
          <NodeRef id="a">
            <PrintNodeRef
              id="a"
              style={{ width: 300, height: 200, border: '1px solid red' }}
            />
          </NodeRef>
        </HTMLObject>
      </NodeRefProvider>
    </Canvas>
  </CanvasProvider>
);

export const nestedHtmlElementCoordinates = () => (
  <CanvasProvider initialHeight={350}>
    <Canvas>
      <NodeRefProvider>
        <HTMLObject width={300} height={250} x={50} y={50}>
          <NodeRef id="a">
            <PrintNodeRef
              id="a"
              style={{
                width: 300,
                height: 300,
                border: '1px solid black',
                overflow: 'scroll',
              }}
            >
              <NodeRef id="b">
                <PrintNodeRef
                  id="b"
                  style={{
                    width: 50,
                    height: 20,
                    border: '1px solid red',
                    marginTop: 50,
                  }}
                />
              </NodeRef>
            </PrintNodeRef>
          </NodeRef>
        </HTMLObject>
      </NodeRefProvider>
    </Canvas>
  </CanvasProvider>
);

export const linksBetweenDifferentHtmlElements = () => (
  <CanvasProvider initialHeight={350}>
    <Canvas>
      <NodeRefProvider>
        <HTMLObject width={80} height={250} x={50} y={50}>
          <NodeRef id="a">
            <ul>
              <NodeRef id="a1">
                <li>item a1</li>
              </NodeRef>
              <NodeRef id="a2">
                <li>item a2</li>
              </NodeRef>
              <NodeRef id="a3">
                <li>item a3</li>
              </NodeRef>
            </ul>
          </NodeRef>
        </HTMLObject>
        <HTMLObject width={80} height={250} x={350} y={50}>
          <NodeRef id="b">
            <ul>
              <NodeRef id="b1">
                <li>item b1</li>
              </NodeRef>
              <NodeRef id="b2">
                <li>item b2</li>
              </NodeRef>
              <NodeRef id="b3">
                <li>item b3</li>
              </NodeRef>
            </ul>
          </NodeRef>
        </HTMLObject>
        <NodesArc start={'a1'} end={'b3'} />
        <NodesArc start={'a3'} end={'b2'} />
        <NodesArc start={'a2'} end={'b1'} />
      </NodeRefProvider>
    </Canvas>
  </CanvasProvider>
);
