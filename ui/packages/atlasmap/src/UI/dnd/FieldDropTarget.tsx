import React, {
  ReactElement,
  forwardRef,
  ElementType,
  HTMLAttributes,
  useRef,
  useEffect,
  // useEffect,
} from "react";
import { useDrop } from "react-dnd";
import { IDragAndDropField } from "./models";
import { useFieldsDnd } from "./FieldsDndProvider";

export interface IFieldDropTargetChildren {
  isOver: boolean;
  isDroppable: boolean;
  isTarget: boolean;
  field: IDragAndDropField;
}

export interface IFieldDropTargetProps
  extends Omit<HTMLAttributes<HTMLElement>, "onDrop"> {
  accept: string[];
  target: IDragAndDropField;
  canDrop: (draggedField: IDragAndDropField) => boolean;
  as?: ElementType;
  children: (props: IFieldDropTargetChildren) => ReactElement;
}

export const FieldDropTarget = forwardRef<HTMLElement, IFieldDropTargetProps>(
  ({ accept, target, canDrop, as: As = "div", children, ...props }, ref) => {
    const { setHoveredTarget } = useFieldsDnd();
    const [{ isOver, isDroppable, isTarget, field }, dropRef] = useDrop<
      IDragAndDropField,
      IDragAndDropField,
      IFieldDropTargetChildren
    >({
      accept,
      collect: (monitor) => ({
        isOver: monitor.isOver(),
        isDroppable: monitor.canDrop(),
        isTarget: monitor.isOver() && monitor.canDrop(),
        field: monitor.getItem(),
      }),
      canDrop: (_, monitor) => canDrop(monitor.getItem()),
      drop: () => target,
    });

    const handleRef = (el: HTMLElement) => {
      if (ref) {
        // @ts-ignore
        // by default forwardedRef.current is readonly. Let's ignore it
        ref.current = el;
      }
      dropRef(el);
    };

    const wasOver = useRef(false);
    const wasTarget = useRef(false);
    useEffect(() => {
      if (isOver !== wasOver.current || isTarget !== wasTarget.current) {
        if (isTarget) {
          setHoveredTarget(target);
        } else if (!isOver) {
          setHoveredTarget(null);
        }
        wasOver.current = isOver;
        wasTarget.current = isTarget;
      }
    }, [isOver, isTarget, setHoveredTarget, target]);

    return (
      <As ref={handleRef} {...props}>
        {children({ isDroppable, isOver, isTarget, field })}
      </As>
    );
  },
);
