import React, { FunctionComponent } from 'react';
import {
  FieldsBoxHeader,
  Document,
  IFieldsNode,
  IFieldsGroup,
  Target,
  GroupId,
  IMapping,
} from '../../CanvasView';
import { DocumentField } from './DocumentField';
import { DocumentFooter } from './DocumentFooter';
import { IAtlasmapDocument, IAtlasmapField, IAtlasmapGroup } from '../models';
import { useAtlasmapUI } from '../AtlasmapUIProvider';
import { DropTarget } from './DropTarget';
import { DocumentFieldPreviewResults } from './DocumentFieldPreviewResults';

export interface IAtlasmapCanvasViewTargetProps {
  onAddToMapping: (node: IAtlasmapField, mapping: IMapping) => void;
  onCreateMapping: (source: IAtlasmapField, target: IAtlasmapField) => void;
  onDeleteDocument: (id: GroupId) => void;
  onImportDocument: (selectedFile: File) => void;
  onSearch: (content: string) => void;
  showMappingPreview: boolean;
  showTypes: boolean;
  targets: Array<IAtlasmapDocument>;
}

export const AtlasmapCanvasViewTarget: FunctionComponent<
  IAtlasmapCanvasViewTargetProps
> = ({
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
    selectMapping,
    isEditingMapping,
    isFieldAddableToSelection,
    isFieldPartOfSelection,
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
                  onDrop={item => item.onCreateMapping(node as IAtlasmapField)}
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
                      onDropToMapping={mapping => {
                        selectMapping(mapping.id);
                        onAddToMapping(node as IAtlasmapField, mapping);
                      }}
                      onClickAddToMapping={() =>
                        currentMapping &&
                        onAddToMapping(node as IAtlasmapField, currentMapping)
                      }
                      onCreateMapping={target => {
                        onCreateMapping(node as IAtlasmapField, target);
                      }}
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
