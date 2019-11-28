import { TopologyView } from '@patternfly/react-topology';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { Loading } from '../common';
import { ElementId, ElementType, IFieldsGroup, IMappings } from '../models';
import {
  CanvasView,
  CanvasViewControlBar,
  CanvasViewProvider,
  CanvasViewToolbar,
} from '../views/CanvasView';
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
  exportAtlasFile: (event: any) => void;
  importAtlasFile: (selectedFile: File) => void;
  resetAtlasmap: () => void;
}

export const Mapper: FunctionComponent<IMapperProps> = ({
  sources,
  mappings,
  targets,
  addToMapping,
  pending,
  error,
  exportAtlasFile,
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

  const contextToolbar = useMemo(() => 
    <MapperContextToolbar
      exportAtlasFile={exportAtlasFile}
      importAtlasFile={importAtlasFile}
      resetAtlasmap={resetAtlasmap}
    />, [exportAtlasFile, importAtlasFile, resetAtlasmap]);

  return (
    <CanvasViewProvider>
      <TopologyView
        contextToolbar={contextToolbar}
        viewToolbar={<CanvasViewToolbar />}
        controlBar={<CanvasViewControlBar />}
        sideBar={sideBar}
        sideBarOpen={isEditingMapping}
      >
        {pending && <Loading />}
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
          />
        )}
      </TopologyView>
    </CanvasViewProvider>
  );
};
