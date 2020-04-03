import React, {
  createContext,
  FunctionComponent,
  ReactElement,
  useCallback,
  useContext,
  useRef,
  useState,
} from 'react';
import { ElementId, IMapping } from '../CanvasView';
import { IAtlasmapDocument } from './models';

export interface IRenderMappingDetailsArgs {
  mapping: IMapping;
  closeDetails: () => void;
}

export interface IAtlasmapUIContext {
  selectedMapping: string | undefined;
  selectMapping: (mapping: string) => void;
  deselectMapping: () => void;
  editMapping: () => void;
  isEditingMapping: boolean;
  closeMappingDetails: () => void;
  isFieldAddableToSelection: (
    mapping: IMapping | undefined,
    documentType: string,
    fieldId: ElementId
  ) => boolean;
  currentMapping: IMapping | undefined;
  isFieldPartOfSelection: (id: string) => boolean;
  error: boolean;
  pending: boolean;
  sources: Array<IAtlasmapDocument>;
  targets: Array<IAtlasmapDocument>;
  mappings: IMapping[];
  renderMappingDetails: (props: IRenderMappingDetailsArgs) => ReactElement;
  deselectMappingOnWhitespaceClicked: () => void;
  onDocumentSelected: () => void;
}

const AtlasmapUIContext = createContext<IAtlasmapUIContext | null>(null);

export interface IAtlasmapUIProviderProps {
  error: boolean;
  pending: boolean;
  sources: Array<IAtlasmapDocument>;
  targets: Array<IAtlasmapDocument>;
  mappings: IMapping[];
  onActiveMappingChange: (id: string) => void;
  renderMappingDetails: (props: IRenderMappingDetailsArgs) => ReactElement;
}

export const AtlasmapUIProvider: FunctionComponent<IAtlasmapUIProviderProps> = ({
  children,
  ...props
}) => {
  const { mappings, onActiveMappingChange } = props;
  const [selectedMapping, setSelectedMapping] = useState<string>();
  const [isEditingMapping, setisEditingMapping] = useState(false);

  const mappingSelected = useRef<boolean>(false);
  const mappingDeselected = useRef<boolean>(false);
  const documentSelected = useRef<boolean>(false);

  const closeMappingDetails = useCallback(() => {
    setisEditingMapping(false);
  }, [setisEditingMapping]);

  const selectMapping = useCallback(
    (mapping: string) => {
      onActiveMappingChange(mapping);
      setSelectedMapping(mapping);
      mappingSelected.current = true;
    },
    [onActiveMappingChange, setSelectedMapping]
  );

  const deselectMapping = useCallback(() => {
    setSelectedMapping(undefined);
    mappingDeselected.current = true;
  }, [setSelectedMapping]);

  const editMapping = useCallback(() => {
    if (selectedMapping) {
      setisEditingMapping(true);
    }
  }, [selectedMapping, setisEditingMapping]);

  const onDocumentSelected = () => {
    documentSelected.current = true;
  };

  const deselectMappingOnWhitespaceClicked = useCallback(() => {
    if (
      !mappingSelected.current &&
      !mappingDeselected.current &&
      !documentSelected.current
    ) {
      closeMappingDetails();
      deselectMapping();
    }
    mappingSelected.current = false;
    mappingDeselected.current = false;
    documentSelected.current = false;
  }, [closeMappingDetails, deselectMapping]);

  const isFieldAddableToSelection = (
    mapping: IMapping | undefined,
    documentType: string,
    fieldId: ElementId
  ) => {
    if (!mapping) {
      return false;
    }
    if (mapping.sourceFields.length <= 1 && mapping.targetFields.length <= 1) {
      if (
        documentType === 'source' &&
        !mapping.sourceFields.find(f => f.id === fieldId)
      ) {
        return true;
      } else if (!mapping.targetFields.find(f => f.id === fieldId)) {
        return true;
      }
    } else if (
      documentType === 'source' &&
      mapping.targetFields.length <= 1 &&
      !mapping.sourceFields.find(f => f.id === fieldId)
    ) {
      return true;
    } else if (
      documentType === 'target' &&
      mapping.sourceFields.length <= 1 &&
      !mapping.targetFields.find(f => f.id === fieldId)
    ) {
      return true;
    }
    return false;
  };

  const currentMapping = mappings.find(m => m.id === selectedMapping);

  const isFieldPartOfSelection = (id: string) => {
    const mapped = currentMapping;
    if (mapped) {
      return !!(
        mapped.sourceFields.find(f => f.id === id) ||
        mapped.targetFields.find(f => f.id === id)
      );
    }
    return false;
  };

  return (
    <AtlasmapUIContext.Provider
      value={{
        ...props,
        selectedMapping,
        selectMapping,
        deselectMapping,
        editMapping,
        isEditingMapping,
        closeMappingDetails,
        isFieldAddableToSelection,
        currentMapping,
        isFieldPartOfSelection,
        deselectMappingOnWhitespaceClicked,
        onDocumentSelected,
      }}
    >
      {children}
    </AtlasmapUIContext.Provider>
  );
};

export function useAtlasmapUI() {
  const context = useContext(AtlasmapUIContext);
  if (!context) {
    throw new Error(
      `Atlasmap compound components cannot be rendered outside an AtlasmapProvider component`
    );
  }
  return context;
}
