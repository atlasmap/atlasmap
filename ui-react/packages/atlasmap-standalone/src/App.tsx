import { useAtlasmap } from '@atlasmap/provider';
import { Mapper } from '@atlasmap/ui';
import React from 'react';
import './App.css';

const App: React.FC = () => {
  const { sourceDocs, targetDocs } = useAtlasmap({
    baseJavaInspectionServiceUrl: '/v2/atlas/java/',
    baseXMLInspectionServiceUrl: '/v2/atlas/xml/',
    baseJSONInspectionServiceUrl: '/v2/atlas/json/',
    baseMappingServiceUrl: '/v2/atlas/',
  });
  console.log('Source docs', sourceDocs);
  console.log('Target docs', targetDocs);
  return (
    <Mapper
      sources={[]}
      targets={[]}
      mappings={[]}
      addToMapping={() => void(0)}
    />
  );
};

export default App;

