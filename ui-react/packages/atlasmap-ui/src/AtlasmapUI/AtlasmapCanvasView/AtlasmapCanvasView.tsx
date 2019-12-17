import React, { FunctionComponent, ReactChild, useCallback, useEffect, useMemo, useState } from 'react';
import {
  CanvasView,
  CanvasViewProvider,
  CanvasViewControlBar, Links,
} from '../../CanvasView';
import { DragLayer } from './DragLayer';
import { useAtlasmapUI } from '../AtlasmapUIProvider';
import {AtlasmapLayout, IAtlasmapLayoutProps} from '../AtlasmapLayout';
import {
  ConnectedIcon,
  DisconnectedIcon,
  EyeIcon,
  InfoIcon,
} from '@patternfly/react-icons';

export interface IAtlasmapCanvasViewProps extends IAtlasmapLayoutProps {
  onShowMappingPreview: (enabled: boolean) => void;
  children: (props: { showTypes: boolean; showMappingPreview: boolean }) => ReactChild;
}

export const AtlasmapCanvasView: FunctionComponent<
  IAtlasmapCanvasViewProps
> = ({
  onExportAtlasFile,
  onImportAtlasFile,
  onResetAtlasmap,
  onShowMappingPreview,
  children
}) => {
  const {
    mappings,
    selectedMapping,
    isEditingMapping,
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
        <CanvasView showMappingColumn={isMappingColumnVisible}>
          {children({ showMappingPreview, showTypes })}
          <Links mappings={mappings} selectedMapping={selectedMapping} />
          <DragLayer />
        </CanvasView>
      </AtlasmapLayout>
    </CanvasViewProvider>
  );
};
