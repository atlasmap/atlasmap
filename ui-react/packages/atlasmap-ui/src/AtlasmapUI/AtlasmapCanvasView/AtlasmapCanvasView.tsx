import React, {
  FunctionComponent,
  ReactChild,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react';
import {
  CanvasView,
  CanvasViewProvider,
  CanvasViewControlBar,
  Links,
  getToolbarIconStyle,
} from '../../CanvasView';
import { DragLayer } from './DragLayer';
import { useAtlasmapUI } from '../AtlasmapUIProvider';
import { AtlasmapLayout, IAtlasmapLayoutProps } from '../AtlasmapLayout';
import {
  MapMarkedIcon,
  EyeIcon,
  MapIcon,
  InfoIcon,
} from '@patternfly/react-icons';

export interface IAtlasmapCanvasViewProps extends IAtlasmapLayoutProps {
  onShowMappingPreview: (enabled: boolean) => void;
  onShowMappedFields: (enabled: boolean) => void;
  onShowUnmappedFields: (enabled: boolean) => void;
  children: (props: {
    showTypes: boolean;
    showMappingPreview: boolean;
  }) => ReactChild;
}

export const AtlasmapCanvasView: FunctionComponent<
  IAtlasmapCanvasViewProps
> = ({
  onExportAtlasFile,
  onImportAtlasFile,
  onResetAtlasmap,
  onShowMappingPreview,
  onShowMappedFields,
  onShowUnmappedFields,
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

  const controlBar = useMemo(
    () => (
      <CanvasViewControlBar
        extraButtons={[
          {
            id: 'Show types',
            icon: <InfoIcon style={getToolbarIconStyle(showTypes)} />,
            tooltip: 'Show types',
            ariaLabel: ' ',
            callback: toggleShowTypes,
          },
          {
            id: 'Show mapped fields',
            icon: (
              <MapMarkedIcon style={getToolbarIconStyle(showMappedFields)} />
            ),
            tooltip: 'Show mapped fields',
            ariaLabel: ' ',
            callback: toggleShowMappedFields,
          },
          {
            id: 'Show unmapped fields',
            icon: (
              <MapIcon style={getToolbarIconStyle(showUnmappedFields)} />
            ),
            tooltip: 'Show unmapped fields',
            ariaLabel: ' ',
            callback: toggleShowUnmappedFields,
          },
          {
            id: 'Show mapping preview',
            icon: <EyeIcon style={getToolbarIconStyle(showMappingPreview)} />,
            tooltip: 'Show mapping preview',
            ariaLabel: ' ',
            callback: toggleShowMappingPreview,
          },
        ]}
      />
    ),
    [
      showTypes,
      toggleShowTypes,
      showMappedFields,
      toggleShowMappedFields,
      showUnmappedFields,
      toggleShowUnmappedFields,
      showMappingPreview,
      toggleShowMappingPreview,
    ]
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
