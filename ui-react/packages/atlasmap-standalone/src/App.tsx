import { Atlasmap } from "@atlasmap/core";
import { Page, PageHeader, PageSection } from "@patternfly/react-core";
import React, { useCallback } from "react";
import { useConfirmationDialog } from "./useConfirmationDialog";
import { useSingleInputDialog } from "./useSingleInputDialog";

const App: React.FC = () => {
  const [importDialog, openImportDialog] = useConfirmationDialog({
    title: "Overwrite selected document?",
    content:
      "Are you sure you want to overwrite the selected document and remove any associated mappings?"
  });

  const defaultCatalogName = "atlasmap-mapping.adm";
  const [exportDialog, openExportDialog] = useSingleInputDialog({
    title: "Export Mappings and Documents.",
    content: "Please enter a name for your exported catalog file",
    placeholder: defaultCatalogName
  });

  const [resetDialog, openResetDialog] = useConfirmationDialog({
    title: "Reset All Mappings and Imports?",
    content:
      "Are you sure you want to reset all mappings and clear all imported documents?"
  });

  const [
    deleteDocumentDialog,
    openDeleteDocumentDialog
  ] = useConfirmationDialog({
    title: "Remove selected document?",
    content:
      "Are you sure you want to remove the selected document and any associated mappings?"
  });

  const handleExportAtlasFile = useCallback(
    (exportAtlasFile: (fileName: string) => void) => {
      openExportDialog(value => {
        if (value.length === 0) {
          value = defaultCatalogName;
        }
        exportAtlasFile(value);
      });
    },
    [openExportDialog]
  );

  const handleResetAtlasmap = useCallback(
    (resetAtlasmap: () => void) => openResetDialog(resetAtlasmap),
    [openResetDialog]
  );
  const handleImportDocument = useCallback(
    (importDocument: () => void) => openImportDialog(importDocument),
    [openImportDialog]
  );
  const handleDeleteDocument = useCallback(
    (deleteDocument: () => void) => openDeleteDocumentDialog(deleteDocument),
    [openDeleteDocumentDialog]
  );

  return (
    <>
      <Page
        header={
          <PageHeader
            logo={
              <>
                <strong>Atlasmap</strong>&nbsp;Data Mapper UI
              </>
            }
            style={{ minHeight: 40 }}
          />
        }
      >
        <PageSection variant={"light"} noPadding={true} isFilled={true}>
          <Atlasmap
            onExportAtlasFile={handleExportAtlasFile}
            onResetAtlasmap={handleResetAtlasmap}
            onImportDocument={handleImportDocument}
            onDeleteDocument={handleDeleteDocument}
          />
        </PageSection>
      </Page>
      {exportDialog}
      {importDialog}
      {deleteDocumentDialog}
      {resetDialog}
    </>
  );
};

export default App;
