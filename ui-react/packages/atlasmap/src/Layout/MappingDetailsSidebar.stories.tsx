import React from "react";

import { action } from "@storybook/addon-actions";

import { MappingDetailsSidebar } from "./MappingDetailsSidebar";

export default {
  title: "Atlasmap|Layout/MappingDetailsSidebar",
};

export const example = () => (
  <MappingDetailsSidebar
    onClose={action("onClose")}
    onDelete={action("onDelete")}
  >
    Lorem ipsum dolor sit amet consectetur adipisicing elit. Dignissimos
    incidunt, fugiat sequi obcaecati sapiente debitis fuga perspiciatis possimus
    minima recusandae dolor minus unde nesciunt in aspernatur accusantium
    laborum sit cumque?
  </MappingDetailsSidebar>
);
