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
  DocumentFieldPreview,
  DocumentFieldPreviewResults,
} from './components';
import { MappingDetails } from './MappingDetails';

export type AtlasmapFields = Array<IAtlasmapGroup | IAtlasmapField>;

export interface IAtlasmapField extends IFieldsNode {
  name: string;
  type: string;
  previewValue: string;
}

export interface IAtlasmapGroup extends IFieldsGroup {
  name: string;
  type: string;
}

export interface IAtlasmapDocument {
  id: string;
  name: string;
  type: string;
  fields: AtlasmapFields;
}

export interface IAtlasmapProps {
  sources: Array<IAtlasmapDocument>;
  targets: Array<IAtlasmapDocument>;
  mappings: IMappings[];
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
  onActiveMappingChange: (id: string) => void;
  onShowMappingPreview: (enabled: boolean) => void;
  onFieldPreviewChange: (field: IAtlasmapField, value: string) => void;
  onAddToMapping: (elementId: ElementId, mappingId: string) => void;
  onCreateMapping: (sourceId: ElementId, targetId: ElementId) => void;
}

export function Atlasmap({
  sources,
  mappings,
  targets,
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
  onActiveMappingChange,
  onShowMappingPreview,
  onFieldPreviewChange,
  onAddToMapping,
  onCreateMapping,
}: IAtlasmapProps) {
  const [selectedMapping, setSelectedMapping] = useState<string>();
  const [isEditingMapping, setisEditingMapping] = useState(false);
  const [showTypes, setShowTypes] = useState(false);
  const toggleShowTypes = useCallback(() => setShowTypes(!showTypes), [
    showTypes,
  ]);
  const [showMappingPreview, setShowMappingPreview] = useState(false);
  const toggleShowMappingPreview = useCallback(() => {
    const newValue = !showMappingPreview;
    setShowMappingPreview(newValue);
    onShowMappingPreview(newValue);
  }, [onShowMappingPreview, showMappingPreview]);

  const closeMappingDetails = useCallback(() => {
    setisEditingMapping(false);
  }, [setisEditingMapping]);

  const selectMapping = useCallback(
    (mapping: string) => {
      if (!isEditingMapping) {
        onActiveMappingChange(mapping);
        setSelectedMapping(mapping);
      }
    },
    [isEditingMapping, onActiveMappingChange]
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
            callback: toggleShowMappingPreview,
          },
        ]}
      />
    ),
    [toggleShowMappingPreview, toggleShowTypes]
  );

  const isMappingColumnVisible = !isEditingMapping;

  const isFieldAddableToSelection = (
    mapping: IMappings | undefined,
    documentType: string,
    fieldId: ElementId
  ) => {
    if (!mapping) {
      return false;
    }
    if (
      mapping.sourceFields.length === 1 &&
      mapping.targetFields.length === 1
    ) {
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
      mapping.targetFields.length === 1 &&
      !mapping.sourceFields.find(f => f.id === fieldId)
    ) {
      return true;
    } else if (
      documentType === 'target' &&
      mapping.sourceFields.length === 1 &&
      !mapping.targetFields.find(f => f.id === fieldId)
    ) {
      return true;
    }
    return false;
  };

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
          <CanvasView isMappingColumnVisible={isMappingColumnVisible}>
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
                    renderGroup={(node) => (node as IAtlasmapGroup).name}
                    renderNode={(node, getCoords) => {
                      const { id, name, type } = node as IAtlasmapField;
                      const showPreview =
                        isFieldPartOfSelection(id) && showMappingPreview;
                      return (
                        <DocumentField
                          id={id}
                          name={name}
                          type={type}
                          documentType={'source'}
                          showType={showTypes}
                          getCoords={getCoords}
                          isSelected={isFieldPartOfSelection(id)}
                          showAddToMapping={
                            isFieldAddableToSelection(
                              currentMapping,
                              'source',
                              id
                            )
                          }
                          onAddToMapping={() =>
                            selectedMapping && onAddToMapping(id, selectedMapping)
                          }
                        >
                          {showPreview && (
                            <DocumentFieldPreview
                              id={id}
                              onChange={value =>
                                onFieldPreviewChange(
                                  node as IAtlasmapField,
                                  value
                                )
                              }
                            />
                          )}
                        </DocumentField>
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
                        boxRef={ref}
                        onDrop={itemId => onAddToMapping(itemId, m.id)}
                        isFieldDroppable={(documentType, fieldId) =>
                          isFieldAddableToSelection(m, documentType, fieldId)
                        }
                      >
                        {({ canDrop, isOver }) => (
                          <MappingElement
                            boxRef={ref}
                            node={m}
                            selectedMapping={selectedMapping}
                            selectMapping={selectMapping}
                            deselectMapping={deselectMapping}
                            editMapping={editMapping}
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
                    renderGroup={(node) => (node as IAtlasmapGroup).name}
                    renderNode={(node, getCoords, boxRef) => {
                      const {
                        id,
                        name,
                        type,
                        previewValue,
                      } = node as IAtlasmapField & (IFieldsNode | IFieldsGroup);
                      const showPreview =
                        isFieldPartOfSelection(id) && showMappingPreview;
                      return (
                        <DropTarget
                          key={id}
                          boxRef={boxRef}
                          onDrop={sourceId => onCreateMapping(sourceId, id)}
                          isFieldDroppable={() => !isEditingMapping}
                        >
                          {({ isOver }) =>
                            <DocumentField
                              id={id}
                              name={name}
                              type={type}
                              documentType={'target'}
                              showType={showTypes}
                              getCoords={getCoords}
                              isSelected={isFieldPartOfSelection(id)}
                              showAddToMapping={
                                isFieldAddableToSelection(
                                  currentMapping,
                                  'target',
                                  id
                                )
                              }
                              onAddToMapping={() =>
                                selectedMapping && onAddToMapping(id, selectedMapping)
                              }
                              isOver={isOver}
                            >
                              {showPreview && (
                                <DocumentFieldPreviewResults
                                  id={id}
                                  value={previewValue}
                                />
                              )}
                            </DocumentField>
                          }
                        </DropTarget>
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
