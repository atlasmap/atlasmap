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
import { Column, ColumnMapper, Columns, NodeRefProvider } from "../UI";
import {
  IMappingDocumentEvents,
  IMappingsColumnData,
  ISourceColumnCallbacks,
  ISourceMappingTargetLinksData,
  ISourceMappingTargetLinksEvents,
  ISourcesColumnData,
  ITargetsColumnCallbacks,
  ITargetsColumnData,
  MappingsColumn,
  SourceMappingTargetLinks,
  SourcesColumn,
  TargetsColumn,
} from "./ColumnMapperView";
import React, { FunctionComponent, useState } from "react";

import { IAtlasmapMapping } from "./models";

export interface ISourceMappingTargetViewProps
  extends ISourcesColumnData,
    IMappingsColumnData,
    ITargetsColumnData,
    ISourceMappingTargetLinksData {
  onSelectMapping: ISourceMappingTargetLinksEvents["onSelectMapping"];
  sourceEvents: ISourceColumnCallbacks;
  mappingEvents: IMappingDocumentEvents;
  targetEvents: ITargetsColumnCallbacks;
}

export const SourceMappingTargetView: FunctionComponent<ISourceMappingTargetViewProps> =
  ({
    sourceProperties,
    targetProperties,
    constants,
    sources,
    mappings,
    targets,
    showMappingPreview,
    showTypes,
    selectedMappingId,
    onSelectMapping,
    sourceEvents,
    mappingEvents,
    targetEvents,
  }) => {
    const [highlightedMappingId, setHighlightedMappingId] =
      useState<string | undefined>();
    const handleMouseOver = (m: IAtlasmapMapping) =>
      setHighlightedMappingId(m.id);
    const handleMouseOut = () => setHighlightedMappingId(undefined);

    return (
      <ColumnMapper role={"main"}>
        <NodeRefProvider>
          <Columns>
            <Column data-testid={"column-source-area"} totalColumns={3}>
              <SourcesColumn
                sourceProperties={sourceProperties}
                constants={constants}
                sources={sources}
                showTypes={showTypes}
                {...sourceEvents}
              />
            </Column>
            <Column data-testid={"column-mappings-area"} totalColumns={3}>
              <MappingsColumn
                mappings={mappings}
                selectedMappingId={selectedMappingId}
                showMappingPreview={showMappingPreview}
                onMouseOver={handleMouseOver}
                onMouseOut={handleMouseOut}
                onSelectMapping={mappingEvents.onSelectMapping}
                onDeselectMapping={mappingEvents.onDeselectMapping}
                onEditMapping={mappingEvents.onEditMapping}
                onFieldPreviewChange={mappingEvents.onFieldPreviewChange}
                canDrop={mappingEvents.canDrop}
              />
            </Column>
            <Column data-testid={"column-target-area"} totalColumns={3}>
              <TargetsColumn
                targetProperties={targetProperties}
                showMappingPreview={showMappingPreview}
                showTypes={showTypes}
                targets={targets}
                {...targetEvents}
              />
            </Column>
          </Columns>
          <SourceMappingTargetLinks
            mappings={mappings}
            selectedMappingId={selectedMappingId}
            highlightedMappingId={highlightedMappingId}
            onSelectMapping={onSelectMapping}
            onMouseOver={handleMouseOver}
            onMouseOut={handleMouseOut}
          />
        </NodeRefProvider>
      </ColumnMapper>
    );
  };
