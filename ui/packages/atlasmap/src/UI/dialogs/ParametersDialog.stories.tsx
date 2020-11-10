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
    label: "Format",
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
    label: "Allow Duplicate Header Names",
    value: "true",
    boolean: true,
    required: false,
  },
  {
    name: "allowMissingColumnNames",
    label: "Allow Missing Column Names",
    value: "true",
    boolean: true,
    required: false,
  },
  {
    name: "commentMarker",
    label: "Comment Marker",
    value: "",
    required: false,
  },
  { name: "delimiter", label: "Delimiter", value: "", required: false },
  { name: "escape", label: "Escape", value: "", required: false },
  {
    name: "firstRecordAsHeader",
    label: "First Record As Header",
    value: "true",
    boolean: true,
    required: false,
  },
  { name: "headers", label: "Headers", value: "", required: false },
  {
    name: "ignoreEmptyLines",
    label: "Ignore Empty Lines",
    value: "true",
    boolean: true,
    required: false,
  },
  {
    name: "ignoreHeaderCase",
    label: "Ignore Header Case",
    value: "true",
    boolean: true,
    required: false,
  },
  {
    name: "ignoreSurroundingSpaces",
    label: "Ignore Surrounding Spaces",
    value: "true",
    boolean: true,
    required: false,
  },
  {
    name: "nullString",
    label: "Null String",
    value: "",
    required: false,
  },
  { name: "quote", label: "Quote", value: "", required: false },
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
