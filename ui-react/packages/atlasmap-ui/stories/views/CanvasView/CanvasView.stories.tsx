import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import React from 'react';
import {
  CanvasView,
  Document,
  FieldsBoxHeader,
  Links,
  Mapping,
  MappingElement,
} from '../../../src/views/CanvasView';
import { Source } from '../../../src/views/CanvasView/components/Source';
import { Target } from '../../../src/views/CanvasView/components/Target';
import { mappings, sources, targets } from '../../sampleData';

export default {
  title: 'Views/Source Target Mapper',
};

export const sample = () => {
  const selectedMapping = text('Selected mapping', '');
  const selectMapping = action('selectMapping');
  const deselectMapping = action('deselectMapping');
  const editMapping = action('editMapping');
  const addToMapping = action('addToMapping');

  return (
    <CanvasView>
      <Source
        header={
          <FieldsBoxHeader
            title={'Source'}
            onSearch={action('onSearch')}
            onImport={action('onImportAtlasFile')}
            onJavaClasses={action('onJavaClasses')}
          />
        }
      >
        {sources.map(s => {
          return (
            <Document
              key={s.id}
              title={s.name}
              footer={'Source document'}
              type={'source'}
              lineConnectionSide={'right'}
              fields={s}
              renderNode={_ => <>test</>}
              onDelete={action('onDelete Source')}
            />
          );
        })}
      </Source>
      <Mapping>
        {({ ref }) => (
          <>
            {mappings.map(m => {
              return (
                <MappingElement
                  key={m.id}
                  node={m}
                  boxRef={ref}
                  selectedMapping={selectedMapping}
                  selectMapping={selectMapping}
                  deselectMapping={deselectMapping}
                  editMapping={editMapping}
                  addToMapping={addToMapping}
                  mappingType={'Split'}
                />
              );
            })}
          </>
        )}
      </Mapping>
      <Target
        header={
          <FieldsBoxHeader
            title={'Source'}
            onSearch={action('onSearch')}
            onImport={action('onImportAtlasFile')}
            onJavaClasses={action('onJavaClasses')}
          />
        }
      >
        {targets.map(t => {
          return (
            <Document
              key={t.id}
              title={t.name}
              footer={'Target document'}
              type={'target'}
              lineConnectionSide={'left'}
              fields={t}
              renderNode={_ => <>test</>}
              onDelete={action('onDelete Target')}
            />
          );
        })}
      </Target>

      <Links
        mappings={mappings}
        selectedMapping={selectedMapping}
      />
    </CanvasView>
  );
}
