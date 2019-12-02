import { useAtlasmap } from '@atlasmap/provider';
import { Atlasmap } from '@atlasmap/ui';
import React, { useState } from 'react';
import './App.css';

const App: React.FC = () => {
  const [sourceFilter, setSourceFilter] = useState<string | undefined>();
  const [targetFilter, setTargetFilter] = useState<string | undefined>();
  const { sources, targets, mappings, pending, error, importAtlasFile, resetAtlasmap, exportAtlasFile } = useAtlasmap({
    sourceFilter,
    targetFilter
  });
  return (
    <Atlasmap
      sources={sources}
      targets={targets}
      mappings={mappings}
      addToMapping={() => void(0)}
      pending={pending}
      error={error}
      onImportAtlasFile={importAtlasFile}
      onResetAtlasmap={resetAtlasmap}
      onSourceSearch={setSourceFilter}
      onTargetSearch={setTargetFilter}
      onExportAtlasFile={exportAtlasFile}
    />
  );
};

export default App;
