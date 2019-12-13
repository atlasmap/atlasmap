import { action } from '@storybook/addon-actions';
import React from 'react';
import { FieldsBoxHeader } from '../../../src/CanvasView/components';

export default {
  title: 'CanvasView/FieldsBoxHeader',
};

export const interactive = () => (
  <FieldsBoxHeader
    title={'Source'}
    onSearch={action('onSearch')}
    onImport={action('onImportAtlasFile')}
    onJavaClasses={action('onJavaClasses')}
  />
);
