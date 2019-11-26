import {
  EmptyState,
  EmptyStateIcon,
  EmptyStateVariant,
  Title,
} from '@patternfly/react-core';
import { Spinner } from '@patternfly/react-core/dist/js/experimental';
import { TopologyView } from '@patternfly/react-topology';
import React, {
  FunctionComponent, ReactNode,
  useCallback,
  useEffect,
  useMemo, useRef,
  useState,
} from 'react';
import { ElementId, ElementType, IFieldsGroup, IMappings } from '../models';
import { CanvasView } from '../views/CanvasView';
import { MapperContextToolbar } from './MapperContextToolbar';
import { MappingDetails } from './MappingDetails';

export interface IMapperProps {
  sources: IFieldsGroup[];
  targets: IFieldsGroup[];
  mappings: IMappings[];
  addToMapping: (
    elementId: ElementId,
    elementType: ElementType,
    mappingId: string
  ) => void;
  pending: boolean;
  error: boolean;
  importAtlasFile: (selectedFile: File) => void;
  resetAtlasmap: () => void;
}

function useLatestValue(): [ReactNode | undefined, (el: ReactNode) => void] {
  const [element, setElement] = useState<ReactNode | undefined>();
  const previousElement = useRef<ReactNode | null>();
  const handleSetElement = useCallback(
    (newElement: ReactNode) => {
      if (previousElement.current !== newElement) {
        previousElement.current = newElement;
        setElement(previousElement.current);
      }
    },
    [setElement, previousElement]
  );
  return [element, handleSetElement];
}

export const Mapper: FunctionComponent<IMapperProps> = ({
  sources,
  mappings,
  targets,
  addToMapping,
  pending,
  error,
  importAtlasFile,
  resetAtlasmap,
}) => {
  const [selectedMapping, setSelectedMapping] = useState<string>();
  const [isEditingMapping, setisEditingMapping] = useState(false);


  const closeMappingDetails = useCallback(() => {
    setisEditingMapping(false);
  }, [setisEditingMapping]);

  const selectMapping = useCallback(
    (mapping: string) => {
      if (!isEditingMapping) {
        setSelectedMapping(mapping);
      }
    },
    [setSelectedMapping, isEditingMapping]
  );

  const deselectMapping = useCallback(() => {
    setSelectedMapping(undefined);
  }, [setSelectedMapping]);

  const editMapping = useCallback(() => {
    if (selectedMapping) {
      setisEditingMapping(true);
    }
  }, [selectedMapping, setisEditingMapping]);

  const sideBar = (
    <MappingDetails show={isEditingMapping} onClose={closeMappingDetails}>
      {selectedMapping}
    </MappingDetails>
  );

  useEffect(() => {
    const timeout = setTimeout(
      () => {
        window.dispatchEvent(new Event('resize'));
      },
      isEditingMapping ? 150 : 0
    );
    return () => clearTimeout(timeout);
  }, [isEditingMapping]);

  const contextToolbar = useMemo(
    () => (
      <MapperContextToolbar
        importAtlasFile={importAtlasFile}
        resetAtlasmap={resetAtlasmap}
      />
    ),
    [importAtlasFile, resetAtlasmap]
  );

  const [viewToolbar, setViewToolbar] = useLatestValue();
  const [controlBar, setControlBar] = useLatestValue();

  return (
    <TopologyView
      contextToolbar={contextToolbar}
      viewToolbar={viewToolbar}
      controlBar={controlBar}
      sideBar={sideBar}
      sideBarOpen={isEditingMapping}
    >
      {pending && (
        <EmptyState variant={EmptyStateVariant.full}>
          <EmptyStateIcon variant="container" component={Spinner} />
          <Title size="lg">Loading</Title>
        </EmptyState>
      )}
      {error && <p>Error</p>}
      {!pending && !error && (
        <CanvasView
          sources={sources}
          mappings={mappings}
          targets={targets}
          selectedMapping={selectedMapping}
          selectMapping={selectMapping}
          deselectMapping={deselectMapping}
          editMapping={editMapping}
          addToMapping={addToMapping}
          setViewToolbar={setViewToolbar}
          setControlBar={setControlBar}
        />
      )}
    </TopologyView>
  );
};
