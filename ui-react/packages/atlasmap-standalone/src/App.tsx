import { useAtlasmap } from '@atlasmap/provider';
import { Mapper } from '@atlasmap/ui';
import React from 'react';
import './App.css';

const App: React.FC = () => {
  const { sources, targets, mappings, pending, error, exportAtlasFile, importAtlasFile, resetAtlasmap } = useAtlasmap({
    baseJavaInspectionServiceUrl: '/v2/atlas/java/',
    baseXMLInspectionServiceUrl: '/v2/atlas/xml/',
    baseJSONInspectionServiceUrl: '/v2/atlas/json/',
    baseMappingServiceUrl: '/v2/atlas/',
  });
  console.log('Sources', sources);
  console.log('Targets', targets);
  console.log('Mappings', mappings);
  return (
    <Mapper
      sources={sources}
      targets={targets}
      mappings={mappings}
      addToMapping={() => void(0)}
      pending={pending}
      error={error}
      exportAtlasFile={exportAtlasFile}
      importAtlasFile={importAtlasFile}
      resetAtlasmap={resetAtlasmap}
    />
  );
};

export default App;
