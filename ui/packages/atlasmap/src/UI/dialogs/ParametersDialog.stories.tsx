import React from "react";

import { boolean, text } from "@storybook/addon-knobs";
import { action } from "@storybook/addon-actions";

import { ParametersDialog } from "./ParametersDialog";

export default {
  title: "UI|Dialogs",
  component: ParametersDialog,
};

const parameters = [
  {
    name: "format",
    value: "Default",
    options: [
      { label: "Default", value: "Default" },
      { label: "Excel", value: "Excel" },
      { label: "InformixUnload", value: "InformixUnload" },
      { label: "InformixUnloadCsv", value: "InformixUnloadCsv" },
      { label: "MongoDBCsv", value: "MongoDBCsv" },
      { label: "MongoDBTsv", value: "MongoDBTsv" },
      { label: "MySQL", value: "MySQL" },
      { label: "Oracle", value: "Oracle" },
      { label: "PostgreSQLCsv", value: "PostgreSQLCsv" },
      { label: "PostgreSQLText", value: "PostgreSQLText" },
      { label: "RFC4180", value: "RFC4180" },
      { label: "TDF", value: "TDF" },
    ],
    required: true,
  },
  {
    name: "allowDuplicateHeaderNames",
    value: "true",
    options: [
      { label: "true", value: "true" },
      { label: "false", value: "false" },
    ],
    required: false,
  },
  {
    name: "allowMissingColumnNames",
    value: "true",
    options: [
      { label: "true", value: "true" },
      { label: "false", value: "false" },
    ],
    required: false,
  },
  { name: "commentMarker", value: "", required: false },
  { name: "delimiter", value: "", required: false },
  { name: "escape", value: "", required: false },
  { name: "escape", value: "", required: false },
  {
    name: "firstRecordAsHeader",
    value: "true",
    options: [
      { label: "true", value: "true" },
      { label: "false", value: "false" },
    ],
    required: false,
  },
  { name: "headers", value: "", required: false },
  {
    name: "ignoreEmptyLines",
    value: "true",
    options: [
      { label: "true", value: "true" },
      { label: "false", value: "false" },
    ],
    required: false,
  },
  {
    name: "ignoreHeaderCase",
    value: "true",
    options: [
      { label: "true", value: "true" },
      { label: "false", value: "false" },
    ],
    required: false,
  },
  {
    name: "ignoreSurroundingSpaces",
    value: "true",
    options: [
      { label: "true", value: "true" },
      { label: "false", value: "false" },
    ],
    required: false,
  },
  { name: "nullString", value: "", required: false },
  { name: "quote", value: "", required: false },
];

export const parametersDialog = () => (
  <ParametersDialog
    title={text("Title", "Parameters dialog title")}
    isOpen={boolean("Is open", true)}
    onCancel={action("onCancel")}
    onConfirm={action("onConfirm")}
    parameters={parameters}
  />
);
