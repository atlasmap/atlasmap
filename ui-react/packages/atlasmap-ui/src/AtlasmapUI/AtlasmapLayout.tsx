import React, { FunctionComponent, useCallback, useMemo } from 'react';
import { TopologyView } from '@patternfly/react-topology';
import { AtlasmapContextToolbar } from './AtlasmapContextToolbar';
import { useAtlasmapUI } from './AtlasmapUIProvider';
import { Loading } from '../common';
import { AtlasmapSidebar } from './AtlasmapSidebar';
import { CanvasViewToolbar } from '../CanvasView/components';
import { useCanvasViewOptionsContext } from '../CanvasView/CanvasViewOptionsProvider';
import { useCanvasViewContext } from '../CanvasView/CanvasViewCanvasProvider';

export interface IAtlasmapLayoutProps {
  onExportAtlasFile: (event: any) => void;
  onImportAtlasFile: (selectedFile: File) => void;
  onResetAtlasmap: () => void;
  onToggleShowTypes: (id: any) => void;
  onToggleShowMappingPreview: (id: any) => void;
  onToggleShowMappedFields: (id: any) => void;
  onToggleShowUnmappedFields: (id: any) => void;
}

export const AtlasmapLayout: FunctionComponent<IAtlasmapLayoutProps> = ({
  onExportAtlasFile,
  onImportAtlasFile,
  onResetAtlasmap,
  onToggleShowTypes,
  onToggleShowMappingPreview,
  onToggleShowMappedFields,
  onToggleShowUnmappedFields,
  children,
}) => {
  const { pending, isEditingMapping } = useAtlasmapUI();

  const { updateZoom, resetZoom, resetPan } = useCanvasViewContext();

  const { freeView, toggleFreeView } = useCanvasViewOptionsContext();

  const handleZoomIn = useCallback(() => {
    updateZoom(0.2);
  }, [updateZoom]);
  const handleZoomOut = useCallback(() => {
    updateZoom(-0.2);
  }, [updateZoom]);
  const handleResetView = useCallback(() => {
    resetZoom();
    resetPan();
  }, [resetZoom, resetPan]);

  const contextToolbar = useMemo(
    () => (
      <AtlasmapContextToolbar
        onExportAtlasFile={onExportAtlasFile}
        onImportAtlasFile={onImportAtlasFile}
        onResetAtlasmap={onResetAtlasmap}
        onToggleShowTypes={onToggleShowTypes}
        onToggleShowMappingPreview={onToggleShowMappingPreview}
        onToggleShowMappedFields={onToggleShowMappedFields}
        onToggleShowUnmappedFields={onToggleShowUnmappedFields}
        onToggleShowFreeView={toggleFreeView}
        onZoomIn={handleZoomIn}
        onZoomOut={handleZoomOut}
        onResetView={handleResetView}
        showFreeView={freeView}
      />
    ),
    [onExportAtlasFile, onImportAtlasFile, onResetAtlasmap]
  );

  return pending ? (
    <Loading />
  ) : (
    <TopologyView
      contextToolbar={contextToolbar}
      viewToolbar={<CanvasViewToolbar />}
      sideBar={<AtlasmapSidebar />}
      sideBarOpen={isEditingMapping}
    >
      {children}
    </TopologyView>
  );
};
