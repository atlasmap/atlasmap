/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import {
  ColumnBody,
  ColumnHeader,
  Document,
  DocumentFieldPreview,
  DocumentFieldPreviewResults,
  DraggedField,
  FieldDropTarget,
  IDragAndDropField,
  NodeRef,
  TruncatedString,
} from "../../../UI";
import { IAtlasmapField, IAtlasmapMapping } from "../../models";
import {
  MAPPINGS_DOCUMENT_ID_PREFIX,
  MAPPINGS_DROP_TYPE,
  MAPPINGS_FIELD_ID_PREFIX,
  MAPPINGS_HEIGHT_BOUNDARY_ID,
  MAPPINGS_WIDTH_BOUNDARY_ID,
  SOURCES_DRAGGABLE_TYPE,
  TARGETS_DRAGGABLE_TYPE,
} from "./constants";
import React, { FunctionComponent, useCallback } from "react";
import { Split, SplitItem } from "@patternfly/react-core";

import { EditMappingAction } from "../Actions";

export interface IMappingsColumnData
  extends Omit<Omit<IMappingDocumentData, "mapping">, "isSelected"> {
  mappings: IAtlasmapMapping[];
  selectedMappingId?: string;
}

export const MappingsColumn: FunctionComponent<
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
  onEditMapping: (mapping: IAtlasmapMapping) => void;
  onFieldPreviewChange: (field: IAtlasmapField, value: string) => void;
  onMouseOver: (mapping: IAtlasmapMapping) => void;
  onMouseOut: () => void;
  canDrop: (target: IDragAndDropField, mapping: IAtlasmapMapping) => boolean;
}

export interface IMappingDocumentData {
  mapping: IAtlasmapMapping;
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
  onEditMapping,
  onFieldPreviewChange,
  onMouseOver,
  onMouseOut,
  canDrop,
}) => {
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
          <Document
            title={mapping.name}
            dropAccepted={isDroppable}
            dropTarget={isTarget}
            actions={[
              <EditMappingAction
                id={mapping.id}
                onClick={() => onEditMapping(mapping)}
                key="edit"
              />,
            ]}
            selected={isSelected}
            selectable={true}
            onSelect={handleSelect}
            onDeselect={handleDeselect}
            onMouseOver={() => onMouseOver(mapping)}
            onMouseOut={onMouseOut}
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
          </Document>
        </NodeRef>
      )}
    </FieldDropTarget>
  );
};
