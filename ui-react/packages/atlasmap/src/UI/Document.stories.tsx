import React from "react";

import { Button } from "@patternfly/react-core";
import { text } from "@storybook/addon-knobs";

import { Document } from "./Document";

export default {
  title: "Document",
  includeStories: [], // or don't load this file at all
};

export const document = () => (
  <div style={{ width: 300 }}>
    <Document
      title={text("Title", "Some title that can be extra long")}
      footer={text("footer", "Source document")}
      actions={[<Button key={"1"}>some action</Button>]}
    >
      Lorem ipsum dolor sit amet, consectetur adipisicing elit. Accusantium
      assumenda atque consequuntur cupiditate doloremque eius eligendi et, ex,
      harum impedit ipsum magnam minus, officia provident qui quidem veniam.
      Facere, quo.
    </Document>
  </div>
);

export const states = () => (
  <div>
    <Document
      style={{ width: 250, marginRight: 10, display: "inline-block" }}
      title={"Selected"}
      selected={true}
    />
    <Document
      style={{ width: 250, marginRight: 10, display: "inline-block" }}
      title={"DropTarget"}
      dropTarget={true}
    />
    <Document
      style={{ width: 250, marginRight: 10, display: "inline-block" }}
      title={"DropAccepted"}
      dropAccepted={true}
    />
  </div>
);

export const noActions = () => (
  <div style={{ width: 300 }}>
    <Document title={"Lorem dolor"} footer={"Lorem dolor"}>
      Lorem ipsum dolor sit amet, consectetur adipisicing elit. Accusantium
      assumenda atque consequuntur cupiditate doloremque eius eligendi et, ex,
      harum impedit ipsum magnam minus, officia provident qui quidem veniam.
      Facere, quo.
    </Document>
  </div>
);

export const stacked = () => (
  <div style={{ width: 300 }}>
    <Document title={"Lorem dolor"} footer={"Lorem dolor"} stacked={true} />
    <Document title={"Lorem dolor"} footer={"Lorem dolor"} stacked={true} />
    <Document title={"Lorem dolor"} footer={"Lorem dolor"} stacked={true} />
  </div>
);
