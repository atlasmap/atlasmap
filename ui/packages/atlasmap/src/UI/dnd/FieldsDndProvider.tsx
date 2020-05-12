import React, {
  FunctionComponent,
  createContext,
  useRef,
  useContext,
} from "react";
import { DndProvider } from "react-dnd";
import TouchBackend from "react-dnd-touch-backend";
import Html5Backend from "react-dnd-html5-backend";
import { IDragAndDropField } from "./models";

const probablyTouch =
  window.matchMedia &&
  window.matchMedia("(pointer: none)").matches &&
  window.matchMedia("(hover: none)").matches;
const maybeTouch =
  window.matchMedia &&
  (window.matchMedia("(pointer: coarse)").matches ||
    window.matchMedia("(hover: none)").matches); // iOS is triggering this check, probably because it supports the Apple Pencil

const TouchOnlyProvider: FunctionComponent = ({ children }) => (
  <DndProvider backend={TouchBackend}>{children}</DndProvider>
);

const TouchAndPointerProvider: FunctionComponent = ({ children }) => (
  <DndProvider backend={TouchBackend} options={{ enableMouseEvents: true }}>
    {children}
  </DndProvider>
);

const MouseOnlyProvider: FunctionComponent = ({ children }) => (
  <DndProvider backend={Html5Backend}>{children}</DndProvider>
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
  const Provider = probablyTouch
    ? TouchOnlyProvider
    : maybeTouch
    ? TouchAndPointerProvider
    : MouseOnlyProvider;
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
