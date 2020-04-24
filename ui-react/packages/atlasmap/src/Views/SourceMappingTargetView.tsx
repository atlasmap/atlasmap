import React, { FunctionComponent, useState } from "react";

import { Column, ColumnMapper, Columns, NodeRefProvider } from "../UI";
import {
  IMappingDocumentEvents,
  IMappingsColumnData,
  ISourceColumnEvents,
  ISourceMappingTargetLinksData,
  ISourceMappingTargetLinksEvents,
  ISourcesColumnData,
  ITargetsColumnData,
  ITargetsColumnEvents,
  MappingsColumn,
  SourceMappingTargetLinks,
  SourcesColumn,
  TargetsColumn,
} from "./ColumnMapperView";
import { IAtlasmapMapping } from "./models";

export interface ISourceMappingTargetViewProps
  extends ISourcesColumnData,
    IMappingsColumnData,
    ITargetsColumnData,
    ISourceMappingTargetLinksData {
  onSelectMapping: ISourceMappingTargetLinksEvents["onSelectMapping"];
  onDeselectMapping: () => void;
  sourceEvents: ISourceColumnEvents;
  mappingEvents: IMappingDocumentEvents;
  targetEvents: ITargetsColumnEvents;
}

export const SourceMappingTargetView: FunctionComponent<ISourceMappingTargetViewProps> = ({
  properties,
  constants,
  sources,
  mappings,
  targets,
  showMappingPreview,
  showTypes,
  selectedMappingId,
  onSelectMapping,
  onDeselectMapping,
  sourceEvents,
  mappingEvents,
  targetEvents,
}) => {
  const [highlightedMappingId, setHighlightedMappingId] = useState<
    string | undefined
  >();
  const handleMouseOver = (m: IAtlasmapMapping) =>
    setHighlightedMappingId(m.id);
  const handleMouseOut = () => setHighlightedMappingId(undefined);

  return (
    <ColumnMapper onClick={onDeselectMapping} role={"main"}>
      <NodeRefProvider>
        <Columns>
          <Column totalColumns={3}>
            <SourcesColumn
              properties={properties}
              constants={constants}
              sources={sources}
              showTypes={showTypes}
              {...sourceEvents}
            />
          </Column>
          <Column totalColumns={3}>
            <MappingsColumn
              mappings={mappings}
              selectedMappingId={selectedMappingId}
              showMappingPreview={showMappingPreview}
              onMouseOver={handleMouseOver}
              onMouseOut={handleMouseOut}
              {...mappingEvents}
            />
          </Column>
          <Column totalColumns={3}>
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
        />
      </NodeRefProvider>
    </ColumnMapper>
  );
};
