import React, {
  createContext,
  FunctionComponent,
  MutableRefObject,
  useContext,
  useRef,
  useEffect,
  useCallback,
} from "react";
import { useBoundingCanvasRect } from "./useBoundingCanvasRect";

export interface NodeRect extends DOMRect {
  clipped?: boolean;
}

export interface NodeRefProps {
  ref: MutableRefObject<HTMLElement | SVGElement | null>;
  parentId?: string;
  boundaryId?: string;
  overrideWidth?: string;
  overrideHeight?: string;
}

export interface NodeRefPropsWithOptionalId extends NodeRefProps {
  id: string | undefined | Array<string | undefined>;
}

export interface NodeRefPropsWithId extends NodeRefProps {
  id: string;
}

export interface NodeRefPropsWithKey extends NodeRefProps {
  key: string;
}

export interface NodeRefMap {
  [id: string]: NodeRefPropsWithKey;
}

export interface INodeRefContext {
  nodes: MutableRefObject<NodeRefMap>;
  setRef: (props: NodeRefPropsWithId) => string;
  unsetRef: (
    id: string | undefined | Array<string | undefined>,
    key: string,
  ) => void;
}

export const NodeRefContext = createContext<INodeRefContext | null>(null);

export const NodeRefProvider: FunctionComponent = ({ children }) => {
  const nodes = useRef<NodeRefMap>({});
  const setRef = ({ id, ...props }: NodeRefPropsWithId) => {
    const key = `${id}-${Date.now()}`;
    const ids = Array.isArray(id) ? id : [id];
    for (let index = 0; index < ids.length; index++) {
      const curId = ids[index];
      if (curId) {
        nodes.current[ids[index]] = {
          ...props,
          key,
        };
      }
    }
    return key;
  };
  const unsetRef = (
    id: string | undefined | Array<string | undefined>,
    key: string,
  ) => {
    const ids = Array.isArray(id) ? id : [id];
    for (let index = 0; index < ids.length; index++) {
      const curId = ids[index];
      if (curId && nodes.current[curId].key === key) {
        delete nodes.current[curId];
      }
    }
  };

  return (
    <NodeRefContext.Provider
      value={{
        nodes,
        setRef,
        unsetRef,
      }}
    >
      {children}
    </NodeRefContext.Provider>
  );
};

export function useNodeRef(props: NodeRefPropsWithOptionalId) {
  const context = useContext(NodeRefContext);
  useEffect(() => {
    if (context && props.id) {
      const { setRef, unsetRef } = context;
      const key = setRef(props as NodeRefPropsWithId);
      return () => unsetRef(props.id!, key);
    }
    return;
  }, [context, props]);
}

export function useNodeRect() {
  const context = useContext(NodeRefContext);
  const { getBoundingCanvasRect } = useBoundingCanvasRect();
  const getRect = useCallback(
    (id: string): NodeRect | null => {
      if (!context) {
        return null;
      }
      const { nodes } = context;
      const node = nodes.current[id];
      const element = node ? node.ref.current : undefined;
      if (element) {
        let dimensions = getBoundingCanvasRect(element);
        if (dimensions.height === 0 && node.parentId) {
          return getRect(node.parentId);
        }
        let boundaryRect;
        if (node.boundaryId && (boundaryRect = getRect(node.boundaryId))) {
          const overrideWidthRect = node.overrideWidth
            ? getRect(node.overrideWidth)
            : undefined;
          const overrideHeightRect = node.overrideHeight
            ? getRect(node.overrideHeight)
            : undefined;
          const width = overrideWidthRect?.width || dimensions.width;
          const height = overrideHeightRect?.height || dimensions.height;
          const x = Math.min(
            Math.max(
              overrideWidthRect?.left || dimensions.left,
              boundaryRect.left,
            ),
            boundaryRect.width + boundaryRect.left,
          );
          const y = Math.min(
            Math.max(
              overrideHeightRect?.top || dimensions.top,
              boundaryRect.top,
            ),
            boundaryRect.height + boundaryRect.top,
          );
          return {
            width,
            height,
            x,
            y,
            left: x,
            top: y,
            right: x + width,
            bottom: y + height,
            toJSON: () => "",
            clipped:
              x !== (overrideWidthRect?.x || dimensions.x) ||
              y !== (overrideHeightRect?.y || dimensions.y),
          };
        }
        return dimensions;
      }
      return null;
    },
    [context, getBoundingCanvasRect],
  );
  return getRect;
}
