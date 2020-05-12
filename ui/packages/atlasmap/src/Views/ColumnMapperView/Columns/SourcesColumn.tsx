import React, { FunctionComponent, useCallback } from "react";

import { Button, Tooltip } from "@patternfly/react-core";
import { PlusIcon } from "@patternfly/react-icons";

import {
  ColumnBody,
  Document,
  DocumentFooter,
  NodeRef,
  SearchableColumnHeader,
  Tree,
  DocumentFieldPreview,
  IDragAndDropField,
} from "../../../UI";
import {
  GroupId,
  IAtlasmapDocument,
  IAtlasmapField,
  IAtlasmapMapping,
} from "../../models";
import {
  DeleteDocumentAction,
  EnableJavaClassAction,
  ImportAction,
} from "../Actions";
import { commonActions } from "./commonActions";
import {
  SOURCES_CONSTANTS_ID,
  SOURCES_DOCUMENT_ID_PREFIX,
  SOURCES_DRAGGABLE_TYPE,
  SOURCES_FIELD_ID_PREFIX,
  SOURCES_HEIGHT_BOUNDARY_ID,
  SOURCES_PROPERTIES_ID,
  SOURCES_WIDTH_BOUNDARY_ID,
  TARGETS_DRAGGABLE_TYPE,
} from "./constants";
import { ConstantsTree, IConstantsTreeCallbacks } from "./ConstantsTree";
import { IPropertiesTreeCallbacks, PropertiesTree } from "./PropertiesTree";
import { TraverseFields } from "./TraverseFields";

export interface ISourceColumnCallbacks
  extends IConstantsTreeCallbacks,
    IPropertiesTreeCallbacks {
  onCreateConstant: () => void;
  onCreateProperty: () => void;
  onCustomClassSearch?: (isSource: boolean) => void;
  onImportDocument?: (selectedFile: File) => void;
  onDeleteDocument?: (id: GroupId) => void;
  onSearch: (content: string) => void;
  canDrop: (source: IAtlasmapField, target: IDragAndDropField) => boolean;
  onDrop: (source: IAtlasmapField, target: IDragAndDropField) => void;
  onShowMappingDetails: (mapping: IAtlasmapMapping) => void;
  canAddToSelectedMapping: (source: IAtlasmapField) => boolean;
  onAddToSelectedMapping: (source: IAtlasmapField) => void;
  canRemoveFromSelectedMapping: (source: IAtlasmapField) => boolean;
  canStartMapping: (field: IAtlasmapField) => boolean;
  onStartMapping: (field: IAtlasmapField) => void;
  onRemoveFromSelectedMapping: (source: IAtlasmapField) => void;
  shouldShowMappingPreviewForField: (field: IAtlasmapField) => boolean;
  onFieldPreviewChange: (field: IAtlasmapField, value: string) => void;
}

export interface ISourcesColumnData {
  properties?: IAtlasmapDocument | null;
  constants?: IAtlasmapDocument | null;
  sources: Array<IAtlasmapDocument>;
  showTypes: boolean;
}

export const SourcesColumn: FunctionComponent<
  ISourcesColumnData & ISourceColumnCallbacks
> = ({
  onCreateConstant,
  onEditConstant,
  onDeleteConstant,
  onCreateProperty,
  onCustomClassSearch,
  onEditProperty,
  onDeleteProperty,
  onImportDocument,
  onDeleteDocument,
  onSearch,
  canDrop,
  onDrop,
  onShowMappingDetails,
  canAddToSelectedMapping,
  onAddToSelectedMapping,
  canRemoveFromSelectedMapping,
  onRemoveFromSelectedMapping,
  canStartMapping,
  onStartMapping,
  shouldShowMappingPreviewForField,
  onFieldPreviewChange,
  properties,
  constants,
  sources,
  showTypes,
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
        title={"Source"}
        onSearch={onSearch}
        actions={[
          onImportDocument && (
            <ImportAction
              id="Source"
              onImport={onImportDocument}
              key={"import"}
            />
          ),
          onCustomClassSearch && (
            <EnableJavaClassAction
              onCustomClassSearch={() => onCustomClassSearch(true)}
              key={"java"}
            />
          ),
        ]}
      />
      <NodeRef id={SOURCES_HEIGHT_BOUNDARY_ID}>
        <ColumnBody>
          <NodeRef id={SOURCES_WIDTH_BOUNDARY_ID}>
            <div>
              <NodeRef
                id={SOURCES_PROPERTIES_ID}
                boundaryId={SOURCES_HEIGHT_BOUNDARY_ID}
                overrideWidth={SOURCES_WIDTH_BOUNDARY_ID}
              >
                <Document
                  title={"Properties"}
                  actions={[
                    <Tooltip
                      position={"top"}
                      enableFlip={true}
                      content={<div>Create a property for use in mapping</div>}
                      key={"create-property"}
                    >
                      <Button
                        onClick={onCreateProperty}
                        variant={"plain"}
                        aria-label="Create a property for use in mapping"
                        data-testid="create-property-button"
                      >
                        <PlusIcon />
                      </Button>
                    </Tooltip>,
                  ]}
                  noPadding={!!properties}
                >
                  {properties ? (
                    <PropertiesTree
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
                      fields={properties.fields}
                      showTypes={showTypes}
                      renderPreview={renderPreview}
                    />
                  ) : (
                    "No properties"
                  )}
                </Document>
              </NodeRef>
              <NodeRef
                id={SOURCES_CONSTANTS_ID}
                boundaryId={SOURCES_HEIGHT_BOUNDARY_ID}
                overrideWidth={SOURCES_WIDTH_BOUNDARY_ID}
              >
                <Document
                  title={"Constants"}
                  actions={[
                    <Tooltip
                      position={"top"}
                      enableFlip={true}
                      content={<div>Create a constant for use in mapping</div>}
                      key={"create-constant"}
                    >
                      <Button
                        onClick={onCreateConstant}
                        variant={"plain"}
                        aria-label="Create a constant for use in mapping"
                        data-testid="create-constant-button"
                      >
                        <PlusIcon />
                      </Button>
                    </Tooltip>,
                  ]}
                  noPadding={!!constants}
                >
                  {constants ? (
                    <ConstantsTree
                      onEditConstant={onEditConstant}
                      onDeleteConstant={onDeleteConstant}
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
                      fields={constants.fields}
                      renderPreview={renderPreview}
                    />
                  ) : (
                    <p>No constants</p>
                  )}
                </Document>
              </NodeRef>
              {sources.map((s) => {
                const documentId = `${SOURCES_DOCUMENT_ID_PREFIX}${s.id}`;
                return (
                  <NodeRef
                    key={s.id}
                    id={documentId}
                    boundaryId={SOURCES_HEIGHT_BOUNDARY_ID}
                    overrideWidth={SOURCES_WIDTH_BOUNDARY_ID}
                  >
                    <Document
                      title={s.name}
                      footer={
                        showTypes ? (
                          <DocumentFooter>Source type: {s.type}</DocumentFooter>
                        ) : undefined
                      }
                      actions={[
                        onDeleteDocument && (
                          <DeleteDocumentAction
                            id={documentId}
                            onClick={() => onDeleteDocument(s.id)}
                            key={"delete-document"}
                          />
                        ),
                      ]}
                      noPadding={true}
                    >
                      <Tree>
                        <TraverseFields
                          fields={s.fields}
                          showTypes={showTypes}
                          boundaryId={SOURCES_HEIGHT_BOUNDARY_ID}
                          overrideWidth={SOURCES_WIDTH_BOUNDARY_ID}
                          parentId={documentId}
                          idPrefix={SOURCES_FIELD_ID_PREFIX}
                          acceptDropType={TARGETS_DRAGGABLE_TYPE}
                          draggableType={SOURCES_DRAGGABLE_TYPE}
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
                          renderPreview={renderPreview}
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
