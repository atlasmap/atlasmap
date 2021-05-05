import { MainLayout } from "./MainLayout";
import React from "react";
import { boolean } from "@storybook/addon-knobs";

export default {
  title: "AtlasMap|Layout/MainLayout",
};

export const example = () => (
  <MainLayout
    loading={boolean("loading", false)}
    contextToolbar={<div>context toolbar</div>}
    expressionToolbar={<div>view toolbar</div>}
    controlBar={<div>canvas bar</div>}
    showSidebar={boolean("showSidebar", true)}
    renderSidebar={() => <div>a sidebar</div>}
  >
    <div style={{ minHeight: 300 }}>
      Lorem ipsum dolor sit amet consectetur, adipisicing elit. Molestias
      tenetur veritatis dolore perferendis dicta excepturi illum necessitatibus
      eos accusantium ipsum. Aliquid a doloribus libero nemo et veniam quaerat
      nesciunt repudiandae!
    </div>
  </MainLayout>
);
