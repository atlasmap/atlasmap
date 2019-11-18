import React, {
  createContext,
  FunctionComponent,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from 'react';
import { Coords } from '../models';
import { useCanvas } from './CanvasContext';

export type SourceTargetNodes = {
  color: string;
  start: string;
  end: string;
};

export type SourceTargetLine = {
  id: string;
  color: string;
  start: Coords;
  end: Coords;
};

export interface LinkNodes {
  [id: string]: () => Coords;
}

interface ILinksContext {
  nodes: LinkNodes;
  setLineNode: (id: string, getCoords: () => Coords) => void;
}
const LinksContext = createContext<ILinksContext | null>(null);

export const CanvasLinksProvider: FunctionComponent = ({ children }) => {
  const nodes = useRef<LinkNodes>({});
  const setLineNode = useCallback(
    (id: string, getCoords: () => Coords) => {
      nodes.current[id] = getCoords;
    },
    [nodes]
  );

  return (
    <LinksContext.Provider value={{ nodes: nodes.current, setLineNode }}>
      {children}
    </LinksContext.Provider>
  );
};

export function useCanvasLinks(linkedNodes: SourceTargetNodes[]) {
  const { addRedrawListener, removeRedrawListener } = useCanvas();
  const context = useContext(LinksContext);
  if (!context) {
    throw new Error('A LinksProvider wrapper is required to use this hook.');
  }

  const { nodes } = context;
  const [links, setLinks] = useState<SourceTargetLine[]>([]);

  const calculateLinks = useCallback(() => {
    const updatedLinks = linkedNodes
      .map(
        ({
          color,
          start: sourceId,
          end: targetId,
        }): SourceTargetLine | null => {
          const source = nodes[sourceId];
          const target = nodes[targetId];
          if (source && target) {
            return {
              id: `${sourceId}-${targetId}`,
              start: source(),
              end: target(),
              color,
            };
          }
          return null;
        }
      )
      .filter(a => a) as SourceTargetLine[];
    setLinks(updatedLinks);
  }, [nodes, linkedNodes, setLinks]);

  useEffect(() => {
    addRedrawListener(calculateLinks);
    return () => {
      removeRedrawListener(calculateLinks);
    };
  }, [addRedrawListener, removeRedrawListener, calculateLinks]);

  return {
    links,
    calculateLinks,
  };
}

export function useMappingNode() {
  const context = useContext(LinksContext);
  if (!context) {
    throw new Error('A LinksProvider wrapper is required to use this hook.');
  }
  const { setLineNode } = context;
  return setLineNode;
}
