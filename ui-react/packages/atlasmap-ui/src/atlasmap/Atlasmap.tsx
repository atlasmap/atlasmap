import {
  ConnectedIcon,
  DisconnectedIcon,
  EyeIcon,
  InfoIcon,
} from '@patternfly/react-icons';
import { TopologyView } from '@patternfly/react-topology';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Loading } from '../common';
import {
  ElementId,
  IFieldsGroup,
  IMappings,
  IFieldsNode,
  GroupId,
  Target,
  Source,
  CanvasView,
  CanvasViewControlBar,
  CanvasViewProvider,
  CanvasViewToolbar,
  Document,
  FieldsBoxHeader,
  Links,
  Mapping,
  MappingElement,
} from '../views/CanvasView';
import { AtlasmapContextToolbar } from './AtlasmapContextToolbar';
import {
  DocumentFooter,
  DragLayer,
  DropTarget,
  DocumentField,
} from './components';
import { MappingDetails } from './MappingDetails';

export interface IDocumentField extends Object {
  name: string;
  type: string;
}

export interface IDocument extends IFieldsGroup {
  name: string;
  type: string;
  fields: Array<IFieldsGroup & IDocumentField | IFieldsNode & IDocumentField>;
}

export interface IAtlasmapProps {
  sources: Array<IDocument>;
  targets: Array<IDocument>;
  mappings: IMappings[];
  addToMapping: (elementId: ElementId, mappingId: string) => void;
  pending: boolean;
  error: boolean;
  onExportAtlasFile: (event: any) => void;
  onImportAtlasFile: (selectedFile: File) => void;
  onImportSourceDocument: (selectedFile: File) => void;
  onImportTargetDocument: (selectedFile: File) => void;
  onDeleteSourceDocument: (id: GroupId) => void;
  onDeleteTargetDocument: (id: GroupId) => void;
  onResetAtlasmap: () => void;
  onSourceSearch: (content: string) => void;
  onTargetSearch: (content: string) => void;
}

export function Atlasmap({
  sources,
  mappings,
  targets,
  addToMapping,
  pending,
  error,
  onExportAtlasFile,
  onImportAtlasFile,
  onImportSourceDocument,
  onImportTargetDocument,
  onDeleteSourceDocument,
  onDeleteTargetDocument,
  onResetAtlasmap,
  onSourceSearch,
  onTargetSearch,
}: IAtlasmapProps) {
  const [selectedMapping, setSelectedMapping] = useState<string>();
  const [isEditingMapping, setisEditingMapping] = useState(false);
  const [showTypes, setShowTypes] = useState(false);
  const toggleShowTypes = useCallback(() => setShowTypes(!showTypes), [
    showTypes,
  ]);

  const closeMappingDetails = useCallback(() => {
    setisEditingMapping(false);
  }, [setisEditingMapping]);

  const selectMapping = useCallback(
    (mapping: string) => {
      if (!isEditingMapping) {
        setSelectedMapping(mapping);
      }
    },
    [isEditingMapping]
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
    [onExportAtlasFile, onImportAtlasFile, onResetAtlasmap]
  );

  const controlBar = useMemo(
    () => (
      <CanvasViewControlBar
        extraButtons={[
          {
            id: 'Show types',
            icon: <InfoIcon />,
            tooltip: 'Show types',
            ariaLabel: ' ',
            callback: toggleShowTypes,
          },
          {
            id: 'Show mapped fields',
            icon: <ConnectedIcon />,
            tooltip: 'Show mapped fields',
            ariaLabel: ' ',
          },
          {
            id: 'Show unmapped fields',
            icon: <DisconnectedIcon />,
            tooltip: 'Show unmapped fields',
            ariaLabel: ' ',
          },
          {
            id: 'Show mapping preview',
            icon: <EyeIcon />,
            tooltip: 'Show mapping preview',
            ariaLabel: ' ',
          },
        ]}
      />
    ),
    [toggleShowTypes]
  );

  return (
    <CanvasViewProvider>
      <TopologyView
        contextToolbar={contextToolbar}
        viewToolbar={<CanvasViewToolbar />}
        controlBar={controlBar}
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
                  onImport={onImportSourceDocument}
                  onJavaClasses={() => void 0}
                />
              }
            >
              {sources.map(s => {
                return (
                  <Document
                    key={s.id}
                    title={s.name}
                    footer={
                      <DocumentFooter
                        title={'Source document'}
                        type={s.type}
                        showType={showTypes}
                      />
                    }
                    lineConnectionSide={'right'}
                    fields={s}
                    renderNode={(node, getCoords) => {
                      const { id, name, type } = node as IDocumentField &
                        (IFieldsNode | IFieldsGroup);
                      return (
                        <DocumentField
                          id={id}
                          name={name}
                          type={type}
                          documentType={'source'}
                          showType={showTypes}
                          getCoords={getCoords}
                        />
                      );
                    }}
                    onDelete={() => onDeleteSourceDocument(s.id)}
                  />
                );
              })}
            </Source>
            <Mapping>
              {({ ref }) => (
                <>
                  {mappings.map(m => {
                    return (
                      <DropTarget
                        key={m.id}
                        node={m}
                        boxRef={ref}
                        addToMapping={addToMapping}
                      >
                        {({ canDrop, isOver }) => (
                          <MappingElement
                            boxRef={ref}
                            node={m}
                            selectedMapping={selectedMapping}
                            selectMapping={selectMapping}
                            deselectMapping={deselectMapping}
                            editMapping={editMapping}
                            mappingType={'Split'}
                            canDrop={canDrop}
                            isOver={isOver}
                          />
                        )}
                      </DropTarget>
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
                  onImport={onImportTargetDocument}
                  onJavaClasses={() => void 0}
                />
              }
            >
              {targets.map(t => {
                return (
                  <Document
                    key={t.id}
                    title={t.name}
                    footer={
                      <DocumentFooter
                        title="Target document"
                        type={t.type}
                        showType={showTypes}
                      />
                    }
                    lineConnectionSide={'left'}
                    fields={t}
                    renderNode={(node, getCoords) => {
                      const { id, name, type } = node as IDocumentField &
                        (IFieldsNode | IFieldsGroup);
                      return (
                        <DocumentField
                          id={id}
                          name={name}
                          type={type}
                          documentType={'target'}
                          showType={showTypes}
                          getCoords={getCoords}
                        />
                      );
                    }}
                    onDelete={() => onDeleteTargetDocument(t.id)}
                  />
                );
              })}
            </Target>

            <Links mappings={mappings} selectedMapping={selectedMapping} />

            <DragLayer />
          </CanvasView>
        )}
      </TopologyView>
    </CanvasViewProvider>
  );
}
