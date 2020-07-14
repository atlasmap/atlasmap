import React from "react";

import { action } from "@storybook/addon-actions";
import { boolean } from "@storybook/addon-knobs";

import decorators from "../../stories/decorators";
import {
  constants,
  mappings,
  properties,
  sources,
  targets,
} from "../../stories/sampleData";
import { CanvasProvider, Column } from "../../UI";
import { MappingsColumn, SourcesColumn, TargetsColumn } from "../../Views";

export default {
  title: "AtlasMap|Views/ColumnMapperView",
  decorators,
};

export const sourcesColumn = () => (
  <CanvasProvider>
    <Column data-testid={"column-source-area"} totalColumns={1}>
      <SourcesColumn
        onCreateConstant={action("onCreateConstant")}
        onEditConstant={action("onEditConstant")}
        onDeleteConstant={action("onDeleteConstant")}
        onCreateProperty={action("onCreateProperty")}
        onEditProperty={action("onEditProperty")}
        onDeleteProperty={action("onDeleteProperty")}
        onDeleteDocument={action("onDeleteDocument")}
        onImportDocument={action("onImportDocument")}
        onCustomClassSearch={action("onCustomClassSearch")}
        onShowMappingDetails={action("onShowMappingDetails")}
        canAddToSelectedMapping={() => true}
        onAddToSelectedMapping={action("onAddToSelectedMapping")}
        canRemoveFromSelectedMapping={() => true}
        onRemoveFromSelectedMapping={action("onRemoveFromSelectedMapping")}
        canStartMapping={() => true}
        onStartMapping={action("onStartMapping")}
        shouldShowMappingPreviewForField={() => true}
        onFieldPreviewChange={action("onFieldPreviewChange")}
        canDrop={() => true}
        onDrop={action("onDrop")}
        onSearch={action("onSearch")}
        showTypes={boolean("Show types", true)}
        properties={properties}
        constants={constants}
        sources={sources}
      />
    </Column>
  </CanvasProvider>
);

export const targetsColumn = () => (
  <CanvasProvider>
    <Column data-testid={"column-target-area"} totalColumns={1}>
      <TargetsColumn
        onDeleteDocument={action("onDeleteDocument")}
        onImportDocument={action("onImportDocument")}
        onSearch={action("onSearch")}
        onCustomClassSearch={action("onCustomClassSearch")}
        onDrop={action("onDrop")}
        canDrop={() => true}
        onShowMappingDetails={action("onShowMappingDetails")}
        canAddToSelectedMapping={() => true}
        onAddToSelectedMapping={action("onAddToSelectedMapping")}
        canRemoveFromSelectedMapping={() => true}
        onRemoveFromSelectedMapping={action("onRemoveFromSelectedMapping")}
        canStartMapping={() => true}
        onStartMapping={action("onStartMapping")}
        shouldShowMappingPreviewForField={() => true}
        showMappingPreview={boolean("Show mapping preview", false)}
        showTypes={boolean("Show types", true)}
        targets={targets}
      />
    </Column>
  </CanvasProvider>
);

export const mappingsColumn = () => (
  <CanvasProvider>
    <Column data-testid={"column-mappings-area"} totalColumns={1}>
      <MappingsColumn
        showMappingPreview={boolean("Show mapping preview", true)}
        onFieldPreviewChange={action("onFieldPreviewChange")}
        onSelectMapping={action("onSelectMapping")}
        onDeselectMapping={action("onDeselectMapping")}
        onEditMapping={action("onEditMapping")}
        onMouseOver={action("onMouseOver")}
        onMouseOut={action("onMouseOut")}
        canDrop={() => true}
        onAddMapping={action("onAddMapping")}
        mappings={mappings}
      />
    </Column>
  </CanvasProvider>
);
