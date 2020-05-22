import React from "react";

import { boolean, text, select } from "@storybook/addon-knobs";
import { action } from "@storybook/addon-actions";

import { ConstantDialog } from "./ConstantDialog";

export default {
  title: "UI|Dialogs",
  component: ConstantDialog,
};

const options = [
  { label: "Foo", value: "foo" },
  { label: "Bar", value: "bar" },
  { label: "Baz", value: "baz" },
];

const valueTypeOptions = options.map((o) => o.value);

export const propertyDialog = () => (
  <ConstantDialog
    title={text("Title", "Constant dialog title")}
    isOpen={boolean("Is open", true)}
    onCancel={action("onCancel")}
    onConfirm={action("onConfirm")}
    value={text("Initial name", "")}
    valueType={select(
      "Initial valueType",
      valueTypeOptions,
      valueTypeOptions[0],
    )}
    valueTypeOptions={options}
  />
);
