import React from 'react';
import './App.css';
import { Mapper } from '@atlasmap/ui';

const App: React.FC = () => {
  return (
    <Mapper
      sources={[]}
      targets={[]}
      mappings={[]}
      addToMapping={() => void(0)}
    />
  );
}

export default App;
