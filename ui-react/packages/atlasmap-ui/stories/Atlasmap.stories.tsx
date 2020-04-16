import { action } from '@storybook/addon-actions';
import React, { createElement, useState } from 'react';
import { IMapping } from '../src/CanvasView';
import { mappings as sampleMappings, sources, targets } from './sampleData';
import { Button } from '@patternfly/react-core';
import {
  AtlasmapCanvasView,
  AtlasmapCanvasViewMappings,
  AtlasmapCanvasViewSource,
  AtlasmapCanvasViewTarget,
  AtlasmapUIProvider,
  IAtlasmapField,
} from '../src/AtlasmapUI';
import { boolean } from '@storybook/addon-knobs';

export default {
  title: 'Mapper',
};

export const sample = () =>
  createElement(() => {
    const [mappings, setMappings] = useState<IMapping[]>(sampleMappings);
    const addToMapping = (node: IAtlasmapField, mapping: IMapping) => {
      const updatedMappings = mappings.map(m => {
        if (m.id === mapping.id) {
          if (node.id.includes('source')) {
            m.sourceFields = [...m.sourceFields, { id: node.id }];
          } else {
            m.targetFields = [...m.targetFields, { id: node.id }];
          }
        }
        return m;
      });
      setMappings(updatedMappings);
    };

    const createMapping = (
      source: IAtlasmapField | undefined,
      target?: IAtlasmapField
    ) => {
      setMappings([
        ...mappings,
        {
          id: `${Date.now()}`,
          name: 'One to One (mock)',
          sourceFields: source ? [{ id: source.id }] : [],
          targetFields: target ? [{ id: target.id }] : [],
        },
      ]);
    };

    const conditionalMappingExpressionEnabled = () => {
      return false;
    };

    const execFieldSearch = (searchFilter: string, isSource: boolean) => {
      if (searchFilter && isSource) {
        return searchFilter;
      }
      return '';
    };

    const getMappingExpression = () => {
      return '';
    };

    const mappingExprAddField = (
      selectedField: any,
      newTextNode: any,
      atIndex: number,
      isTrailer: boolean
    ) => {};

    const mappingExprClearText = (
      nodeId?: string,
      startOffset?: number,
      endOffset?: number
    ) => {
      if (nodeId || startOffset || endOffset) {
        return 'startOffset';
      } else {
        return '';
      }
    };
    const mappingExprEmpty = () => {
      return false;
    };
    const mappingExprInit = () => {};
    const mappingExpressionInsertText = (
      str: string,
      nodeId?: string,
      offset?: number
    ) => {
      if (nodeId || offset) {
        console.log(str);
      }
    };
    const mappingExpressionObservable = () => {};
    const mappingExpressionRemoveField = () => {};
    const trailerID = '';

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
          executeFieldSearch={execFieldSearch}
          onShowMappingPreview={action('AtlasmapCanvasView')}
          onShowMappedFields={action('onShowMappedFields')}
          onShowUnmappedFields={action('onShowUnmappedFields')}
          onExportAtlasFile={action('onExportAtlasFile')}
          onImportAtlasFile={action('onImportAtlasFile')}
          onResetAtlasmap={action('onResetAtlasmap')}
          onAddMapping={action('onAddMapping')}
          onConditionalMappingExpressionEnabled={
            conditionalMappingExpressionEnabled
          }
          onGetMappingExpression={getMappingExpression}
          mappingExpressionAddField={mappingExprAddField}
          mappingExpressionClearText={mappingExprClearText}
          mappingExpressionEmpty={mappingExprEmpty}
          mappingExpressionInit={mappingExprInit}
          mappingExpressionInsertText={mappingExpressionInsertText}
          mappingExpressionObservable={mappingExpressionObservable}
          mappingExpressionRemoveField={mappingExpressionRemoveField}
          onToggleExpressionMode={action('onToggleExpressionMode')}
          trailerId={trailerID}
        >
          {({ showTypes, showMappingPreview }) => (
            <>
              <AtlasmapCanvasViewSource
                onAddToMapping={addToMapping}
                onCreateConstant={action('onCreateConstant')}
                onDeleteConstant={action('onDeleteConstant')}
                onEditConstant={action('onEditConstant')}
                onCreateProperty={action('onCreateProperty')}
                onCreateMapping={createMapping}
                onDeleteProperty={action('onDeleteProperty')}
                onEditProperty={action('onEditProperty')}
                onDeleteDocument={action('onDeleteDocument')}
                onFieldPreviewChange={action('onFieldPreviewChange')}
                onImportDocument={action('onImportDocument')}
                onSearch={action('onSearch')}
                showMappingPreview={showMappingPreview}
                showTypes={showTypes}
                sources={sources}
              />

              <AtlasmapCanvasViewMappings />

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
