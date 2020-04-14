import React, { FunctionComponent } from 'react';
import {
  FieldsBoxHeader,
  Source,
  Document,
  GroupId,
  IMapping,
} from '../../CanvasView';
import { DocumentField } from './DocumentField';
import { DocumentFooter } from './DocumentFooter';
import { DocumentFieldPreview } from './DocumentFieldPreview';
import { IAtlasmapDocument, IAtlasmapField, IAtlasmapGroup } from '../models';
import { useAtlasmapUI } from '../AtlasmapUIProvider';
import { DocumentGroup } from './DocumentGroup';

export interface IAtlasmapCanvasViewSourceProps {
  onAddToMapping: (field: IAtlasmapField, mapping: IMapping) => void;
  onCreateConstant: () => void;
  onDeleteConstant: (value: string) => void;
  onEditConstant: (value: string) => void;
  onCreateMapping: (source: IAtlasmapField, target?: IAtlasmapField) => void;
  onCreateProperty: () => void;
  onDeleteProperty: (field: string) => void;
  onEditProperty: (field: string) => void;
  onDeleteDocument: (id: GroupId) => void;
  onFieldPreviewChange: (field: IAtlasmapField, value: string) => void;
  onImportDocument: (selectedFile: File) => void;
  onSearch: (content: string) => void;
  sources: Array<IAtlasmapDocument>;
  showMappingPreview: boolean;
  showTypes: boolean;
}

export const AtlasmapCanvasViewSource: FunctionComponent<IAtlasmapCanvasViewSourceProps> = ({
  onAddToMapping,
  onCreateConstant,
  onCreateMapping,
  onCreateProperty,
  onDeleteConstant,
  onDeleteDocument,
  onDeleteProperty,
  onEditConstant,
  onEditProperty,
  onFieldPreviewChange,
  onImportDocument,
  onSearch,
  showMappingPreview,
  showTypes,
  sources,
}) => {
  const {
    currentMapping,
    isFieldAddableToSelection,
    isFieldPartOfSelection,
    selectMapping,
    onDocumentSelected,
  } = useAtlasmapUI();
  return (
    <Source
      header={
        <FieldsBoxHeader
          title={'Source'}
          onSearch={onSearch}
          onImport={onImportDocument}
          onCreateConstant={onCreateConstant}
          onCreateProperty={onCreateProperty}
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
                  isConstantOrProperty={s.isConstantOrProperty}
                  onDeleteConstProp={name => {
                    s.type === 'Constants'
                      ? onDeleteConstant(name)
                      : onDeleteProperty(name);
                  }}
                  onEditConstProp={selectedName => {
                    s.type === 'Constants'
                      ? onEditConstant(selectedName)
                      : onEditProperty(selectedName);
                  }}
                  showAddToMapping={isFieldAddableToSelection(
                    currentMapping,
                    'source',
                    id
                  )}
                  onDropToMapping={mapping => {
                    selectMapping(mapping.id);
                    onAddToMapping(node as IAtlasmapField, mapping);
                  }}
                  onClickAddToMapping={() => {
                    if (currentMapping) {
                      onAddToMapping(node as IAtlasmapField, currentMapping);
                    } else {
                      onCreateMapping(node as IAtlasmapField);
                    }
                  }}
                  onCreateMapping={target =>
                    onCreateMapping(node as IAtlasmapField, target)
                  }
                >
                  {showPreview && (
                    <DocumentFieldPreview
                      id={id}
                      onChange={value =>
                        onFieldPreviewChange(node as IAtlasmapField, value)
                      }
                    />
                  )}
                </DocumentField>
              );
            }}
            onDelete={() => onDeleteDocument(s.id)}
            onDocumentSelected={onDocumentSelected}
          />
        );
      })}
    </Source>
  );
};
