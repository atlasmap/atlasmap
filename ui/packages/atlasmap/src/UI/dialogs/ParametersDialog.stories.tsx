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
    label: "CSV File Format",
    value: "Excel",
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
    enabled: true,
  },
  {
    name: "allowDuplicateHeaderNames",
    label: "Allow Duplicate Header Names",
    value: "true",
    boolean: true,
    required: false,
    enabled: true,
  },
  {
    name: "allowMissingColumnNames",
    label: "Allow Missing Column Names",
    value: "true",
    boolean: true,
    required: false,
    enabled: true,
  },
  {
    name: "commentMarker",
    label: "Comment Marker",
    value: "#",
    required: false,
    enabled: true,
  },
  {
    name: "delimiter",
    label: "Delimiter",
    value: ":",
    required: false,
    enabled: true,
  },
  {
    name: "escape",
    label: "Escape",
    value: "<esc>",
    required: false,
    enabled: true,
  },
  {
    name: "firstRecordAsHeader",
    label: "First Record As Header",
    value: "false",
    boolean: true,
    required: false,
    enabled: true,
  },
  { name: "headers", label: "Headers", value: "hdr", required: false },
  {
    name: "ignoreEmptyLines",
    label: "Ignore Empty Lines",
    value: "true",
    boolean: true,
    required: false,
    enabled: true,
  },
  {
    name: "ignoreHeaderCase",
    label: "Ignore Header Case",
    value: "false",
    boolean: true,
    required: false,
    enabled: true,
  },
  {
    name: "ignoreSurroundingSpaces",
    label: "Ignore Surrounding Spaces",
    value: "true",
    boolean: true,
    required: false,
    enabled: true,
  },
  {
    name: "nullString",
    label: "Null String",
    value: "<nul>",
    required: false,
    enabled: true,
  },
  { name: "quote", label: "Quote", value: "", required: false, enabled: false },
];

export const parametersDialog = () => (
  <ParametersDialog
    title={text("Title", "Select CSV Processing Parameters")}
    isOpen={boolean("Is open", true)}
    onCancel={action("onCancel")}
    onConfirm={action("onConfirm")}
    parameters={parameters}
  />
);
