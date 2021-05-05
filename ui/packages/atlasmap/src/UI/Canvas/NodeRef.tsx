import {
  Children,
  PropsWithChildren,
  cloneElement,
  forwardRef,
  isValidElement,
  useRef,
} from "react";
import { NodeRefPropsWithOptionalId, useNodeRef } from "./NodeRefProvider";

export const NodeRef = forwardRef<
  HTMLElement | SVGElement,
  PropsWithChildren<Omit<NodeRefPropsWithOptionalId, "ref">>
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
