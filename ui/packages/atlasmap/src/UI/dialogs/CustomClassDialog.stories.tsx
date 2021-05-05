import { boolean, select, text } from "@storybook/addon-knobs";

import { CustomClassDialog } from "./CustomClassDialog";
import React from "react";
import { action } from "@storybook/addon-actions";

export default {
  title: "UI|Dialogs",
  component: CustomClassDialog,
};

const options = [
  { label: "Foo", value: "foo" },
  { label: "Bar", value: "bar" },
  { label: "Baz", value: "baz" },
];

const collectionTypeOptions = options.map((o) => o.value);

export const customClassDialog = () => (
  <CustomClassDialog
    title={text("Title", "CustomClass dialog title")}
    isOpen={boolean("Is open", true)}
    onCancel={action("onCancel")}
    onConfirm={action("onConfirm")}
    customClassName={text("Initial name", "")}
    customClassNames={["className1", "className2", "className3"]}
    collectionType={select(
      "Initial collectionType",
      collectionTypeOptions,
      collectionTypeOptions[0],
    )}
    collectionTypeOptions={options}
  />
);
