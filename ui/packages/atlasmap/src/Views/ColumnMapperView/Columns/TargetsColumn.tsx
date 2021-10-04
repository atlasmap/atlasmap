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
  AtlasmapDocumentType,
  GroupId,
  IAtlasmapDocument,
  IAtlasmapField,
  IAtlasmapMapping,
} from '../../models';
import { Button, Tooltip } from '@patternfly/react-core';
import {
  CaptureDocumentIDAction,
  ChangeDocumentNameAction,
  DeleteDocumentAction,
  EditCSVParamsAction,
  EnableJavaClassAction,
  ImportAction,
} from '../Actions';
import {
  ColumnBody,
  Document,
  DocumentFieldPreviewResults,
  DocumentFooter,
  IDragAndDropField,
  NodeRef,
  SearchableColumnHeader,
  Tree,
} from '../../../UI';
import { IPropertiesTreeCallbacks, PropertiesTree } from './PropertiesTree';
import React, { FunctionComponent, useCallback } from 'react';
import {
  TARGETS_DOCUMENT_ID_PREFIX,
  TARGETS_FIELD_ID_PREFIX,
  TARGETS_HEIGHT_BOUNDARY_ID,
  TARGETS_PROPERTIES_ID,
  TARGETS_WIDTH_BOUNDARY_ID,
} from './constants';

import { DocumentType } from '@atlasmap/core';
import { PlusIcon } from '@patternfly/react-icons';
import { TraverseFields } from './TraverseFields';
import { commonActions } from './commonActions';

export interface ITargetsColumnCallbacks extends IPropertiesTreeCallbacks {
  acceptDropType: AtlasmapDocumentType;
  draggableType: AtlasmapDocumentType;
  isSource: boolean;
  onCreateProperty: (isSource: boolean) => void;
  onCaptureDocumentID?: (id: string) => void;
  onChangeDocumentName?: (id: string, name: string) => void;
  onDeleteDocument?: (id: GroupId) => void;
  onImportDocument?: (selectedFile: File) => void;
  onCustomClassSearch?: (isSource: boolean) => void;
  onSearch: (content: string) => void;
  onDrop: (source: IAtlasmapField, target: IDragAndDropField | null) => void;
  canDrop: (source: IAtlasmapField, target: IDragAndDropField) => boolean;
  onShowMappingDetails: (mapping: IAtlasmapMapping) => void;
  canAddFieldToSelectedMapping: (field: IAtlasmapField) => boolean;
  onAddToSelectedMapping: (field: IAtlasmapField) => void;
  canRemoveFromSelectedMapping: (field: IAtlasmapField) => boolean;
  onRemoveFromSelectedMapping: (field: IAtlasmapField) => void;
  canStartMapping: (field: IAtlasmapField) => boolean;
  onStartMapping: (field: IAtlasmapField) => void;
  shouldShowMappingPreviewForField: (field: IAtlasmapField) => boolean;
  onFieldPreviewChange: (field: IAtlasmapField, value: string) => void;
  canAddToSelectedMapping: (isSource: boolean) => boolean;
  onEditCSVParams: (id: string, isSource: boolean) => void;
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
  onCaptureDocumentID,
  onChangeDocumentName,
  onCustomClassSearch,
  onCreateProperty,
  onEditCSVParams,
  onEditProperty,
  onDeleteProperty,
  onDrop,
  canDrop,
  onShowMappingDetails,
  canAddFieldToSelectedMapping,
  onAddToSelectedMapping,
  canRemoveFromSelectedMapping,
  onRemoveFromSelectedMapping,
  canStartMapping,
  onStartMapping,
  shouldShowMappingPreviewForField,
  targets,
  showTypes,
  targetProperties,
}) => {
  const renderPreviewResult = useCallback(
    (field: IAtlasmapField) =>
      shouldShowMappingPreviewForField(field) && (
        <DocumentFieldPreviewResults id={field.id} value={field.value} />
      ),
    [shouldShowMappingPreviewForField],
  );
  return (
    <>
      <SearchableColumnHeader
        title={'Target'}
        onSearch={onSearch}
        actions={[
          onImportDocument && (
            <ImportAction
              id="Target"
              onImport={onImportDocument}
              key={'import'}
            />
          ),
          onCustomClassSearch && (
            <EnableJavaClassAction
              onCustomClassSearch={() => onCustomClassSearch(false)}
              data-testid={'enable-specific-java-classes-Target-button'}
              key={'java'}
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
                  title={'Properties'}
                  startExpanded={targetProperties ? true : false}
                  actions={[
                    <Tooltip
                      position={'top'}
                      enableFlip={true}
                      content={
                        <div>Create a target property for use in mapping</div>
                      }
                      key={'create-target-property'}
                      entryDelay={750}
                      exitDelay={100}
                    >
                      <Button
                        onClick={() => onCreateProperty(isSource)}
                        variant={'plain'}
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
                      canAddFieldToSelectedMapping={
                        canAddFieldToSelectedMapping
                      }
                      onAddToSelectedMapping={onAddToSelectedMapping}
                      canRemoveFromSelectedMapping={
                        canRemoveFromSelectedMapping
                      }
                      onRemoveFromSelectedMapping={onRemoveFromSelectedMapping}
                      canStartMapping={canStartMapping}
                      onStartMapping={onStartMapping}
                      fields={targetProperties.fields}
                      showTypes={showTypes}
                      renderPreview={renderPreviewResult}
                    />
                  ) : (
                    'No target properties'
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
                      startExpanded={true}
                      footer={
                        showTypes ? (
                          <DocumentFooter>
                            Target document type: {t.type}
                          </DocumentFooter>
                        ) : undefined
                      }
                      actions={
                        t.type === DocumentType.CSV
                          ? [
                              onCaptureDocumentID && (
                                <CaptureDocumentIDAction
                                  id={documentId}
                                  onClick={() => onCaptureDocumentID(t.id)}
                                  key={'capture-tgt-csv-document-id'}
                                />
                              ),
                              onEditCSVParams && (
                                <EditCSVParamsAction
                                  id={documentId}
                                  onClick={() => onEditCSVParams(t.id, false)}
                                  key={'on-edit-tgt-csv-params'}
                                />
                              ),
                              onDeleteDocument && (
                                <DeleteDocumentAction
                                  id={documentId}
                                  onClick={() => onDeleteDocument(t.id)}
                                  key={'delete-tgt-csv-document'}
                                />
                              ),
                            ]
                          : [
                              onCaptureDocumentID && (
                                <CaptureDocumentIDAction
                                  id={documentId}
                                  onClick={() => onCaptureDocumentID(t.id)}
                                  key={'capture-tgt-document-id'}
                                />
                              ),
                              onChangeDocumentName && (
                                <ChangeDocumentNameAction
                                  id={documentId}
                                  onClick={() =>
                                    onChangeDocumentName(t.id, t.name)
                                  }
                                  key={'change-tgt-document-name'}
                                />
                              ),
                              onDeleteDocument && (
                                <DeleteDocumentAction
                                  id={documentId}
                                  onClick={() => onDeleteDocument(t.id)}
                                  key={'delete-tgt-documents'}
                                />
                              ),
                            ]
                      }
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
                              canAddFieldToSelectedMapping:
                                canAddFieldToSelectedMapping(field),
                              onAddToSelectedMapping: () =>
                                onAddToSelectedMapping(field),
                              canRemoveFromSelectedMapping:
                                canRemoveFromSelectedMapping(field),
                              onRemoveFromSelectedMapping: () =>
                                onRemoveFromSelectedMapping(field),
                              canStartMapping: canStartMapping(field),
                              onStartMapping: () => onStartMapping(field),
                            })
                          }
                          renderPreview={renderPreviewResult}
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
