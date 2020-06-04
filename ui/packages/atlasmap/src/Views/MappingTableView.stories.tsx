import React from "react";
import decorators from "../stories/decorators";
import { mappings } from "../stories/sampleData";
import { MappingTableView } from "../Views";
import { action } from "@storybook/addon-actions";
import { boolean } from "@storybook/addon-knobs";

export default {
  title: "AtlasMap|Views",
  decorators,
};

export const mappingTableView = () => (
  <MappingTableView
    mappings={mappings}
    onSelectMapping={action("onSelectMapping")}
    shouldShowMappingPreview={() => boolean("shouldShowMappingPreview", false)}
    onFieldPreviewChange={action("onFieldPreviewChange")}
  />
);

export const mappingTableViewNoMappings = () => (
  <MappingTableView
    mappings={[]}
    onSelectMapping={action("onSelectMapping")}
    shouldShowMappingPreview={() => boolean("shouldShowMappingPreview", false)}
    onFieldPreviewChange={action("onFieldPreviewChange")}
  />
);
