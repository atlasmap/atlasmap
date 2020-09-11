import React from "react";

import { boolean, text, select } from "@storybook/addon-knobs";
import { action } from "@storybook/addon-actions";

import { PropertyDialog } from "./PropertyDialog";

export default {
  title: "UI|Dialogs",
  component: PropertyDialog,
};

const options = [
  { label: "Foo", value: "foo" },
  { label: "Bar", value: "bar" },
  { label: "Baz", value: "baz" },
];

const valueTypeOptions = options.map((o) => o.value);
const scopeOptions = options.map((o) => o.value);

export const sourcePropertyDialog = () => (
  <PropertyDialog
    title={text("Title", "Property dialog title")}
    isOpen={boolean("Is open", true)}
    onCancel={action("onCancel")}
    onConfirm={action("onConfirm")}
    name={text("Initial name", "")}
    valueType={select(
      "Initial valueType",
      valueTypeOptions,
      valueTypeOptions[0],
    )}
    valueTypeOptions={options}
    scope={select("Initial scope", scopeOptions, scopeOptions[0])}
    scopeOptions={options}
    onValidation={() => true}
  />
);

export const targetPropertyDialog = () => (
  <PropertyDialog
    title={text("Title", "Property dialog title")}
    isOpen={boolean("Is open", true)}
    onCancel={action("onCancel")}
    onConfirm={action("onConfirm")}
    name={text("Initial name", "")}
    valueType={select(
      "Initial valueType",
      valueTypeOptions,
      valueTypeOptions[0],
    )}
    valueTypeOptions={options}
    scope={select("Initial scope", scopeOptions, scopeOptions[0])}
    scopeOptions={options}
    onValidation={() => true}
  />
);
