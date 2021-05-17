import React, {
  ElementType,
  HTMLAttributes,
  ReactElement,
  forwardRef,
  useEffect,
} from "react";

import { IDragAndDropField } from "./models";
import { getEmptyImage } from "react-dnd-html5-backend";
import { useDrag } from "react-dnd";

export interface IDraggableChildrenProps {
  isDragging: boolean;
}

export interface IDraggableProps
  extends Omit<HTMLAttributes<HTMLElement>, "onDrop"> {
  field: IDragAndDropField;
  onDrop: (source: IDragAndDropField, target: IDragAndDropField | null) => void;
  as?: ElementType;
  children: (props: IDraggableChildrenProps) => ReactElement;
}

export const DraggableField = forwardRef<HTMLElement, IDraggableProps>(
  function DraggableField(
    { field, onDrop, as: As = "div", children, ...props },
    ref,
  ) {
    const [{ isDragging }, dragRef, preview] = useDrag<
      IDragAndDropField,
      IDragAndDropField,
      Omit<IDraggableChildrenProps, "dragRef">
    >({
      type: field.type,
      item: field,
      collect: (monitor) => ({
        isDragging: monitor.isDragging(),
      }),
      end: (_, monitor) => {
        if (monitor.didDrop()) {
          onDrop(field, monitor.getDropResult());
        }
      },
    });

    useEffect(() => {
      preview(getEmptyImage(), { captureDraggingState: true });
    }, [preview]);

    const handleRef = (el: HTMLElement) => {
      if (ref) {
        // @ts-ignore
        // by default forwardedRef.current is readonly. Let's ignore it
        ref.current = el;
      }
      dragRef(el);
    };

    return (
      <As ref={handleRef} {...props}>
        {children({
          isDragging,
        })}
      </As>
    );
  },
);
