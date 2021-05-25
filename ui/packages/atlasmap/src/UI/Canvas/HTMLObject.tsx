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
import React, { SVGProps, forwardRef } from 'react';
import { css } from '@patternfly/react-styles';
import styles from './HTMLObject.module.css';

export interface IHTMLObjectProps extends SVGProps<SVGForeignObjectElement> {
  width: number;
  height: number;
  x: number;
  y: number;
}

export const HTMLObject = forwardRef<SVGForeignObjectElement, IHTMLObjectProps>(
  function HTMLObject(
    { children, width, height, x, y, className, ...props },
    ref,
  ) {
    return (
      <foreignObject
        width={width}
        height={height}
        x={x}
        y={y}
        className={css(styles.foreignObject, className)}
        {...props}
        ref={ref}
      >
        {children}
      </foreignObject>
    );
  },
);
