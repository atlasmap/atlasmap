import React, {
  forwardRef,
  useCallback,
  useEffect,
  useState,
  HTMLAttributes,
} from "react";
import { useNodeRect, NodeRect } from "./NodeRefProvider";
import { useCanvas } from "./CanvasContext";

export default forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ id, children, ...props }, ref) => {
    const { addRedrawListener, removeRedrawListener } = useCanvas();
    const getRect = useNodeRect();
    const [rect, setRect] = useState<NodeRect | null>(null);

    const getAndSetRect = useCallback(() => {
      if (id) {
        const nodeRect = getRect(id);
        setRect(nodeRect ? getRect(id) : null);
      }
    }, [getRect, id]);

    useEffect(() => {
      addRedrawListener(getAndSetRect);
      return () => {
        removeRedrawListener(getAndSetRect);
      };
    }, [addRedrawListener, removeRedrawListener, getAndSetRect]);

    return (
      <div {...props} ref={ref}>
        <pre>{JSON.stringify(rect, null, 2)}</pre>
        {children}
      </div>
    );
  },
);
