import React from "react";
import decorators from "../stories/decorators";
import { mappings } from "../stories/sampleData";
import { MappingTableView } from "../Views";

export default {
  title: "AtlasMap|Views",
  decorators,
};

export const mappingTableView = () => <MappingTableView mappings={mappings} />;

export const mappingTableViewNoMappings = () => (
  <MappingTableView mappings={[]} />
);
