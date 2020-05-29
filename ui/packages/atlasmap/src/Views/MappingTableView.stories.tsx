import React from "react";
import decorators from "../stories/decorators";
import { mappings } from "../stories/sampleData";
import { MappingTableView } from "../Views";
import { action } from "@storybook/addon-actions";

export default {
  title: "AtlasMap|Views",
  decorators,
};

export const mappingTableView = () => (
  <MappingTableView
    mappings={mappings}
    onSelectMapping={action("onSelectMapping")}
  />
);

export const mappingTableViewNoMappings = () => (
  <MappingTableView mappings={[]} onSelectMapping={action("onSelectMapping")} />
);
