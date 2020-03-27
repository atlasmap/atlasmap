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
  onShowMappingPreview: (enabled: boolean) => void;
  onShowMappedFields: (enabled: boolean) => void;
  onShowUnmappedFields: (enabled: boolean) => void;
  onConditionalMappingExpressionEnabled: () => boolean;
  onGetMappingExpression: () => string;
  onToggleExpressionMode: () => void;
  onExportAtlasFile: (event: any) => void;
  onImportAtlasFile: (selectedFile: File) => void;
  onResetAtlasmap: () => void;
  onAddMapping: () => void;
  children: (props: {
    showTypes: boolean;
    showMappingPreview: boolean;
  }) => ReactChild;
  trailerId: string;
}

export const AtlasmapCanvasView: FunctionComponent<IAtlasmapCanvasViewProps> = ({
  mappingExpressionClearText,
  mappingExpressionEmpty,
  mappingExpressionInit,
  mappingExpressionInsertText,
  mappingExpressionObservable,
  mappingExpressionRemoveField,
  onShowMappingPreview,
  onShowMappedFields,
  onShowUnmappedFields,
  onConditionalMappingExpressionEnabled,
  onGetMappingExpression,
  onToggleExpressionMode,
  onExportAtlasFile,
  onImportAtlasFile,
  onResetAtlasmap,
  onAddMapping,
  children,
  trailerId,
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

  const mappingExprEmpty = useCallback((): boolean => {
    return mappingExpressionEmpty();
  }, [mappingExpressionEmpty]);

  const mappingExprInit = useCallback(() => {
    return mappingExpressionInit();
  }, [mappingExpressionInit]);

  const mappingExprClearText = useCallback(() => {
    return mappingExpressionClearText();
  }, [mappingExpressionClearText]);

  const getMappingExpression = useCallback(() => {
    return onGetMappingExpression();
  }, [onGetMappingExpression]);

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
        mappingExpressionClearText={mappingExprClearText}
        mappingExpressionEmpty={mappingExprEmpty}
        mappingExpressionInit={mappingExprInit}
        mappingExpressionInsertText={mappingExpressionInsertText}
        mappingExpressionObservable={mappingExpressionObservable}
        mappingExpressionRemoveField={mappingExpressionRemoveField}
        onExportAtlasFile={onExportAtlasFile}
        onImportAtlasFile={onImportAtlasFile}
        onResetAtlasmap={onResetAtlasmap}
        onAddMapping={onAddMapping}
        controlBar={controlBar}
        onConditionalMappingExpressionEnabled={
          onConditionalMappingExpressionEnabled
        }
        onGetMappingExpression={getMappingExpression}
        onToggleExpressionMode={toggleExpressionMode}
        onToggleShowTypes={toggleShowTypes}
        onToggleShowMappingPreview={toggleShowMappingPreview}
        onToggleShowMappedFields={toggleShowMappedFields}
        onToggleShowUnmappedFields={toggleShowUnmappedFields}
        trailerId={trailerId}
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
