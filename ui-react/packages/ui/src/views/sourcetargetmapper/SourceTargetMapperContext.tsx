import React, {
  createContext,
  FunctionComponent,
  useCallback,
  useContext,
  useEffect,
  useState,
} from 'react';
import { useCanvas } from '../../canvas';

interface ISourceTargetMapperContext {
  focusedMapping?: string;
  focusMapping: (id: string) => void;
  blurMapping: () => void;
}
const SourceTargetMapperContext = createContext<ISourceTargetMapperContext>({
  focusMapping: () => void 0,
  blurMapping: () => void 0,
});

export interface ISourceTargetMapperProviderProps {
  selectedMapping?: string;
}

export const SourceTargetMapperProvider: FunctionComponent<ISourceTargetMapperProviderProps> = ({ children, selectedMapping }) => {
  const { redraw } = useCanvas();
  const [focusedMapping, setFocusedMapping] = useState<string>();
  const focusMapping = useCallback((mapping: string) => !selectedMapping && setFocusedMapping(mapping), [
    setFocusedMapping, selectedMapping
  ]);
  const blurMapping = useCallback(() => setFocusedMapping(undefined), [
    setFocusedMapping,
  ]);
  useEffect(() => {
    redraw();
  }, [focusedMapping]);
  return (
    <SourceTargetMapperContext.Provider
      value={{
        focusedMapping: selectedMapping || focusedMapping,
        focusMapping,
        blurMapping,
      }}
    >
      {children}
    </SourceTargetMapperContext.Provider>
  );
};

export function useSourceTargetMapper() {
  const context = useContext(SourceTargetMapperContext);
  if (!context) {
    throw new Error('A CanvasProvider wrapper is required to use this hook.');
  }
  const { focusedMapping, focusMapping, blurMapping } = context;
  return {
    focusMapping: focusMapping,
    focusedMapping: focusedMapping,
    blurMapping: blurMapping,
  };
}
