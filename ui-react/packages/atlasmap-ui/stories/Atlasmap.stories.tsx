import { action } from '@storybook/addon-actions';
import React, { createElement, useState } from 'react';
import { ElementId, IMapping } from '../src/CanvasView';
import { mappings as sampleMappings, sources, targets } from './sampleData';
import {Button} from "@patternfly/react-core";
import {
  AtlasmapCanvasView,
  AtlasmapCanvasViewMappings,
  AtlasmapCanvasViewSource, AtlasmapCanvasViewTarget,
  AtlasmapUIProvider
} from "../src/AtlasmapUI";
import { boolean } from "@storybook/addon-knobs";

export default {
  title: 'Mapper',
};

export const sample = () =>
  createElement(() => {
    const [mappings, setMappings] = useState<IMapping[]>(sampleMappings);
    const addToMapping = (elementId: ElementId, mappingId: string) => {
      const updatedMappings = mappings.map(m => {
        if (m.id === mappingId) {
          if (/*elementType === */ 'source') {
            m.sourceFields = [
              ...m.sourceFields,
              { id: elementId },
            ];
          } else {
            m.targetFields = [
              ...m.targetFields,
              { id: elementId },
            ];
          }
        }
        return m;
      });
      setMappings(updatedMappings);
    };

    const createMapping = (sourceId: ElementId, targetId: ElementId) => {
      setMappings([
        ...mappings,
        {
          id: `${Date.now()}`,
          name: 'One to One (mock)',
          sourceFields: [{ id: sourceId }],
          targetFields: [{ id: targetId }],
        },
      ]);
    };

    return (
      <AtlasmapUIProvider
        error={boolean('error', false)}
        pending={boolean('pending', false)}
        sources={sources}
        targets={targets}
        mappings={mappings}
        onActiveMappingChange={action('onActiveMappingChange')}
        renderMappingDetails={({ mapping, closeDetails }) => (
          <>
            {mapping.name}
            <Button onClick={closeDetails}>Close</Button>
          </>
        )}
      >
        <AtlasmapCanvasView
          onShowMappingPreview={action('AtlasmapCanvasView')}
          onExportAtlasFile={action('onExportAtlasFile')}
          onImportAtlasFile={action('onImportAtlasFile')}
          onResetAtlasmap={action('onResetAtlasmap')}
        >
          {({ showTypes, showMappingPreview }) => (
            <>
              <AtlasmapCanvasViewSource
                onAddToMapping={addToMapping}
                onDeleteDocument={action('onDeleteDocument')}
                onFieldPreviewChange={action('onFieldPreviewChange')}
                onImportDocument={action('onImportDocument')}
                onSearch={action('onSearch')}
                showMappingPreview={showMappingPreview}
                showTypes={showTypes}
                sources={sources}
              />

              <AtlasmapCanvasViewMappings onAddToMapping={addToMapping} />

              <AtlasmapCanvasViewTarget
                onAddToMapping={addToMapping}
                onCreateMapping={createMapping}
                onDeleteDocument={action('onDeleteDocument')}
                onImportDocument={action('onImportDocument')}
                onSearch={action('onSearch')}
                showMappingPreview={showMappingPreview}
                showTypes={showTypes}
                targets={targets}
              />
            </>
          )}
        </AtlasmapCanvasView>
      </AtlasmapUIProvider>
    );
  });
