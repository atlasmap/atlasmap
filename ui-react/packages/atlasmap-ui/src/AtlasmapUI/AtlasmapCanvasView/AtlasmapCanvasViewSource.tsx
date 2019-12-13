import React, { FunctionComponent } from 'react';
import {
  FieldsBoxHeader,
  Source,
  Document,
  GroupId,
  ElementId,
} from '../../CanvasView';
import { DocumentField } from './DocumentField';
import { DocumentFooter } from './DocumentFooter';
import { DocumentFieldPreview } from './DocumentFieldPreview';
import { IAtlasmapDocument, IAtlasmapField, IAtlasmapGroup } from '../models';
import { useAtlasmapUI } from '../AtlasmapUIProvider';

export interface IAtlasmapCanvasViewSourceProps {
  onAddToMapping: (elementId: ElementId, mappingId: string) => void;
  onDeleteDocument: (id: GroupId) => void;
  onFieldPreviewChange: (field: IAtlasmapField, value: string) => void;
  onImportDocument: (selectedFile: File) => void;
  onSearch: (content: string) => void;
  sources: Array<IAtlasmapDocument>;
  showMappingPreview: boolean;
  showTypes: boolean;
}

export const AtlasmapCanvasViewSource: FunctionComponent<
  IAtlasmapCanvasViewSourceProps
> = ({
  onAddToMapping,
  onDeleteDocument,
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
    selectedMapping,
  } = useAtlasmapUI();
  return (
    <Source
      header={
        <FieldsBoxHeader
          title={'Source'}
          onSearch={onSearch}
          onImport={onImportDocument}
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
            renderGroup={node => (node as IAtlasmapGroup).name}
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
                  showAddToMapping={isFieldAddableToSelection(
                    currentMapping,
                    'source',
                    id
                  )}
                  onAddToMapping={() =>
                    selectedMapping && onAddToMapping(id, selectedMapping)
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
          />
        );
      })}
    </Source>
  );
};
