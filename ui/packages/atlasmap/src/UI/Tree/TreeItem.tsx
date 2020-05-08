import React, {
  forwardRef,
  HTMLAttributes,
  PropsWithChildren,
  useRef,
  ReactNode,
} from "react";

import { useTreeFocus } from "./TreeFocusProvider";

export interface ITreeItemProps extends HTMLAttributes<HTMLDivElement> {
  level: number;
  setSize: number;
  position: number;
  children: (props: { focused: boolean }) => ReactNode;
}

export const TreeItem = forwardRef<
  HTMLDivElement,
  PropsWithChildren<ITreeItemProps>
>(({ level, setSize, position, children, ...props }, ref) => {
  const divRef = useRef<HTMLDivElement | null>(null);
  const { focused, handlers: focusHandlers } = useTreeFocus({
    ref: divRef,
  });

  const handleRef = (el: HTMLDivElement | null) => {
    divRef.current = el;
    if (ref) {
      if (typeof ref === "function") {
        ref(el);
      } else {
        // @ts-ignore
        ref.current = el;
      }
    }
  };
  return (
    <div
      ref={handleRef}
      role={"treeitem"}
      aria-level={level}
      aria-setsize={setSize}
      aria-posinset={position}
      {...focusHandlers}
      {...props}
    >
      {children({ focused: focused || false })}
    </div>
  );
});
