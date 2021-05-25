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
  Children,
  PropsWithChildren,
  cloneElement,
  forwardRef,
  isValidElement,
  useRef,
} from 'react';
import { NodeRefPropsWithOptionalId, useNodeRef } from './NodeRefProvider';

export const NodeRef = forwardRef<
  HTMLElement | SVGElement,
  PropsWithChildren<Omit<NodeRefPropsWithOptionalId, 'ref'>>
>(function NodeRef({ children, ...props }, ref) {
  const node = Children.only(children);
  const nodeRef = useRef<HTMLElement | SVGElement | null>(null);

  useNodeRef({ ...props, ref: nodeRef });

  const handleRef = (el: HTMLElement) => {
    if (ref) {
      // @ts-ignore
      // by default forwardedRef.current is readonly. Let's ignore it
      ref.current = el;
    }
    nodeRef.current = el;
  };

  return isValidElement(node)
    ? cloneElement(node, {
        ref: handleRef,
      })
    : null;
});
