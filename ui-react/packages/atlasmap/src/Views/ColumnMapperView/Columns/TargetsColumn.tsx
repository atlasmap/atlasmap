import React, { FunctionComponent } from "react";

import {
  ColumnBody,
  Document,
  DocumentFooter,
  IDragAndDropField,
  NodeRef,
  SearchableColumnHeader,
  Tree,
  DocumentFieldPreviewResults,
} from "../../../UI";
import {
  IAtlasmapDocument,
  IAtlasmapField,
  IAtlasmapMapping,
  GroupId,
} from "../../models";
import {
  DeleteDocumentAction,
  EnableJavaClassAction,
  ImportAction,
} from "../Actions";
import { commonActions } from "./commonActions";
import {
  SOURCES_DRAGGABLE_TYPE,
  TARGETS_DOCUMENT_ID_PREFIX,
  TARGETS_DRAGGABLE_TYPE,
  TARGETS_FIELD_ID_PREFIX,
  TARGETS_HEIGHT_BOUNDARY_ID,
  TARGETS_WIDTH_BOUNDARY_ID,
} from "./constants";
import { TraverseFields } from "./TraverseFields";

export interface ITargetsColumnCallbacks {
  onDeleteDocument: (id: GroupId) => void;
  onImportDocument: (selectedFile: File) => void;
  onEnableJavaClasses: () => void;
  onSearch: (content: string) => void;
  onDrop: (source: IAtlasmapField, target: IDragAndDropField) => void;
  canDrop: (source: IAtlasmapField, target: IDragAndDropField) => boolean;
  onShowMappingDetails: (mapping: IAtlasmapMapping) => void;
  canAddToSelectedMapping: (field: IAtlasmapField) => boolean;
  onAddToSelectedMapping: (field: IAtlasmapField) => void;
  canRemoveFromSelectedMapping: (field: IAtlasmapField) => boolean;
  onRemoveFromSelectedMapping: (field: IAtlasmapField) => void;
  canStartMapping: (field: IAtlasmapField) => boolean;
  onStartMapping: (field: IAtlasmapField) => void;
  shouldShowMappingPreviewForField: (field: IAtlasmapField) => boolean;
}

export interface ITargetsColumnData {
  showMappingPreview: boolean;
  showTypes: boolean;
  targets: Array<IAtlasmapDocument>;
}

export const TargetsColumn: FunctionComponent<
  ITargetsColumnData & ITargetsColumnCallbacks
> = ({
  onSearch,
  onImportDocument,
  onDeleteDocument,
  onEnableJavaClasses,
  onDrop,
  canDrop,
  onShowMappingDetails,
  canAddToSelectedMapping,
  onAddToSelectedMapping,
  canRemoveFromSelectedMapping,
  onRemoveFromSelectedMapping,
  canStartMapping,
  onStartMapping,
  shouldShowMappingPreviewForField,
  targets,
  showTypes,
}) => {
  return (
    <>
      <SearchableColumnHeader
        title={"Target"}
        onSearch={onSearch}
        actions={[
          <ImportAction onImport={onImportDocument} key={"import"} />,
          <EnableJavaClassAction onClick={onEnableJavaClasses} key={"java"} />,
        ]}
      />
      <NodeRef id={TARGETS_HEIGHT_BOUNDARY_ID}>
        <ColumnBody>
          <NodeRef id={TARGETS_WIDTH_BOUNDARY_ID}>
            <div>
              {targets.map((t) => {
                const documentId = `${TARGETS_DOCUMENT_ID_PREFIX}${t.id}`;
                return (
                  <NodeRef
                    key={t.id}
                    id={documentId}
                    boundaryId={TARGETS_HEIGHT_BOUNDARY_ID}
                    overrideWidth={TARGETS_WIDTH_BOUNDARY_ID}
                  >
                    <Document
                      title={t.name}
                      footer={
                        showTypes ? (
                          <DocumentFooter>Target type: {t.type}</DocumentFooter>
                        ) : undefined
                      }
                      actions={[
                        <DeleteDocumentAction
                          onClick={() => onDeleteDocument(t.id)}
                          key={"delete-documents"}
                        />,
                      ]}
                      noPadding={true}
                    >
                      <Tree>
                        <TraverseFields
                          fields={t.fields}
                          showTypes={showTypes}
                          boundaryId={TARGETS_HEIGHT_BOUNDARY_ID}
                          overrideWidth={TARGETS_WIDTH_BOUNDARY_ID}
                          parentId={documentId}
                          idPrefix={TARGETS_FIELD_ID_PREFIX}
                          acceptDropType={SOURCES_DRAGGABLE_TYPE}
                          draggableType={TARGETS_DRAGGABLE_TYPE}
                          onDrop={onDrop}
                          canDrop={canDrop}
                          renderActions={(field) =>
                            commonActions({
                              connectedMappings: field.mappings,
                              onShowMappingDetails,
                              canAddToSelectedMapping: canAddToSelectedMapping(
                                field,
                              ),
                              onAddToSelectedMapping: () =>
                                onAddToSelectedMapping(field),
                              canRemoveFromSelectedMapping: canRemoveFromSelectedMapping(
                                field,
                              ),
                              onRemoveFromSelectedMapping: () =>
                                onRemoveFromSelectedMapping(field),
                              canStartMapping: canStartMapping(field),
                              onStartMapping: () => onStartMapping(field),
                            })
                          }
                          renderPreview={(field) =>
                            shouldShowMappingPreviewForField(field) && (
                              <DocumentFieldPreviewResults
                                id={field.id}
                                value={field.previewValue}
                              />
                            )
                          }
                        />
                      </Tree>
                    </Document>
                  </NodeRef>
                );
              })}
            </div>
          </NodeRef>
        </ColumnBody>
      </NodeRef>
    </>
  );
};
