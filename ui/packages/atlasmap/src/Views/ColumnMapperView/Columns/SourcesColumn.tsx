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
  DocumentFieldPreview,
  DocumentFooter,
  IDragAndDropField,
  NodeRef,
  SearchableColumnHeader,
  Tree,
} from '../../../UI';
import { ConstantsTree, IConstantsTreeCallbacks } from './ConstantsTree';
import { IPropertiesTreeCallbacks, PropertiesTree } from './PropertiesTree';
import React, { FunctionComponent, useCallback } from 'react';
import {
  SOURCES_CONSTANTS_ID,
  SOURCES_DOCUMENT_ID_PREFIX,
  SOURCES_FIELD_ID_PREFIX,
  SOURCES_HEIGHT_BOUNDARY_ID,
  SOURCES_PROPERTIES_ID,
  SOURCES_WIDTH_BOUNDARY_ID,
} from './constants';

import { DocumentType } from '@atlasmap/core';
import { PlusIcon } from '@patternfly/react-icons';
import { TraverseFields } from './TraverseFields';
import { commonActions } from './commonActions';

export interface ISourceColumnCallbacks
  extends IConstantsTreeCallbacks,
    IPropertiesTreeCallbacks {
  acceptDropType: AtlasmapDocumentType;
  draggableType: AtlasmapDocumentType;
  isSource: boolean;
  onCreateConstant: () => void;
  onCreateProperty: (isSource: boolean) => void;
  onCustomClassSearch?: (isSource: boolean) => void;
  onCaptureDocumentID?: (id: string) => void;
  onChangeDocumentName?: (id: string, name: string) => void;
  onImportDocument?: (selectedFile: File) => void;
  onDeleteDocument?: (id: GroupId) => void;
  onSearch: (content: string) => void;
  canDrop: (source: IAtlasmapField, target: IDragAndDropField) => boolean;
  onDrop: (source: IAtlasmapField, target: IDragAndDropField | null) => void;
  onShowMappingDetails: (mapping: IAtlasmapMapping) => void;
  canAddFieldToSelectedMapping: (source: IAtlasmapField) => boolean;
  onAddToSelectedMapping: (source: IAtlasmapField) => void;
  canRemoveFromSelectedMapping: (source: IAtlasmapField) => boolean;
  canStartMapping: (field: IAtlasmapField) => boolean;
  onStartMapping: (field: IAtlasmapField) => void;
  onRemoveFromSelectedMapping: (source: IAtlasmapField) => void;
  shouldShowMappingPreviewForField: (field: IAtlasmapField) => boolean;
  onFieldPreviewChange: (field: IAtlasmapField, value: string) => void;
  canAddToSelectedMapping: (isSource: boolean) => boolean;
  onEditCSVParams: (id: string, isSource: boolean) => void;
}

export interface ISourcesColumnData {
  sourceProperties?: IAtlasmapDocument | null;
  constants?: IAtlasmapDocument | null;
  sources: Array<IAtlasmapDocument>;
  showTypes: boolean;
}

export const SourcesColumn: FunctionComponent<
  ISourcesColumnData & ISourceColumnCallbacks
> = ({
  acceptDropType,
  draggableType,
  isSource,
  onCreateConstant,
  onEditConstant,
  onDeleteConstant,
  onCreateProperty,
  onCustomClassSearch,
  onEditProperty,
  onDeleteProperty,
  onImportDocument,
  onDeleteDocument,
  onCaptureDocumentID,
  onChangeDocumentName,
  onEditCSVParams,
  onSearch,
  canDrop,
  onDrop,
  onShowMappingDetails,
  canAddFieldToSelectedMapping,
  onAddToSelectedMapping,
  canRemoveFromSelectedMapping,
  onRemoveFromSelectedMapping,
  canStartMapping,
  onStartMapping,
  shouldShowMappingPreviewForField,
  onFieldPreviewChange,
  sourceProperties,
  constants,
  sources,
  showTypes,
}) => {
  const renderPreview = useCallback(
    (field: IAtlasmapField) =>
      shouldShowMappingPreviewForField(field) && (
        <DocumentFieldPreview
          id={field.id}
          value={field.value}
          onChange={(value) => onFieldPreviewChange(field, value)}
        />
      ),
    [onFieldPreviewChange, shouldShowMappingPreviewForField],
  );

  return (
    <>
      <SearchableColumnHeader
        title={'Source'}
        onSearch={onSearch}
        actions={[
          onImportDocument && (
            <ImportAction
              id="Source"
              onImport={onImportDocument}
              key={'import'}
            />
          ),
          onCustomClassSearch && (
            <EnableJavaClassAction
              onCustomClassSearch={() => onCustomClassSearch(true)}
              data-testid={'enable-specific-java-classes-Source-button'}
              key={'java'}
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
                  title={'Properties'}
                  startExpanded={sourceProperties ? true : false}
                  actions={[
                    <Tooltip
                      position={'top'}
                      enableFlip={true}
                      content={
                        <div>Create a source property for use in mapping</div>
                      }
                      key={'create-property'}
                      entryDelay={750}
                      exitDelay={100}
                    >
                      <Button
                        onClick={() => onCreateProperty(isSource)}
                        variant={'plain'}
                        aria-label="Create a source property for use in mapping"
                        data-testid="create-source-property-button"
                      >
                        <PlusIcon />
                      </Button>
                    </Tooltip>,
                  ]}
                  noPadding={!!sourceProperties}
                >
                  {sourceProperties ? (
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
                      fields={sourceProperties.fields}
                      showTypes={showTypes}
                      renderPreview={renderPreview}
                    />
                  ) : (
                    'No source properties'
                  )}
                </Document>
              </NodeRef>
              <NodeRef
                id={SOURCES_CONSTANTS_ID}
                boundaryId={SOURCES_HEIGHT_BOUNDARY_ID}
                overrideWidth={SOURCES_WIDTH_BOUNDARY_ID}
              >
                <Document
                  title={'Constants'}
                  startExpanded={constants ? true : false}
                  actions={[
                    <Tooltip
                      position={'top'}
                      enableFlip={true}
                      content={<div>Create a constant for use in mapping</div>}
                      key={'create-constant'}
                      entryDelay={750}
                      exitDelay={100}
                    >
                      <Button
                        onClick={onCreateConstant}
                        variant={'plain'}
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
                      startExpanded={true}
                      footer={
                        showTypes ? (
                          <DocumentFooter>
                            Source document type: {s.type}
                          </DocumentFooter>
                        ) : undefined
                      }
                      actions={
                        s.type === DocumentType.CSV
                          ? [
                              onCaptureDocumentID && (
                                <CaptureDocumentIDAction
                                  id={documentId}
                                  onClick={() => onCaptureDocumentID(s.id)}
                                  key={'capture-document-id'}
                                />
                              ),
                              onEditCSVParams && (
                                <EditCSVParamsAction
                                  id={documentId}
                                  onClick={() => onEditCSVParams(s.id, true)}
                                  key={'on-edit-csv-params'}
                                />
                              ),
                              onDeleteDocument && (
                                <DeleteDocumentAction
                                  id={documentId}
                                  onClick={() => onDeleteDocument(s.id)}
                                  key={'delete-document'}
                                />
                              ),
                            ]
                          : [
                              onCaptureDocumentID && (
                                <CaptureDocumentIDAction
                                  id={documentId}
                                  onClick={() => onCaptureDocumentID(s.id)}
                                  key={'capture-document-id'}
                                />
                              ),
                              onChangeDocumentName && (
                                <ChangeDocumentNameAction
                                  id={documentId}
                                  onClick={() =>
                                    onChangeDocumentName(s.id, s.name)
                                  }
                                  key={'change-document-name'}
                                />
                              ),
                              onDeleteDocument && (
                                <DeleteDocumentAction
                                  id={documentId}
                                  onClick={() => onDeleteDocument(s.id)}
                                  key={'delete-document'}
                                />
                              ),
                            ]
                      }
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
