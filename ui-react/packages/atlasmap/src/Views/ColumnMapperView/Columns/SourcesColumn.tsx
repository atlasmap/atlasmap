import React, { FunctionComponent } from "react";

import { Button, Tooltip } from "@patternfly/react-core";
import { EditIcon, PlusIcon, TrashIcon } from "@patternfly/react-icons";

import {
  ColumnBody,
  Document,
  DocumentFooter,
  GroupId,
  IDragAndDropField,
  NodeRef,
  SearchableColumnHeader,
} from "../../../UI";
import {
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
import { TraverseFields } from "./TraverseFields";

export interface ISourceColumnEvents {
  onCreateConstant: () => void;
  onEditConstant: (value: string) => void;
  onDeleteConstant: (value: string) => void;
  onCreateProperty: () => void;
  onEditProperty: (name: string) => void;
  onDeleteProperty: (name: string) => void;
  onDeleteDocument: (id: GroupId) => void;
  onImportDocument: (selectedFile: File) => void;
  onEnableJavaClasses: () => void;
  onSearch: (content: string) => void;
  canDrop: (source: IAtlasmapField, target: IDragAndDropField) => boolean;
  onDrop: (source: IAtlasmapField, target: IDragAndDropField) => void;
  onShowMappingDetails: (mapping: IAtlasmapMapping) => void;
  canAddToSelectedMapping: (source: IAtlasmapField) => boolean;
  onAddToSelectedMapping: (source: IAtlasmapField) => void;
  canRemoveFromSelectedMapping: (source: IAtlasmapField) => boolean;
  onRemoveFromSelectedMapping: (source: IAtlasmapField) => void;
}

export interface ISourcesColumnData {
  properties?: IAtlasmapDocument | null;
  constants?: IAtlasmapDocument | null;
  sources: Array<IAtlasmapDocument>;
  showTypes: boolean;
}

export const SourcesColumn: FunctionComponent<
  ISourcesColumnData & ISourceColumnEvents
> = ({
  onCreateConstant,
  onEditConstant,
  onDeleteConstant,
  onCreateProperty,
  onEditProperty,
  onDeleteProperty,
  onImportDocument,
  onDeleteDocument,
  onEnableJavaClasses,
  onSearch,
  canDrop,
  onDrop,
  onShowMappingDetails,
  canAddToSelectedMapping,
  onAddToSelectedMapping,
  canRemoveFromSelectedMapping,
  onRemoveFromSelectedMapping,
  properties,
  constants,
  sources,
  showTypes,
}) => {
  return (
    <>
      <SearchableColumnHeader
        title={"Source"}
        onSearch={onSearch}
        actions={[
          <ImportAction onImport={onImportDocument} key={"import"} />,
          <EnableJavaClassAction onClick={onEnableJavaClasses} key={"java"} />,
        ]}
      />
      <NodeRef id={SOURCES_HEIGHT_BOUNDARY_ID}>
        <ColumnBody>
          <NodeRef id={SOURCES_WIDTH_BOUNDARY_ID}>
            <div>
              {properties && (
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
                        content={
                          <div>Create a constant for use in mapping</div>
                        }
                        key={"create-constant"}
                      >
                        <Button
                          onClick={onCreateConstant}
                          variant={"plain"}
                          aria-label="Create a constant for use in mapping"
                        >
                          <PlusIcon />
                        </Button>
                      </Tooltip>,
                    ]}
                  >
                    <TraverseFields
                      fields={properties.fields}
                      showTypes={showTypes}
                      parentId={SOURCES_PROPERTIES_ID}
                      boundaryId={SOURCES_HEIGHT_BOUNDARY_ID}
                      overrideWidth={SOURCES_WIDTH_BOUNDARY_ID}
                      idPrefix={SOURCES_FIELD_ID_PREFIX}
                      acceptDropType={TARGETS_DRAGGABLE_TYPE}
                      draggableType={SOURCES_DRAGGABLE_TYPE}
                      onDrop={onDrop}
                      canDrop={canDrop}
                      renderActions={(field) => [
                        ...commonActions({
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
                          onStartMapping: () => void 0,
                        }),
                        <Tooltip
                          key={"edit"}
                          position={"top"}
                          enableFlip={true}
                          content={<div>Edit property</div>}
                        >
                          <Button
                            variant="plain"
                            onClick={() => onEditProperty(field.name)}
                            aria-label={"Edit property"}
                            tabIndex={0}
                          >
                            <EditIcon />
                          </Button>
                        </Tooltip>,
                        <Tooltip
                          key={"delete"}
                          position={"top"}
                          enableFlip={true}
                          content={<div>Remove property</div>}
                        >
                          <Button
                            variant="plain"
                            onClick={() => onDeleteProperty(field.name)}
                            aria-label={"Remove property"}
                            tabIndex={0}
                          >
                            <TrashIcon />
                          </Button>
                        </Tooltip>,
                      ]}
                    />
                  </Document>
                </NodeRef>
              )}
              {constants && (
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
                        content={
                          <div>Create a constant for use in mapping</div>
                        }
                        key={"create-constant"}
                      >
                        <Button
                          onClick={onCreateProperty}
                          variant={"plain"}
                          aria-label="Create a constant for use in mapping"
                        >
                          <PlusIcon />
                        </Button>
                      </Tooltip>,
                    ]}
                  >
                    <TraverseFields
                      fields={constants.fields}
                      showTypes={false}
                      parentId={SOURCES_CONSTANTS_ID}
                      boundaryId={SOURCES_HEIGHT_BOUNDARY_ID}
                      overrideWidth={SOURCES_WIDTH_BOUNDARY_ID}
                      idPrefix={SOURCES_FIELD_ID_PREFIX}
                      acceptDropType={TARGETS_DRAGGABLE_TYPE}
                      draggableType={SOURCES_DRAGGABLE_TYPE}
                      onDrop={onDrop}
                      canDrop={canDrop}
                      renderActions={(field) => [
                        ...commonActions({
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
                          onStartMapping: () => void 0,
                        }),
                        <Tooltip
                          key={"edit"}
                          position={"top"}
                          enableFlip={true}
                          content={<div>Edit constant</div>}
                        >
                          <Button
                            variant="plain"
                            onClick={() => onEditConstant(field.name)}
                            aria-label={"Edit constant"}
                            tabIndex={0}
                          >
                            <EditIcon />
                          </Button>
                        </Tooltip>,
                        <Tooltip
                          key={"delete"}
                          position={"top"}
                          enableFlip={true}
                          content={<div>Remove constant</div>}
                        >
                          <Button
                            variant="plain"
                            onClick={() => onDeleteConstant(field.name)}
                            aria-label={"Remove constant"}
                            tabIndex={0}
                          >
                            <TrashIcon />
                          </Button>
                        </Tooltip>,
                      ]}
                    />
                  </Document>
                </NodeRef>
              )}
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
                        <DeleteDocumentAction
                          onClick={() => onDeleteDocument(s.id)}
                          key={"delete-document"}
                        />,
                      ]}
                    >
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
                            onStartMapping: () => void 0,
                          })
                        }
                      />
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
