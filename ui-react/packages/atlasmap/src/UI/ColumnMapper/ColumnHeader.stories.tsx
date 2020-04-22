import React from "react";

import { ColumnHeader } from ".";

export default {
  title: "ColumnMapper",
  component: ColumnHeader,
  includeStories: [], // or don't load this file at all
};

export const example = () => (
  <div style={{ width: 300, minHeight: 300 }}>
    <ColumnHeader
      title={"Source"}
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
