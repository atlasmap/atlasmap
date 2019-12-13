import React, { FunctionComponent } from 'react';
import {
  FieldsBoxHeader,
  Document,
  IFieldsNode,
  IFieldsGroup,
  Target, ElementId, GroupId,
} from '../../CanvasView';
import { DocumentField } from './DocumentField';
import { DocumentFooter } from './DocumentFooter';
import {IAtlasmapDocument, IAtlasmapField, IAtlasmapGroup} from '../models';
import { useAtlasmapUI } from '../AtlasmapUIProvider';
import { DropTarget } from './DropTarget';
import { DocumentFieldPreviewResults } from './DocumentFieldPreviewResults';


export interface IAtlasmapCanvasViewTargetProps {
  onAddToMapping: (elementId: ElementId, mappingId: string) => void;
  onCreateMapping: (sourceId: ElementId, targetId: ElementId) => void;
  onDeleteDocument: (id: GroupId) => void;
  onImportDocument: (selectedFile: File) => void;
  onSearch: (content: string) => void;
  showMappingPreview: boolean;
  showTypes: boolean;
  targets: Array<IAtlasmapDocument>;
}

export const AtlasmapCanvasViewTarget: FunctionComponent<IAtlasmapCanvasViewTargetProps> = ({
  onAddToMapping,
  onCreateMapping,
  onDeleteDocument,
  onImportDocument,
  onSearch,
  showMappingPreview,
  showTypes,
  targets,
}) => {
  const {
    currentMapping,
    isEditingMapping,
    isFieldAddableToSelection,
    isFieldPartOfSelection,
    selectedMapping,
  } = useAtlasmapUI();
  return (
    <Target
      header={
        <FieldsBoxHeader
          title={'Target'}
          onSearch={onSearch}
          onImport={onImportDocument}
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
            renderGroup={node => (node as IAtlasmapGroup).name}
            renderNode={(node, getCoords, boxRef) => {
              const { id, name, type, previewValue } = node as IAtlasmapField &
                (IFieldsNode | IFieldsGroup);
              const showPreview =
                isFieldPartOfSelection(id) && showMappingPreview;
              return (
                <DropTarget
                  key={id}
                  boxRef={boxRef}
                  onDrop={sourceId => onCreateMapping(sourceId, id)}
                  isFieldDroppable={() => !isEditingMapping}
                >
                  {({ isOver }) => (
                    <DocumentField
                      id={id}
                      name={name}
                      type={type}
                      documentType={'target'}
                      showType={showTypes}
                      getCoords={getCoords}
                      isSelected={isFieldPartOfSelection(id)}
                      showAddToMapping={isFieldAddableToSelection(
                        currentMapping,
                        'target',
                        id
                      )}
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
                  )}
                </DropTarget>
              );
            }}
            onDelete={() => onDeleteDocument(t.id)}
          />
        );
      })}
    </Target>
  );
};
