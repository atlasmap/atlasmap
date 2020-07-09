import React, { FunctionComponent, useState } from "react";
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
import { IAtlasmapMapping } from "./models";

export interface ISourceMappingTargetXformViewProps
  extends ISourcesColumnData,
    IMappingsColumnData,
    ITargetsColumnData,
    ISourceMappingTargetLinksData {
  onSelectMapping: ISourceMappingTargetLinksEvents["onSelectMapping"];
  sourceEvents: ISourceColumnCallbacks;
  mappingEvents: IMappingDocumentEvents;
  targetEvents: ITargetsColumnCallbacks;
  showAllLinks: boolean;
}

export const SourceMappingTargetXformView: FunctionComponent<ISourceMappingTargetXformViewProps> = ({
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
              {...mappingEvents}
              onMouseOver={handleMouseOver}
              onMouseOut={handleMouseOut}
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
