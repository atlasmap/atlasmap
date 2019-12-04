import { useAtlasmap } from "@atlasmap/provider";
import { Atlasmap } from "@atlasmap/ui";
import React, { useCallback, useState } from "react";
import "./App.css";

const App: React.FC = () => {
  const [sourceFilter, setSourceFilter] = useState<string | undefined>();
  const [targetFilter, setTargetFilter] = useState<string | undefined>();
  const {
    sources,
    targets,
    mappings,
    pending,
    error,
    importAtlasFile,
    resetAtlasmap,
    exportAtlasFile
  } = useAtlasmap({
    sourceFilter,
    targetFilter
  });

  const handleImportAtlasFile = useCallback(
    (selectedFile: File) => importAtlasFile(selectedFile, false),
    [importAtlasFile]
  );
  const handleImportSourceDocument = useCallback(
    (selectedFile: File) => importAtlasFile(selectedFile, true),
    [importAtlasFile]
  );
  const handleImportTargetDocument = useCallback(
    (selectedFile: File) => importAtlasFile(selectedFile, true),
    [importAtlasFile]
  );

  return (
    <Atlasmap
      sources={sources}
      targets={targets}
      mappings={mappings}
      addToMapping={() => void 0}
      pending={pending}
      error={error}
      onImportAtlasFile={handleImportAtlasFile}
      onImportSourceDocument={handleImportSourceDocument}
      onImportTargetDocument={handleImportTargetDocument}
      onResetAtlasmap={resetAtlasmap}
      onSourceSearch={setSourceFilter}
      onTargetSearch={setTargetFilter}
      onExportAtlasFile={exportAtlasFile}
    />
  );
};

export default App;
