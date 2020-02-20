import React, {
  FunctionComponent,
  createContext,
  useState,
  useContext,
} from "react";
import { DndProvider } from "react-dnd";
import TouchBackend from "react-dnd-touch-backend";
import { IDragAndDropField } from "./models";

interface IFieldsDndContext {
  hoveredTarget: IDragAndDropField | null;
  setHoveredTarget: (target: IDragAndDropField | null) => void;
}

const FieldsDndContext = createContext<IFieldsDndContext | null>(null);

export const FieldsDndProvider: FunctionComponent = ({ children }) => {
  const [hoveredTarget, setHoveredTarget] = useState<IDragAndDropField | null>(
    null,
  );
  return (
    <DndProvider
      backend={TouchBackend}
      options={{ enableMouseEvents: true, delay: window.Touch ? 100 : 0 }}
    >
      <FieldsDndContext.Provider value={{ hoveredTarget, setHoveredTarget }}>
        {children}
      </FieldsDndContext.Provider>
    </DndProvider>
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
