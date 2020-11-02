import React, { FunctionComponent, useCallback } from "react";

import {
  ColumnBody,
  Document,
  DocumentFooter,
  IDragAndDropField,
  NodeRef,
  SearchableColumnHeader,
  Tree,
  DocumentFieldPreviewResults,
  DocumentFieldPreview,
} from "../../../UI";
import {
  IAtlasmapDocument,
  IAtlasmapField,
  IAtlasmapMapping,
  GroupId,
  AtlasmapDocumentType,
} from "../../models";
import {
  CaptureDocumentNameAction,
  ChangeDocumentNameAction,
  DeleteDocumentAction,
  EnableJavaClassAction,
  ImportAction,
} from "../Actions";
import { commonActions } from "./commonActions";
import {
  TARGETS_DOCUMENT_ID_PREFIX,
  TARGETS_FIELD_ID_PREFIX,
  TARGETS_HEIGHT_BOUNDARY_ID,
  TARGETS_PROPERTIES_ID,
  TARGETS_WIDTH_BOUNDARY_ID,
} from "./constants";
import { TraverseFields } from "./TraverseFields";
import { Tooltip, Button } from "@patternfly/react-core";
import { PlusIcon } from "@patternfly/react-icons";
import { IPropertiesTreeCallbacks, PropertiesTree } from "./PropertiesTree";

export interface ITargetsColumnCallbacks extends IPropertiesTreeCallbacks {
  acceptDropType: AtlasmapDocumentType;
  draggableType: AtlasmapDocumentType;
  isSource: boolean;
  onCreateProperty: (isSource: boolean) => void;
  onCaptureDocumentName?: (id: string) => void;
  onChangeDocumentName?: (id: string, name: string) => void;
  onDeleteDocument?: (id: GroupId) => void;
  onImportDocument?: (selectedFile: File) => void;
  onCustomClassSearch?: (isSource: boolean) => void;
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
  onFieldPreviewChange: (field: IAtlasmapField, value: string) => void;
}

export interface ITargetsColumnData {
  targetProperties?: IAtlasmapDocument | null;
  showMappingPreview: boolean;
  showTypes: boolean;
  targets: Array<IAtlasmapDocument>;
}

export const TargetsColumn: FunctionComponent<
  ITargetsColumnData & ITargetsColumnCallbacks
> = ({
  acceptDropType,
  draggableType,
  isSource,
  onSearch,
  onImportDocument,
  onDeleteDocument,
  onCaptureDocumentName,
  onChangeDocumentName,
  onCustomClassSearch,
  onCreateProperty,
  onEditProperty,
  onDeleteProperty,
  onDrop,
  canDrop,
  onShowMappingDetails,
  canAddToSelectedMapping,
  onAddToSelectedMapping,
  canRemoveFromSelectedMapping,
  onRemoveFromSelectedMapping,
  canStartMapping,
  onStartMapping,
  onFieldPreviewChange,
  shouldShowMappingPreviewForField,
  targets,
  showTypes,
  targetProperties,
}) => {
  const renderPreview = useCallback(
    (field: IAtlasmapField) =>
      shouldShowMappingPreviewForField(field) && (
        <DocumentFieldPreview
          id={field.id}
          value={field.previewValue}
          onChange={(value) => onFieldPreviewChange(field, value)}
        />
      ),
    [onFieldPreviewChange, shouldShowMappingPreviewForField],
  );
  return (
    <>
      <SearchableColumnHeader
        title={"Target"}
        onSearch={onSearch}
        actions={[
          onImportDocument && (
            <ImportAction
              id="Target"
              onImport={onImportDocument}
              key={"import"}
            />
          ),
          onCustomClassSearch && (
            <EnableJavaClassAction
              onCustomClassSearch={() => onCustomClassSearch(false)}
              data-testid={"enable-specific-java-classes-Target-button"}
              key={"java"}
            />
          ),
        ]}
      />
      <NodeRef id={TARGETS_HEIGHT_BOUNDARY_ID}>
        <ColumnBody>
          <NodeRef id={TARGETS_WIDTH_BOUNDARY_ID}>
            <div>
              <NodeRef
                id={TARGETS_PROPERTIES_ID}
                boundaryId={TARGETS_HEIGHT_BOUNDARY_ID}
                overrideWidth={TARGETS_WIDTH_BOUNDARY_ID}
              >
                <Document
                  title={"Properties"}
                  actions={[
                    <Tooltip
                      position={"top"}
                      enableFlip={true}
                      content={
                        <div>Create a target property for use in mapping</div>
                      }
                      key={"create-target-property"}
                      entryDelay={750}
                      exitDelay={100}
                    >
                      <Button
                        onClick={() => onCreateProperty(isSource)}
                        variant={"plain"}
                        aria-label="Create a target property for use in mapping"
                        data-testid="create-target-property-button"
                      >
                        <PlusIcon />
                      </Button>
                    </Tooltip>,
                  ]}
                  noPadding={!!targetProperties}
                >
                  {targetProperties ? (
                    <PropertiesTree
                      acceptDropType={acceptDropType}
                      draggableType={draggableType}
                      isSource={isSource}
                      onEditProperty={onEditProperty}
                      onDeleteProperty={onDeleteProperty}
                      canDrop={canDrop}
                      onDrop={onDrop}
                      onShowMappingDetails={onShowMappingDetails}
                      canAddToSelectedMapping={canAddToSelectedMapping}
                      onAddToSelectedMapping={onAddToSelectedMapping}
                      canRemoveFromSelectedMapping={
                        canRemoveFromSelectedMapping
                      }
                      onRemoveFromSelectedMapping={onRemoveFromSelectedMapping}
                      canStartMapping={canStartMapping}
                      onStartMapping={onStartMapping}
                      fields={targetProperties.fields}
                      showTypes={showTypes}
                      renderPreview={renderPreview}
                    />
                  ) : (
                    "No target properties"
                  )}
                </Document>
              </NodeRef>
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
                          <DocumentFooter>
                            Target document type: {t.type}
                          </DocumentFooter>
                        ) : undefined
                      }
                      actions={[
                        onCaptureDocumentName && (
                          <CaptureDocumentNameAction
                            id={documentId}
                            onClick={() => onCaptureDocumentName(t.id)}
                            key={"capture-document-name"}
                          />
                        ),
                        onChangeDocumentName && (
                          <ChangeDocumentNameAction
                            id={documentId}
                            onClick={() => onChangeDocumentName(t.id, t.name)}
                            key={"change-document-name"}
                          />
                        ),
                        onDeleteDocument && (
                          <DeleteDocumentAction
                            id={documentId}
                            onClick={() => onDeleteDocument(t.id)}
                            key={"delete-documents"}
                          />
                        ),
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
                          acceptDropType={acceptDropType}
                          draggableType={draggableType}
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
