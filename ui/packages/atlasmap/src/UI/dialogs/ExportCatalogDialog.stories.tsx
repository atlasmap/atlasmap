import React from "react";

import { boolean } from "@storybook/addon-knobs";
import { action } from "@storybook/addon-actions";

import { ExportCatalogDialog } from "./ExportCatalogDialog";

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
