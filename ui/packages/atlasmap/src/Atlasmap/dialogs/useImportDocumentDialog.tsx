import React from "react";
import { useCallback, ReactElement } from "react";

import { useAtlasmap } from "../AtlasmapProvider";
import { useConfirmationDialog } from "./useConfirmationDialog";
import { useParametersDialog } from "./useParametersDialog";

export function useImportDocumentDialog(): [
  ReactElement,
  (selectedFile: File, isSource: boolean) => void,
] {
  const { documentExists, importAtlasFile } = useAtlasmap();
  const [importDialog, openImportDialog] = useConfirmationDialog(
    "Confirm document import",
    "A document with the selected name has already been imported into the specified panel. It will appear in addition to the pre-existing document.",
  );
  const [parametersDialog, openParametersDialog] = useParametersDialog(
    "CSV processing parameters",
  );

  const importFile = useCallback(
    (selectedFile: File, isSource: boolean) => {
      if (selectedFile.name) {
        const userFileSplit = selectedFile.name.split(".");
        const userFileSuffix: string = userFileSplit[
          userFileSplit.length - 1
        ].toUpperCase();
        if (userFileSuffix === "CSV") {
          openParametersDialog(
            (parameters) => {
              const inspectionParameters: { [key: string]: string } = {};
              for (let parameter of parameters) {
                inspectionParameters[parameter.name] = parameter.value;
              }
              importAtlasFile(selectedFile, isSource, inspectionParameters);
            },
            [
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
              {
                name: "commentMarker",
                value: "",
                required: false,
              },
              { name: "delimiter", value: "", required: false },
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
            ],
          );
          return;
        }
      }
      importAtlasFile(selectedFile, isSource);
    },
    [importAtlasFile, openParametersDialog],
  );

  const onImportDocument = useCallback(
    (selectedFile: File, isSource: boolean) => {
      if (documentExists(selectedFile, isSource)) {
        openImportDialog(() => importFile(selectedFile, isSource));
      } else {
        importFile(selectedFile, isSource);
      }
    },
    [documentExists, importFile, openImportDialog],
  );
  return [
    <>
      {importDialog}
      {parametersDialog}
    </>,
    onImportDocument,
  ];
}
