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
import { DocumentGroup } from './DocumentGroup';

export interface IAtlasmapCanvasViewTargetProps {
  onAddToMapping: (node: IAtlasmapField, mapping: IMapping) => void;
  onCreateMapping: (
    source: IAtlasmapField | undefined,
    target?: IAtlasmapField
  ) => void;
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
    selectMapping,
    isEditingMapping,
    isFieldAddableToSelection,
    isFieldPartOfSelection,
    onDocumentSelected,
  } = useAtlasmapUI();
  return (
    <Target
      header={
        <FieldsBoxHeader
          title={'Target'}
          onSearch={onSearch}
          onImport={onImportDocument}
          onJavaClasses={() => void 0}
          onCreateConstant={() => void 0}
          onCreateProperty={() => void 0}
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
            renderGroup={node => {
              const group = node as IAtlasmapGroup;
              return (
                <DocumentGroup
                  name={group.name}
                  type={group.type}
                  showType={showTypes}
                />
              );
            }}
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
                      onClickAddToMapping={() => {
                        if (currentMapping) {
                          onAddToMapping(
                            node as IAtlasmapField,
                            currentMapping
                          );
                        } else {
                          onCreateMapping(undefined, node as IAtlasmapField);
                        }
                      }}
                      onCreateMapping={target => {
                        onCreateMapping(node as IAtlasmapField, target);
                      }}
                      isOver={isOver}
                      isConstantOrProperty={t.isConstantOrProperty}
                      onDeleteConstProp={() => {
                        void 0;
                      }}
                      onEditConstProp={() => {
                        void 0;
                      }}
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
            onDocumentSelected={onDocumentSelected}
          />
        );
      })}
    </Target>
  );
};
