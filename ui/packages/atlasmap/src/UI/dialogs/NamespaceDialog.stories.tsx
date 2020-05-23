import React from "react";

import { boolean, text } from "@storybook/addon-knobs";
import { action } from "@storybook/addon-actions";

import { NamespaceDialog } from "./NamespaceDialog";

export default {
  title: "UI|Dialogs",
  component: NamespaceDialog,
};

export const namespaceDialog = () => (
  <NamespaceDialog
    title={text("Title", "Namespace dialog title")}
    isOpen={boolean("Is open", true)}
    onCancel={action("onCancel")}
    onConfirm={action("onConfirm")}
    alias={text("Initial alias", "")}
    uri={text("Initial uri", "")}
    locationUri={text("Initial locationUri", "")}
    targetNamespace={boolean("Initial targetNamespace", false)}
  />
);
