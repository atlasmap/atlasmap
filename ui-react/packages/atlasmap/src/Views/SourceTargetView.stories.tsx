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
import { SourceTargetView, IAtlasmapMapping } from "../Views";

export default {
  title: "Atlasmap|Views",
  decorators,
};

export const sourceTargetView = () =>
  createElement(() => {
    const [selectedMappingId, setSelectedMappingId] = useState<
      string | undefined
    >(undefined);
    const onSelectMapping = (m: IAtlasmapMapping) => setSelectedMappingId(m.id);
    const onDeselectMapping = () => setSelectedMappingId(undefined);
    return (
      <CanvasProvider>
        <SourceTargetView
          sourceEvents={{
            onCreateConstant: action("onCreateConstant"),
            onEditConstant: action("onEditConstant"),
            onDeleteConstant: action("onDeleteConstant"),
            onCreateProperty: action("onCreateProperty"),
            onEditProperty: action("onEditProperty"),
            onDeleteProperty: action("onDeleteProperty"),
            onDeleteDocument: action("onDeleteDocument"),
            onImportDocument: action("onImportDocument"),
            onEnableJavaClasses: action("onEnableJavaClasses"),
            onSearch: action("onSearch"),
            onDrop: action("onDrop"),
            canDrop: () => true,
            onAddToSelectedMapping: action("onAddToSelectedMapping"),
            onShowMappingDetails: action("onShowMappingDetails"),
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
            onEnableJavaClasses: action("onEnableJavaClasses"),
            onSearch: action("onSearch"),
            onDrop: action("onDrop"),
            canDrop: () => true,
            onAddToSelectedMapping: action("onAddToSelectedMapping"),
            onShowMappingDetails: action("onShowMappingDetails"),
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
          showTypes={boolean("Show types", false)}
          showMappingPreview={boolean("Show mapping preview", false)}
          properties={properties}
          constants={constants}
          sources={sources}
          mappings={mappings}
          targets={targets}
          selectedMappingId={selectedMappingId}
          onSelectMapping={onSelectMapping}
          onDeselectMapping={onDeselectMapping}
        />
      </CanvasProvider>
    );
  });
