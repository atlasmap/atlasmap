import React from "react";

import { action } from "@storybook/addon-actions";

import { SearchableColumnHeader } from ".";

export default {
  title: "ColumnMapper",
  component: SearchableColumnHeader,
  includeStories: [], // or don't load this file at all
};

export const example = () => (
  <div style={{ width: 400, minHeight: 300 }}>
    <SearchableColumnHeader
      title={"Source"}
      onSearch={action("onSearch")}
      actions={[
        <div key={"1"}>
          <button>#1</button>
        </div>,
        <div key={"2"}>
          <button>#2</button>
        </div>,
        <div key={"1"}>
          <button>#1</button>
        </div>,
      ]}
    />
  </div>
);
