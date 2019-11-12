import { number } from '@storybook/addon-knobs';
import React from 'react';
import { Canvas, SourceTargetMapper } from '@src';
import { mappings, sources, targets } from '../../sampleData';

export default {
  title: 'Views/Source Target Mapper',
};

export const sample = () => (
  <Canvas
    width={number('width', 800)}
    height={number('height', 600)}
    zoom={number('zoom', 1)}
  >
    <SourceTargetMapper
      sources={sources}
      targets={targets}
      mappings={mappings}
    />
  </Canvas>
);
