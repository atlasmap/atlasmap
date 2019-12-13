import React, {FunctionComponent, useCallback, useEffect, useMemo, useState} from 'react';
import {
  Links,
  CanvasView,
  CanvasViewProvider,
  CanvasViewControlBar,
  GroupId,
  ElementId,
} from '../../CanvasView';
import { DragLayer } from './DragLayer';
import { AtlasmapCanvasViewSource } from './AtlasmapCanvasViewSource';
import { AtlasmapCanvasViewMappings } from './AtlasmapCanvasViewMappings';
import { AtlasmapCanvasViewTarget } from './AtlasmapCanvasViewTarget';
import { useAtlasmapUI } from '../AtlasmapUIProvider';
import {AtlasmapLayout, IAtlasmapLayoutProps} from '../AtlasmapLayout';
import {
  ConnectedIcon,
  DisconnectedIcon,
  EyeIcon,
  InfoIcon,
} from '@patternfly/react-icons';
import { IAtlasmapField } from '../models';

export interface IAtlasmapCanvasViewProps extends IAtlasmapLayoutProps {
  onImportSourceDocument: (selectedFile: File) => void;
  onImportTargetDocument: (selectedFile: File) => void;
  onDeleteSourceDocument: (id: GroupId) => void;
  onDeleteTargetDocument: (id: GroupId) => void;
  onSourceSearch: (content: string) => void;
  onShowMappingPreview: (enabled: boolean) => void;
  onTargetSearch: (content: string) => void;
  onFieldPreviewChange: (field: IAtlasmapField, value: string) => void;
  onAddToMapping: (elementId: ElementId, mappingId: string) => void;
  onCreateMapping: (sourceId: ElementId, targetId: ElementId) => void;
}

export const AtlasmapCanvasView: FunctionComponent<
  IAtlasmapCanvasViewProps
> = ({
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
}) => {
  const {
    sources,
    targets,
    mappings,
    isEditingMapping,
    selectedMapping,
  } = useAtlasmapUI();

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

  const controlBar = useMemo(
    () => (
      <CanvasViewControlBar
        extraButtons={[
          {
            id: 'Show types',
            icon: <InfoIcon />,
            tooltip: 'Show types',
            ariaLabel: ' ',
            callback: toggleShowTypes,
          },
          {
            id: 'Show mapped fields',
            icon: <ConnectedIcon />,
            tooltip: 'Show mapped fields',
            ariaLabel: ' ',
          },
          {
            id: 'Show unmapped fields',
            icon: <DisconnectedIcon />,
            tooltip: 'Show unmapped fields',
            ariaLabel: ' ',
          },
          {
            id: 'Show mapping preview',
            icon: <EyeIcon />,
            tooltip: 'Show mapping preview',
            ariaLabel: ' ',
            callback: toggleShowMappingPreview,
          },
        ]}
      />
    ),
    [toggleShowMappingPreview, toggleShowTypes]
  );

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
        controlBar={controlBar}
        onExportAtlasFile={onExportAtlasFile}
        onImportAtlasFile={onImportAtlasFile}
        onResetAtlasmap={onResetAtlasmap}
      >
        <CanvasView isMappingColumnVisible={isMappingColumnVisible}>
          <AtlasmapCanvasViewSource
            onAddToMapping={onAddToMapping}
            onDeleteDocument={onDeleteSourceDocument}
            onFieldPreviewChange={onFieldPreviewChange}
            onImportDocument={onImportSourceDocument}
            onSearch={onSourceSearch}
            showMappingPreview={showMappingPreview}
            showTypes={showTypes}
            sources={sources}
          />

          <AtlasmapCanvasViewMappings
            onAddToMapping={onAddToMapping}
          />

          <AtlasmapCanvasViewTarget
            onAddToMapping={onAddToMapping}
            onCreateMapping={onCreateMapping}
            onDeleteDocument={onDeleteTargetDocument}
            onImportDocument={onImportTargetDocument}
            onSearch={onTargetSearch}
            showMappingPreview={showMappingPreview}
            showTypes={showTypes}
            targets={targets}
          />

          <Links mappings={mappings} selectedMapping={selectedMapping} />

          <DragLayer />
        </CanvasView>
      </AtlasmapLayout>
    </CanvasViewProvider>
  );
};
