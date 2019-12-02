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
  Document,
  FieldGroup,
  FieldsBoxHeader,
  Links,
  Mapping,
  MappingElement,
} from '../views/CanvasView';
import { Source } from '../views/CanvasView/components/Source';
import { Target } from '../views/CanvasView/components/Target';
import { AtlasmapContextToolbar } from './AtlasmapContextToolbar';
import { MappingDetails } from './MappingDetails';

export interface IAtlasmapProps {
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
  onExportAtlasFile: (event: any) => void;
  onImportAtlasFile: (selectedFile: File) => void;
  onResetAtlasmap: () => void;
  onSourceSearch: (content: string) => void;
  onTargetSearch: (content: string) => void;
}

export const Atlasmap: FunctionComponent<IAtlasmapProps> = ({
  sources,
  mappings,
  targets,
  addToMapping,
  pending,
  error,
  onExportAtlasFile,
  onImportAtlasFile,
  onResetAtlasmap,
  onSourceSearch,
  onTargetSearch,
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
      <AtlasmapContextToolbar
        onExportAtlasFile={onExportAtlasFile}
        onImportAtlasFile={onImportAtlasFile}
        onResetAtlasmap={onResetAtlasmap}
      />
    ),
    [onImportAtlasFile, onResetAtlasmap]
  );

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
          <CanvasView>
            <Source
              header={
                <FieldsBoxHeader
                  title={'Source'}
                  onSearch={onSourceSearch}
                  onImport={() => void(0)}
                  onJavaClasses={() => void(0)}
                />
              }
            >
              {sources.map(s => {
                return (
                  <Document key={s.id} title={s.title} footer={'Source document'}>
                    {({ getRef, isExpanded, expandFields }) => (
                      <FieldGroup
                        isVisible={true}
                        group={s}
                        getBoxRef={getRef}
                        type={'source'}
                        rightAlign={false}
                        parentExpanded={isExpanded}
                        initiallyExpanded={expandFields}
                      />
                    )}
                  </Document>
                );
              })}
            </Source>
            <Mapping>
              {({ ref }) => (
                <>
                  {mappings.map(m => {
                    return (
                      <MappingElement
                        key={m.id}
                        node={m}
                        boxRef={ref}
                        selectedMapping={selectedMapping}
                        selectMapping={selectMapping}
                        deselectMapping={deselectMapping}
                        editMapping={editMapping}
                        addToMapping={addToMapping}
                        mappingType={'Split'}
                      />
                    );
                  })}
                </>
              )}
            </Mapping>
            <Target
              header={
                <FieldsBoxHeader
                  title={'Target'}
                  onSearch={onTargetSearch}
                  onImport={() => void(0)}
                  onJavaClasses={() => void(0)}
                />
              }
            >
              {targets.map(t => {
                return (
                  <Document key={t.id} title={t.title} rightAlign={true} footer={'Target document'}>
                    {({ getRef, isExpanded, expandFields }) => (
                      <FieldGroup
                        isVisible={true}
                        group={t}
                        getBoxRef={getRef}
                        type={'target'}
                        rightAlign={true}
                        parentExpanded={isExpanded}
                        initiallyExpanded={expandFields}
                      />
                    )}
                  </Document>
                );
              })}
            </Target>

            <Links mappings={mappings} selectedMapping={selectedMapping} />
          </CanvasView>
        )}
      </TopologyView>
    </CanvasViewProvider>
  );
};
