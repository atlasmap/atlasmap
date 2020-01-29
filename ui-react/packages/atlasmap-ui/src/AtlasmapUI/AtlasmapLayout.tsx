import React, { FunctionComponent, useMemo } from 'react';
import { TopologyView } from '@patternfly/react-topology';
import { AtlasmapContextToolbar } from './AtlasmapContextToolbar';
import { useAtlasmapUI } from './AtlasmapUIProvider';
import { Loading } from '../common';
import { AtlasmapSidebar } from './AtlasmapSidebar';
import { CanvasViewToolbar } from '../CanvasView/components';

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
