import React, {
  FunctionComponent,
  createContext,
  useContext,
  useRef,
} from "react";
import { DndProvider } from "react-dnd";
import { HTML5Backend } from "react-dnd-html5-backend";
import { IDragAndDropField } from "./models";
import { TouchBackend } from "react-dnd-touch-backend";

const probablyTouch =
  window.matchMedia &&
  (window.matchMedia("(pointer: none)").matches ||
    window.matchMedia("(pointer: coarse)").matches) &&
  window.matchMedia("(hover: none)").matches;

const TouchAndPointerProvider: FunctionComponent = ({ children }) => {
  return (
    <DndProvider backend={TouchBackend} options={{ enableMouseEvents: true }}>
      {children}
    </DndProvider>
  );
};

const MouseOnlyProvider: FunctionComponent = ({ children }) => (
  <DndProvider backend={HTML5Backend}>{children}</DndProvider>
);

interface IFieldsDndContext {
  getHoveredTarget: () => IDragAndDropField | null;
  setHoveredTarget: (target: IDragAndDropField | null) => void;
}

const FieldsDndContext = createContext<IFieldsDndContext | null>(null);

export const FieldsDndProvider: FunctionComponent = ({ children }) => {
  const hoveredTarget = useRef<IDragAndDropField | null>(null);
  const getHoveredTarget = () => hoveredTarget.current;
  const setHoveredTarget = (target: IDragAndDropField | null) =>
    (hoveredTarget.current = target);
  const Provider = probablyTouch ? TouchAndPointerProvider : MouseOnlyProvider;
  return (
    <Provider>
      <FieldsDndContext.Provider value={{ getHoveredTarget, setHoveredTarget }}>
        {children}
      </FieldsDndContext.Provider>
    </Provider>
  );
};

export function useFieldsDnd() {
  const context = useContext(FieldsDndContext);
  if (!context) {
    throw new Error(
      "A FieldsDndProvider wrapper is required to use this hook.",
    );
  }

  return context;
}
