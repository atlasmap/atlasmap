import React, { FunctionComponent, useState } from "react";

import { Column, ColumnMapper, Columns, NodeRefProvider } from "../UI";
import {
  ISourceColumnCallbacks,
  ISourceMappingTargetLinksEvents,
  ISourcesColumnData,
  ITargetsColumnData,
  ITargetsColumnCallbacks,
  SourcesColumn,
  TargetsColumn,
  ISourceMappingTargetLinksData,
  IMappingsColumnData,
  IMappingDocumentEvents,
} from "./ColumnMapperView";
import { IAtlasmapMapping } from "./models";
import { MappingsColumn } from "./ColumnMapperView/Columns/MappingsColumn.story";
import { SourceMappingTargetLinks } from "./ColumnMapperView/Links/SourceMappingTargetLinks.story";

export interface ISourceMappingTargetViewProps
  extends ISourcesColumnData,
    IMappingsColumnData,
    ITargetsColumnData,
    ISourceMappingTargetLinksData {
  onSelectMapping: ISourceMappingTargetLinksEvents["onSelectMapping"];
  sourceEvents: ISourceColumnCallbacks;
  mappingEvents: IMappingDocumentEvents;
  targetEvents: ITargetsColumnCallbacks;
  onMappingNameChange: (mapping: IAtlasmapMapping, name: string) => void;
  onRemoveMapping: (mapping: IAtlasmapMapping) => void;
  showAllLinks: boolean;
}

export const SourceMappingTargetViewStory: FunctionComponent<ISourceMappingTargetViewProps> = ({
  properties,
  constants,
  sources,
  mappings,
  targets,
  showMappingPreview,
  showTypes,
  showAllLinks,
  selectedMappingId,
  onSelectMapping,
  sourceEvents,
  mappingEvents,
  targetEvents,
  onMappingNameChange,
  onRemoveMapping,
}) => {
  const [highlightedMappingId, setHighlightedMappingId] = useState<
    string | undefined
  >();
  const handleMouseOver = (m: IAtlasmapMapping) =>
    setHighlightedMappingId(m.id);
  const handleMouseOut = () => setHighlightedMappingId(undefined);

  return (
    <ColumnMapper role={"main"}>
      <NodeRefProvider>
        <Columns>
          <Column data-testid={"column-source-area"} totalColumns={3}>
            <SourcesColumn
              properties={properties}
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
              {...mappingEvents}
              onMappingNameChange={onMappingNameChange}
              onRemoveMapping={onRemoveMapping}
            />
          </Column>
          <Column data-testid={"column-target-area"} totalColumns={3}>
            <TargetsColumn
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
          showAllLinks={showAllLinks}
        />
      </NodeRefProvider>
    </ColumnMapper>
  );
};
