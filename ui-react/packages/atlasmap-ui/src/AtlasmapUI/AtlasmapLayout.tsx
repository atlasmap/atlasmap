import React, { FunctionComponent, ReactNode, useMemo } from 'react';
import { TopologyView } from '@patternfly/react-topology';
import { AtlasmapContextToolbar } from './AtlasmapContextToolbar';
import { useAtlasmapUI } from './AtlasmapUIProvider';
import { Loading } from '../common';
import { AtlasmapSidebar } from './AtlasmapSidebar';
import { CanvasViewToolbar } from '../CanvasView/components';
import { useCanvasViewOptionsContext } from '../CanvasView/CanvasViewOptionsProvider';
import { MappingTable } from '.';

export interface IAtlasmapLayoutProps {
  mappingExpressionClearText: (
    nodeId?: string,
    startOffset?: number,
    endOffset?: number
  ) => any;
  mappingExpressionEmpty: () => boolean;
  mappingExpressionInit: () => void;
  mappingExpressionInsertText: (
    str: string,
    nodeId?: string,
    offset?: number
  ) => void;
  mappingExpressionObservable: () => any;
  mappingExpressionRemoveField: (
    tokenPosition?: string,
    offset?: number
  ) => void;
  onExportAtlasFile: (event: any) => void;
  onImportAtlasFile: (selectedFile: File) => void;
  onResetAtlasmap: () => void;
  onAddMapping: () => void;
  controlBar?: ReactNode;
  onConditionalMappingExpressionEnabled: () => boolean;
  onGetMappingExpression: () => string;
  onToggleExpressionMode: () => void;
  onToggleShowTypes: (id: any) => void;
  onToggleShowMappingPreview: (id: any) => void;
  showMappingTable: boolean;
  onToggleShowMappingTable: (id: any) => void;
  onToggleShowMappedFields: (id: any) => void;
  onToggleShowUnmappedFields: (id: any) => void;
  trailerId: string;
}

export const AtlasmapLayout: FunctionComponent<IAtlasmapLayoutProps> = ({
  onExportAtlasFile,
  onImportAtlasFile,
  onResetAtlasmap,
  onAddMapping,
  controlBar,
  mappingExpressionClearText,
  mappingExpressionEmpty,
  mappingExpressionInit,
  mappingExpressionInsertText,
  mappingExpressionObservable,
  mappingExpressionRemoveField,
  onConditionalMappingExpressionEnabled,
  onGetMappingExpression,
  onToggleExpressionMode,
  onToggleShowTypes,
  onToggleShowMappingPreview,
  showMappingTable,
  onToggleShowMappingTable,
  onToggleShowMappedFields,
  onToggleShowUnmappedFields,
  children,
  trailerId,
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
        onToggleShowMappingTable={onToggleShowMappingTable}
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
      onToggleShowMappingTable,
      onToggleShowTypes,
      onToggleShowUnmappedFields,
      toggleFreeView,
    ]
  );

  const canvasViewToolbar = useMemo(
    () => (
      <CanvasViewToolbar
        mappingExpressionClearText={mappingExpressionClearText}
        mappingExpressionEmpty={mappingExpressionEmpty}
        mappingExpressionInit={mappingExpressionInit}
        mappingExpressionInsertText={mappingExpressionInsertText}
        mappingExpressionObservable={mappingExpressionObservable}
        mappingExpressionRemoveField={mappingExpressionRemoveField}
        onConditionalMappingExpressionEnabled={
          onConditionalMappingExpressionEnabled
        }
        onGetMappingExpression={onGetMappingExpression}
        onToggleExpressionMode={onToggleExpressionMode}
        trailerId={trailerId}
      />
    ),
    [
      mappingExpressionClearText,
      mappingExpressionEmpty,
      mappingExpressionInit,
      mappingExpressionInsertText,
      mappingExpressionObservable,
      mappingExpressionRemoveField,
      onConditionalMappingExpressionEnabled,
      onGetMappingExpression,
      onToggleExpressionMode,
      trailerId,
    ]
  );

  return pending ? (
    <Loading />
  ) : !showMappingTable ? (
    <TopologyView
      contextToolbar={contextToolbar}
      viewToolbar={canvasViewToolbar}
      controlBar={controlBar}
      sideBar={<AtlasmapSidebar />}
      sideBarOpen={isEditingMapping}
    >
      {children}
    </TopologyView>
  ) : (
    <MappingTable
      contextToolbar={contextToolbar}
      viewToolbar={canvasViewToolbar}
    />
  );
};
