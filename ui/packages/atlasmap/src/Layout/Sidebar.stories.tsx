import React from "react";

import { boolean } from "@storybook/addon-knobs";
import { Sidebar } from "./Sidebar";

export default {
  title: "AtlasMap|Layout/Sidebar",
};

export const example = () => (
  <div style={{ minHeight: 300 }}>
    <Sidebar show={boolean("Show sidebar", true)}>
      {() => (
        <div>
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Dignissimos
          incidunt, fugiat sequi obcaecati sapiente debitis fuga perspiciatis
          possimus minima recusandae dolor minus unde nesciunt in aspernatur
          accusantium laborum sit cumque?
        </div>
      )}
    </Sidebar>
  </div>
);
