import React, {
  createContext,
  FunctionComponent,
  useContext,
  useRef,
} from 'react';

interface IFieldCapabilities {
  requireVisible: (require: boolean) => void;
}

interface ICanvasViewFieldsContext {
  addField: (id: string, capabilities: IFieldCapabilities) => void;
  removeField: (id: string) => void;
  requireVisible: (id: string, visible: boolean) => void;
}
const CanvasViewFieldsContext = createContext<
  ICanvasViewFieldsContext | undefined
>(undefined);

export const CanvasViewFieldsProvider: FunctionComponent = ({ children }) => {
  const fields = useRef<{ [id: string]: IFieldCapabilities }>({});

  const addField = (id: string, capabilities: IFieldCapabilities) => {
    fields.current[id] = capabilities;
  };

  const requireVisible = (id: string, visible: boolean) => {
    const field = fields.current[id];
    field && field.requireVisible(visible);
  };

  const removeField = (id: string) => {
    delete fields.current[id];
  };

  return (
    <CanvasViewFieldsContext.Provider
      value={{
        addField,
        removeField,
        requireVisible,
      }}
    >
      {children}
    </CanvasViewFieldsContext.Provider>
  );
};

export function useCanvasViewFieldsContext() {
  const context = useContext(CanvasViewFieldsContext);
  if (!context) {
    throw new Error(
      `CanvasViewFields compound components cannot be rendered outside the CanvasViewFields component`
    );
  }
  return context;
}
