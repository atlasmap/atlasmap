import React, {
  FunctionComponent,
  ReactChild,
  useCallback,
  useEffect,
  useState,
  useMemo,
} from 'react';
import {
  CanvasView,
  CanvasViewProvider,
  Links,
  CanvasViewControlBar,
} from '../../CanvasView';
import { DragLayer } from './DragLayer';
import { useAtlasmapUI } from '../AtlasmapUIProvider';
import { AtlasmapLayout } from '../AtlasmapLayout';

export interface IAtlasmapCanvasViewProps {
  onShowMappingPreview: (enabled: boolean) => void;
  onShowMappedFields: (enabled: boolean) => void;
  onShowUnmappedFields: (enabled: boolean) => void;
  onConditionalMappingExpressionEnabled: () => boolean;
  onGetMappingExpressionStr: () => string;
  onToggleExpressionMode: () => void;
  onExportAtlasFile: (event: any) => void;
  onImportAtlasFile: (selectedFile: File) => void;
  onResetAtlasmap: () => void;
  expressionTokens: string[];
  children: (props: {
    showTypes: boolean;
    showMappingPreview: boolean;
  }) => ReactChild;
}

export const AtlasmapCanvasView: FunctionComponent<IAtlasmapCanvasViewProps> = ({
  onShowMappingPreview,
  onShowMappedFields,
  onShowUnmappedFields,
  onConditionalMappingExpressionEnabled,
  onGetMappingExpressionStr,
  onToggleExpressionMode,
  onExportAtlasFile,
  onImportAtlasFile,
  onResetAtlasmap,
  expressionTokens,
  children,
}) => {
  const { mappings, selectedMapping, isEditingMapping } = useAtlasmapUI();

  const isMappingColumnVisible = !isEditingMapping;

  const [showTypes, setShowTypes] = useState(false);
  const toggleShowTypes = useCallback(() => setShowTypes(!showTypes), [
    showTypes,
  ]);
  const [showMappingPreview, setShowMappingPreview] = useState(false);

  const toggleShowMappingPreview = useCallback(() => {
    const newValue = !showMappingPreview;
    setShowMappingPreview(newValue);
    onShowMappingPreview(newValue);
  }, [onShowMappingPreview, showMappingPreview]);

  const getMappingExpressionStr = useCallback(() => {
    return onGetMappingExpressionStr();
  }, [onGetMappingExpressionStr]);

  const toggleExpressionMode = useCallback(() => {
    onToggleExpressionMode();
  }, [onToggleExpressionMode]);

  const [showMappedFields, setShowMappedFields] = useState(true);
  const toggleShowMappedFields = useCallback(() => {
    const newValue = !showMappedFields;
    setShowMappedFields(newValue);
    onShowMappedFields(newValue);
  }, [showMappedFields, onShowMappedFields]);
  const [showUnmappedFields, setShowUnmappedFields] = useState(true);
  const toggleShowUnmappedFields = useCallback(() => {
    const newValue = !showUnmappedFields;
    setShowUnmappedFields(newValue);
    onShowUnmappedFields(newValue);
  }, [showUnmappedFields, onShowUnmappedFields]);

  const controlBar = useMemo(() => <CanvasViewControlBar />, []);

  useEffect(() => {
    const timeout = setTimeout(
      () => {
        window.dispatchEvent(new Event('resize'));
      },
      isEditingMapping ? 150 : 0
    );
    return () => clearTimeout(timeout);
  }, [isEditingMapping]);

  return (
    <CanvasViewProvider>
      <AtlasmapLayout
        onExportAtlasFile={onExportAtlasFile}
        onImportAtlasFile={onImportAtlasFile}
        onResetAtlasmap={onResetAtlasmap}
        controlBar={controlBar}
        onConditionalMappingExpressionEnabled={
          onConditionalMappingExpressionEnabled
        }
        onGetMappingExpressionStr={getMappingExpressionStr}
        onToggleExpressionMode={toggleExpressionMode}
        onToggleShowTypes={toggleShowTypes}
        onToggleShowMappingPreview={toggleShowMappingPreview}
        onToggleShowMappedFields={toggleShowMappedFields}
        onToggleShowUnmappedFields={toggleShowUnmappedFields}
        expressionTokens={expressionTokens}
      >
        <CanvasView showMappingColumn={isMappingColumnVisible}>
          {children({ showMappingPreview, showTypes })}
          <Links mappings={mappings} selectedMapping={selectedMapping} />
          <DragLayer />
        </CanvasView>
      </AtlasmapLayout>
    </CanvasViewProvider>
  );
};
