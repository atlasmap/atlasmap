import React, { createElement, useState } from "react";

import { action } from "@storybook/addon-actions";
import { boolean } from "@storybook/addon-knobs";

import decorators from "../stories/decorators";
import {
  constants,
  mappings,
  properties,
  sources,
  targets,
} from "../stories/sampleData";
import { CanvasProvider } from "../UI";
import { IAtlasmapMapping, SourceMappingTargetView } from "../Views";

export default {
  title: "AtlasMap|Views",
  decorators,
};

export const sourceMappingTargetView = () =>
  createElement(() => {
    const [selectedMappingId, setSelectedMappingId] = useState<
      string | undefined
    >(undefined);
    const onSelectMapping = (m: IAtlasmapMapping) => setSelectedMappingId(m.id);
    return (
      <CanvasProvider>
        <SourceMappingTargetView
          sourceEvents={{
            onCreateConstant: action("onCreateConstant"),
            onEditConstant: action("onEditConstant"),
            onDeleteConstant: action("onDeleteConstant"),
            onCreateProperty: action("onCreateProperty"),
            onEditProperty: action("onEditProperty"),
            onDeleteProperty: action("onDeleteProperty"),
            onDeleteDocument: action("onDeleteDocument"),
            onImportDocument: action("onImportDocument"),
            onCustomClassSearch: action("onCustomClassSearch"),
            onSearch: action("onSearch"),
            onDrop: action("onDrop"),
            canDrop: () => true,
            onShowMappingDetails: action("onShowMappingDetails"),
            onAddToSelectedMapping: action("onAddToSelectedMapping"),
            canAddToSelectedMapping: (item) =>
              !!selectedMappingId &&
              !item.mappings.find((m) => m.id === selectedMappingId),
            onRemoveFromSelectedMapping: action("onRemoveFromSelectedMapping"),
            canRemoveFromSelectedMapping: (item) =>
              !!selectedMappingId &&
              !!item.mappings.find((m) => m.id === selectedMappingId),
            canStartMapping: () => true,
            onStartMapping: action("onStartMapping"),
            shouldShowMappingPreviewForField: () => true,
            onFieldPreviewChange: action("onFieldPreviewChange"),
          }}
          targetEvents={{
            onDeleteDocument: action("onDeleteDocument"),
            onImportDocument: action("onImportDocument"),
            onCustomClassSearch: action("onCustomClassSearch"),
            onSearch: action("onSearch"),
            onDrop: action("onDrop"),
            canDrop: () => true,
            onShowMappingDetails: action("onShowMappingDetails"),
            onAddToSelectedMapping: action("onAddToSelectedMapping"),
            canAddToSelectedMapping: (item) =>
              !!selectedMappingId &&
              !item.mappings.find((m) => m.id === selectedMappingId),
            onRemoveFromSelectedMapping: action("onRemoveFromSelectedMapping"),
            canRemoveFromSelectedMapping: (item) =>
              !!selectedMappingId &&
              !!item.mappings.find((m) => m.id === selectedMappingId),
            canStartMapping: () => true,
            onStartMapping: action("onStartMapping"),
            shouldShowMappingPreviewForField: () => true,
          }}
          mappingEvents={{
            canDrop: () => true,
            onEditMapping: action("onEditMapping"),
            onFieldPreviewChange: action("onFieldPreviewChange"),
            onMouseOut: action("onMouseOut"),
            onMouseOver: action("onMouseOver"),
            onSelectMapping: action("onSelectMapping"),
            onDeselectMapping: action("onDeselectMapping"),
            onAddMapping: action("onAddMapping"),
          }}
          showTypes={boolean("Show types", false)}
          showMappingPreview={boolean("Show mapping preview", false)}
          properties={properties}
          constants={constants}
          sources={sources}
          mappings={mappings}
          targets={targets}
          selectedMappingId={selectedMappingId}
          onSelectMapping={onSelectMapping}
        />
      </CanvasProvider>
    );
  });
