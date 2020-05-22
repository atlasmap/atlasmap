import React from "react";

import { boolean, text, select } from "@storybook/addon-knobs";
import { action } from "@storybook/addon-actions";

import { CustomClassDialog } from "./CustomClassDialog";

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
    value={text("Initial name", "")}
    collectionType={select(
      "Initial collectionType",
      collectionTypeOptions,
      collectionTypeOptions[0],
    )}
    collectionTypeOptions={options}
  />
);
