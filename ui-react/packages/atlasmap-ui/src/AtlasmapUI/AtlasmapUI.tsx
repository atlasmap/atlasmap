import React, { FunctionComponent } from 'react';
import {
  AtlasmapUIProvider,
  IAtlasmapUIProviderProps,
} from './AtlasmapUIProvider';
import {
  AtlasmapCanvasView,
  IAtlasmapCanvasViewProps,
} from './AtlasmapCanvasView';
import { MappingDetails } from './MappingDetails';

export interface IAtlasmapUIProps
  extends Omit<IAtlasmapUIProviderProps, 'renderMappingDetails'>,
    IAtlasmapCanvasViewProps {}

export const AtlasmapUI: FunctionComponent<IAtlasmapUIProps> = ({
  onExportAtlasFile,
  onImportAtlasFile,
  onImportSourceDocument,
  onImportTargetDocument,
  onDeleteSourceDocument,
  onDeleteTargetDocument,
  onResetAtlasmap,
  onSourceSearch,
  onShowMappingPreview,
  onTargetSearch,
  onFieldPreviewChange,
  onAddToMapping,
  onCreateMapping,
  ...props
}) => {
  return (
    <AtlasmapUIProvider
      {...props}
      renderMappingDetails={mapping => <MappingDetails mapping={mapping} />}
    >
      <AtlasmapCanvasView
        onExportAtlasFile={onExportAtlasFile}
        onImportAtlasFile={onImportAtlasFile}
        onImportSourceDocument={onImportSourceDocument}
        onImportTargetDocument={onImportTargetDocument}
        onDeleteSourceDocument={onDeleteSourceDocument}
        onDeleteTargetDocument={onDeleteTargetDocument}
        onResetAtlasmap={onResetAtlasmap}
        onSourceSearch={onSourceSearch}
        onShowMappingPreview={onShowMappingPreview}
        onTargetSearch={onTargetSearch}
        onFieldPreviewChange={onFieldPreviewChange}
        onAddToMapping={onAddToMapping}
        onCreateMapping={onCreateMapping}
      />
    </AtlasmapUIProvider>
  );
};
