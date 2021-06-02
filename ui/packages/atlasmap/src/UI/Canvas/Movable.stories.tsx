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
import { Movable } from './Movable';
import PrintMovableRects from './PrintMovableRects';
import React from 'react';

const obj = {
  title: 'Canvas',
  includeStories: [], // or don't load this file at all
};
export default obj;

export const example = () => (
  <CanvasProvider initialHeight={150}>
    <Canvas>
      <Movable<SVGRectElement> id={'id1'}>
        {({ x = 20, y = 70, bind, ref, className }) => (
          <rect
            x={x}
            y={y}
            width={200}
            height={40}
            fill={'red'}
            {...bind()}
            ref={ref}
            className={className}
          />
        )}
      </Movable>
    </Canvas>
  </CanvasProvider>
);

export const disabled = () => (
  <CanvasProvider initialHeight={150}>
    <Canvas>
      <Movable<SVGRectElement> id={'id1'} enabled={false}>
        {({ x = 20, y = 70, bind, ref, className }) => (
          <rect
            x={x}
            y={y}
            width={200}
            height={40}
            fill={'red'}
            {...bind()}
            ref={ref}
            className={className}
          />
        )}
      </Movable>
    </Canvas>
  </CanvasProvider>
);

export const htmlObject = () => (
  <CanvasProvider initialHeight={300}>
    <Canvas>
      <Movable<SVGForeignObjectElement> id={'id'}>
        {({ x = 20, y = 20, bind, ref, className }) => (
          <HTMLObject width={300} height={150} x={x} y={y} ref={ref}>
            <div
              className={className}
              {...bind()}
              style={{
                width: 300,
                height: 150,
                border: '1px solid red',
                borderRadius: 5,
              }}
            >
              <iframe
                src="https://www.youtube.com/embed/dQw4w9WgXcQ"
                frameBorder="0"
                allow="autoplay; encrypted-media"
                allowFullScreen
                title="video"
              />
              <button>Hint: click here to move me!</button>
            </div>
          </HTMLObject>
        )}
      </Movable>
    </Canvas>
  </CanvasProvider>
);

export const multipleMovable = () => (
  <CanvasProvider initialHeight={300}>
    <Canvas>
      <Movable<SVGRectElement> id={'id1'}>
        {({ x = 20, y = 100, bind, ref, className }) => (
          <rect
            x={x}
            y={y}
            width={200}
            height={40}
            fill={'red'}
            {...bind()}
            ref={ref}
            className={className}
          />
        )}
      </Movable>
      <Movable<SVGCircleElement> id={'id2'}>
        {({ x = 300, y = 60, bind, ref, className }) => (
          <circle
            cx={x + 40}
            cy={y + 40}
            r={40}
            fill={'purple'}
            {...bind()}
            ref={ref}
            className={className}
          />
        )}
      </Movable>
      <Movable<SVGEllipseElement> id={'id3'}>
        {({ x = 450, y = 70, bind, ref, className }) => (
          <ellipse
            cx={x + 90}
            cy={y + 50}
            rx={90}
            ry={50}
            fill={'green'}
            {...bind()}
            ref={ref}
            className={className}
          />
        )}
      </Movable>
      <Movable<SVGPolygonElement> id={'id4'}>
        {({ x = 650, y = 90, bind, ref, className }) => (
          <polygon
            points={`${x + 60},${y + 20} ${x + 100},${y + 40} ${x + 100},${
              y + 80
            } ${x + 60},${y + 100} ${x + 20},${y + 80} ${x + 20},${y + 40}`}
            fill={'yellow'}
            {...bind()}
            ref={ref}
            className={className}
          />
        )}
      </Movable>
      <PrintMovableRects />
    </Canvas>
  </CanvasProvider>
);
