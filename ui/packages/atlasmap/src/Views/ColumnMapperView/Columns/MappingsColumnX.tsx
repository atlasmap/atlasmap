import React, { FunctionComponent, useCallback, useState } from "react";

import { Split, SplitItem } from "@patternfly/react-core";

import {
  ColumnBody,
  ColumnHeader,
  Document,
  DocumentFieldPreview,
  DocumentFieldPreviewResults,
  DraggedField,
  FieldDropTarget,
  TruncatedString,
  IDragAndDropField,
  NodeRef,
} from "../../../UI";
import { IAtlasmapField, IAtlasmapMapping } from "../..";
import { EditMappingNameAction } from "../Actions/EditMappingNameAction";
import {
  MAPPINGS_DOCUMENT_ID_PREFIX,
  MAPPINGS_DROP_TYPE,
  MAPPINGS_FIELD_ID_PREFIX,
  MAPPINGS_HEIGHT_BOUNDARY_ID,
  MAPPINGS_WIDTH_BOUNDARY_ID,
  SOURCES_DRAGGABLE_TYPE,
  TARGETS_DRAGGABLE_TYPE,
} from "./constants";
import { DocumentX } from "../../../UI/DocumentX";
import { IMapping } from "../../../Views/modelsX";

export interface IMappingsColumnData
  extends Omit<Omit<IMappingDocumentData, "mapping">, "isSelected"> {
  mappings: IMapping[];
  selectedMappingId?: string;
}

export const MappingsColumnX: FunctionComponent<
  IMappingsColumnData & IMappingDocumentEvents
> = ({ mappings, selectedMappingId, ...props }) => {
  return (
    <>
      <ColumnHeader title={"Mappings"} />
      <NodeRef id={MAPPINGS_HEIGHT_BOUNDARY_ID}>
        <ColumnBody>
          <NodeRef id={MAPPINGS_WIDTH_BOUNDARY_ID}>
            <div>
              {mappings.map((m) => (
                <MappingDocument
                  key={m.id}
                  mapping={m}
                  isSelected={selectedMappingId === m.id}
                  {...props}
                />
              ))}
            </div>
          </NodeRef>
          <DraggedField>
            {({ draggedField, getHoveredTarget }) =>
              draggedField && getHoveredTarget()?.type !== "mapping" ? (
                <NodeRef
                  id={["dnd-new-mapping"]}
                  boundaryId={MAPPINGS_HEIGHT_BOUNDARY_ID}
                  overrideWidth={MAPPINGS_WIDTH_BOUNDARY_ID}
                >
                  <Document
                    title={"Create new mapping"}
                    dropTarget={true}
                    scrollIntoView={true}
                  />
                </NodeRef>
              ) : null
            }
          </DraggedField>
        </ColumnBody>
      </NodeRef>
    </>
  );
};

export interface IMappingDocumentEvents {
  onSelectMapping: (mapping: IAtlasmapMapping) => void;
  onDeselectMapping: (mapping: IAtlasmapMapping) => void;
  onFieldPreviewChange: (field: IAtlasmapField, value: string) => void;
  onMouseOver: (mapping: IAtlasmapMapping) => void;
  onMouseOut: () => void;
  canDrop: (target: IDragAndDropField, mapping: IAtlasmapMapping) => boolean;
  onMappingNameChange: (mapping: IAtlasmapMapping, name: string) => void;
}

export interface IMappingDocumentData {
  mapping: IMapping;
  isSelected: boolean;
  showMappingPreview: boolean;
}

export const MappingDocument: FunctionComponent<
  IMappingDocumentData & IMappingDocumentEvents
> = ({
  mapping,
  isSelected,
  showMappingPreview,
  onSelectMapping,
  onDeselectMapping,
  onFieldPreviewChange,
  onMouseOver,
  onMouseOut,
  canDrop,
  onMappingNameChange,
}) => {
  const [isEditingMappingName, setEditingMappingName] = useState(false);
  const [mappingName, setMappingName] = useState(mapping.name);

  const documentId = `${MAPPINGS_DOCUMENT_ID_PREFIX}${mapping.id}`;
  const handleSelect = useCallback(() => {
    if (!isSelected) {
      onSelectMapping(mapping);
    }
  }, [isSelected, mapping, onSelectMapping]);
  const handleDeselect = useCallback(() => {
    if (isSelected) {
      onDeselectMapping(mapping);
    }
  }, [isSelected, mapping, onDeselectMapping]);
  return (
    <FieldDropTarget
      target={{
        id: mapping.id,
        name: mapping.name,
        type: MAPPINGS_DROP_TYPE,
        payload: mapping,
      }}
      canDrop={(item) => canDrop(item, mapping)}
      accept={[SOURCES_DRAGGABLE_TYPE, TARGETS_DRAGGABLE_TYPE]}
    >
      {({ isDroppable, isTarget }) => (
        <NodeRef
          key={documentId}
          id={[documentId, isTarget ? "dnd-target-mapping" : undefined]}
          boundaryId={MAPPINGS_HEIGHT_BOUNDARY_ID}
          overrideWidth={MAPPINGS_WIDTH_BOUNDARY_ID}
        >
          <DocumentX
            title={mappingName}
            dropAccepted={isDroppable}
            dropTarget={isTarget}
            actions={[
              <EditMappingNameAction
                id={mapping.id}
                onClick={() => setEditingMappingName(true)}
                key="edit"
              />,
            ]}
            selected={isSelected}
            selectable={true}
            onSelect={handleSelect}
            onDeselect={handleDeselect}
            onMouseOver={() => onMouseOver(mapping)}
            onMouseOut={onMouseOut}
            startExpanded={false}
            isEditingTitle={isEditingMappingName}
            onTitleChange={(title: string) => {
              setMappingName(title);
            }}
            onStopEditingTitle={(cancel) => {
              if (cancel) {
                setMappingName(mapping.name);
              } else {
                const name = mappingName || mapping.defaultName;
                setMappingName(name);
                onMappingNameChange(mapping, name);
              }
              setEditingMappingName(false);
            }}
          >
            <Split>
              <SplitItem style={{ maxWidth: "50%", padding: "0 0 0 1rem" }}>
                {mapping.sourceFields.map((mf) => {
                  const fieldId = `${documentId}-${MAPPINGS_FIELD_ID_PREFIX}${mf.id}`;
                  return (
                    <NodeRef
                      key={fieldId}
                      id={fieldId}
                      parentId={documentId}
                      boundaryId={MAPPINGS_HEIGHT_BOUNDARY_ID}
                      overrideWidth={MAPPINGS_WIDTH_BOUNDARY_ID}
                    >
                      <div>
                        <TruncatedString>{mf.name}</TruncatedString>
                        {isSelected && showMappingPreview && (
                          <DocumentFieldPreview
                            id={mf.id}
                            value={mf.previewValue}
                            onChange={(value) =>
                              onFieldPreviewChange(mf, value)
                            }
                          />
                        )}
                      </div>
                    </NodeRef>
                  );
                })}
              </SplitItem>
              <SplitItem isFilled />
              <SplitItem
                style={{
                  maxWidth: "50%",
                  padding: "0 1rem 0 0",
                  textAlign: "right",
                }}
              >
                {mapping.targetFields.map((mf) => {
                  const fieldId = `${documentId}-${MAPPINGS_FIELD_ID_PREFIX}${mf.id}`;
                  return (
                    <NodeRef
                      key={fieldId}
                      id={fieldId}
                      parentId={documentId}
                      boundaryId={MAPPINGS_HEIGHT_BOUNDARY_ID}
                      overrideWidth={MAPPINGS_WIDTH_BOUNDARY_ID}
                    >
                      <div>
                        <TruncatedString>{mf.name}</TruncatedString>
                        {isSelected && showMappingPreview && (
                          <DocumentFieldPreviewResults
                            id={mf.id}
                            value={mf.previewValue}
                          />
                        )}
                      </div>
                    </NodeRef>
                  );
                })}
              </SplitItem>
            </Split>
          </DocumentX>
        </NodeRef>
      )}
    </FieldDropTarget>
  );
};
