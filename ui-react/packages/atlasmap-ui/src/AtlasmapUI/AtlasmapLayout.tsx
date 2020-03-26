import React, { FunctionComponent, ReactNode, useMemo } from 'react';
import { TopologyView } from '@patternfly/react-topology';
import { AtlasmapContextToolbar } from './AtlasmapContextToolbar';
import { useAtlasmapUI } from './AtlasmapUIProvider';
import { Loading } from '../common';
import { AtlasmapSidebar } from './AtlasmapSidebar';
import { CanvasViewToolbar } from '../CanvasView/components';
import { useCanvasViewOptionsContext } from '../CanvasView/CanvasViewOptionsProvider';

export interface IAtlasmapLayoutProps {
  onExportAtlasFile: (event: any) => void;
  onImportAtlasFile: (selectedFile: File) => void;
  onResetAtlasmap: () => void;
  onAddMapping: () => void;
  controlBar?: ReactNode;
  onConditionalMappingExpressionEnabled: () => boolean;
  onGetMappingExpressionStr: () => string;
  onToggleExpressionMode: () => void;
  onToggleShowTypes: (id: any) => void;
  onToggleShowMappingPreview: (id: any) => void;
  onToggleShowMappedFields: (id: any) => void;
  onToggleShowUnmappedFields: (id: any) => void;
  expressionTokens: string[];
}

export const AtlasmapLayout: FunctionComponent<IAtlasmapLayoutProps> = ({
  onExportAtlasFile,
  onImportAtlasFile,
  onResetAtlasmap,
  onAddMapping,
  controlBar,
  onConditionalMappingExpressionEnabled,
  onGetMappingExpressionStr,
  onToggleExpressionMode,
  onToggleShowTypes,
  onToggleShowMappingPreview,
  onToggleShowMappedFields,
  onToggleShowUnmappedFields,
  expressionTokens,
  children,
}) => {
  const { pending, isEditingMapping } = useAtlasmapUI();

  const { toggleFreeView } = useCanvasViewOptionsContext();

  const contextToolbar = useMemo(
    () => (
      <AtlasmapContextToolbar
        onExportAtlasFile={onExportAtlasFile}
        onImportAtlasFile={onImportAtlasFile}
        onResetAtlasmap={onResetAtlasmap}
        onAddMapping={onAddMapping}
        onToggleShowTypes={onToggleShowTypes}
        onToggleShowMappingPreview={onToggleShowMappingPreview}
        onToggleShowMappedFields={onToggleShowMappedFields}
        onToggleShowUnmappedFields={onToggleShowUnmappedFields}
        onToggleShowFreeView={toggleFreeView}
      />
    ),
    [
      onExportAtlasFile,
      onImportAtlasFile,
      onResetAtlasmap,
      onAddMapping,
      onToggleShowMappedFields,
      onToggleShowMappingPreview,
      onToggleShowTypes,
      onToggleShowUnmappedFields,
      toggleFreeView,
    ]
  );

  const canvasViewToolbar = useMemo(
    () => (
      <CanvasViewToolbar
        expressionTokens={expressionTokens}
        onConditionalMappingExpressionEnabled={
          onConditionalMappingExpressionEnabled
        }
        onGetMappingExpressionStr={onGetMappingExpressionStr}
        onToggleExpressionMode={onToggleExpressionMode}
      />
    ),
    [
      expressionTokens,
      onConditionalMappingExpressionEnabled,
      onGetMappingExpressionStr,
      onToggleExpressionMode,
    ]
  );

  return pending ? (
    <Loading />
  ) : (
    <TopologyView
      contextToolbar={contextToolbar}
      viewToolbar={canvasViewToolbar}
      controlBar={controlBar}
      sideBar={<AtlasmapSidebar />}
      sideBarOpen={isEditingMapping}
    >
      {children}
    </TopologyView>
  );
};
