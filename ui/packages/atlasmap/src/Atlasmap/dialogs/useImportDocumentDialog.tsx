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
              {
                name: "delimiter",
                label: "Delimiter",
                value: "",
                required: false,
              },
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
              {
                name: "skipHeaderRecord",
                label: "Skip Header Record",
                value: "true",
                boolean: true,
                required: false,
              },
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
