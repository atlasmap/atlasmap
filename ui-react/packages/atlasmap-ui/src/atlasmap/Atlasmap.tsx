import { ConnectedIcon, DisconnectedIcon, EyeIcon, InfoIcon } from '@patternfly/react-icons';
import { TopologyView } from '@patternfly/react-topology';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { Loading } from '../common';
import { ElementId, DocumentType, IFieldsGroup, IMappings, IFieldsNode, GroupId } from '../views/CanvasView';
import {
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
import { Source } from '../views/CanvasView/components/Source';
import { Target } from '../views/CanvasView/components/Target';
import { AtlasmapContextToolbar } from './AtlasmapContextToolbar';
import { DocumentFooter } from './components';
import { DocumentField } from './components/DocumentField';
import { MappingDetails } from './MappingDetails';

export interface IDocumentField {
  name: string;
  type: string;
}

export interface IDocument extends IFieldsGroup {
  name: string;
  type: string;
  fields: Array<IFieldsGroup & IDocumentField | IFieldsNode & IDocumentField>
}

export interface IAtlasmapProps {
  sources: IDocument[];
  targets: IDocument[];
  mappings: IMappings[];
  addToMapping: (
    elementId: ElementId,
    elementType: DocumentType,
    mappingId: string
  ) => void;
  pending: boolean;
  error: boolean;
  onExportAtlasFile: (event: any) => void;
  onImportAtlasFile: (selectedFile: File) => void;
  onImportSourceDocument: (selectedFile: File) => void;
  onImportTargetDocument: (selectedFile: File) => void;
  onDeleteSourceDocument: (fileName: GroupId) => void;
  onDeleteTargetDocument: (fileName: GroupId) => void;
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
  onImportSourceDocument,
  onImportTargetDocument,
  onDeleteSourceDocument,
  onDeleteTargetDocument,
  onResetAtlasmap,
  onSourceSearch,
  onTargetSearch
}) => {
  const [selectedMapping, setSelectedMapping] = useState<string>();
  const [isEditingMapping, setisEditingMapping] = useState(false);
  const [showTypes, setShowTypes] = useState(false);
  const toggleShowTypes = useCallback(() => setShowTypes(!showTypes), [showTypes]);

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
    [onExportAtlasFile, onImportAtlasFile, onResetAtlasmap]
  );

  const controlBar = useMemo(() =>
    <CanvasViewControlBar
      extraButtons={[
        {
          id: 'Show types',
          icon: <InfoIcon />,
          tooltip: 'Show types',
          ariaLabel: ' ',
          callback: toggleShowTypes
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
    />,
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
                  onJavaClasses={() => void(0)}
                />
              }
            >
              {sources.map(s => {
                return (
                  <Document<IDocumentField>
                    key={s.id}
                    title={s.name}
                    footer={
                      <DocumentFooter
                        title={'Source document'}
                        type={s.type}
                        showType={showTypes}
                      />
                    }
                    type={'source'}
                    lineConnectionSide={'right'}
                    fields={s}
                    renderNode={
                      (node: IDocumentField) =>
                        <DocumentField
                          name={node.name}
                          type={node.type}
                          showType={showTypes}
                        />
                    }
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
                  onImport={onImportTargetDocument}
                  onJavaClasses={() => void(0)}
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
                        title='Target document'
                        type={t.type}
                        showType={showTypes}
                      />
                    }
                    type={'target'}
                    lineConnectionSide={'left'}
                    fields={t}
                    renderNode={
                      (node: IDocumentField) =>
                        <DocumentField
                          name={node.name}
                          type={node.type}
                          showType={showTypes}
                        />
                    }
                    onDelete={() => onDeleteTargetDocument(t.id)}
                  />
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
