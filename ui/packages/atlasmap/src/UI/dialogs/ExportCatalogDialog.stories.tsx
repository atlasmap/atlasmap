import { ExportCatalogDialog } from "./ExportCatalogDialog";
import React from "react";
import { action } from "@storybook/addon-actions";
import { boolean } from "@storybook/addon-knobs";

export default {
  title: "UI|Dialogs",
  component: ExportCatalogDialog,
};

export const exportCatalogDialog = () => (
  <ExportCatalogDialog
    isOpen={boolean("Is open", true)}
    onCancel={action("onCancel")}
    onConfirm={action("onConfirm")}
  />
);
