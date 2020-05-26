import React, { FunctionComponent } from "react";

import { Column, ColumnMapper, Columns, NodeRefProvider } from "../UI";
import {
  ISourcesColumnData,
  ITargetsColumnData,
  SourcesColumn,
  SourceTargetLinks,
  TargetsColumn,
  ISourceMappingTargetLinksData,
  ISourceMappingTargetLinksEvents,
  ISourceColumnCallbacks,
  ITargetsColumnCallbacks,
} from "./ColumnMapperView";

export interface ISourceTargetViewProps
  extends ISourcesColumnData,
    ITargetsColumnData,
    ISourceMappingTargetLinksData {
  onSelectMapping: ISourceMappingTargetLinksEvents["onSelectMapping"];
  sourceEvents: ISourceColumnCallbacks;
  targetEvents: ITargetsColumnCallbacks;
}

export const SourceTargetView: FunctionComponent<ISourceTargetViewProps> = ({
  properties,
  constants,
  sources,
  mappings,
  targets,
  showMappingPreview,
  showTypes,
  selectedMappingId,
  onSelectMapping,
  sourceEvents,
  targetEvents,
}) => {
  return (
    <ColumnMapper role={"main"}>
      <NodeRefProvider>
        <Columns>
          <Column data-testid={"column-source-area"} totalColumns={2}>
            <SourcesColumn
              properties={properties}
              constants={constants}
              sources={sources}
              showTypes={showTypes}
              {...sourceEvents}
            />
          </Column>

          <Column data-testid={"column-target-area"} totalColumns={2}>
            <TargetsColumn
              showMappingPreview={showMappingPreview}
              showTypes={showTypes}
              targets={targets}
              {...targetEvents}
            />
          </Column>
        </Columns>

        <SourceTargetLinks
          mappings={mappings}
          selectedMappingId={selectedMappingId}
          onSelectMapping={onSelectMapping}
        />
      </NodeRefProvider>
    </ColumnMapper>
  );
};
