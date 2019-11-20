import { TopologyView } from '@patternfly/react-topology';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { useGesture } from 'react-use-gesture';
import { Canvas } from '../canvas';
import { useDimensions } from '../common';
import { Coords, ElementId, ElementType, IFieldsGroup, IMappings } from '../models';
import { SourceTargetMapper } from '../views/sourcetargetmapper';
import { MapperContextToolbar } from './MapperContextToolbar';
import { MapperControlBar } from './MapperControlBar';
import { MapperViewToolbar } from './MapperViewToolbar';
import { MappingDetails } from './MappingDetails';

export interface IMapperProps {
  sources: IFieldsGroup[];
  targets: IFieldsGroup[];
  mappings: IMappings[];
  addToMapping: (elementId: ElementId, elementType: ElementType, mappingId: string) => void;
  importAtlasFile: (selectedFile: File) => void;
}

export const Mapper: FunctionComponent<IMapperProps> = ({
  sources,
  mappings,
  targets,
  addToMapping,
  importAtlasFile
}) => {
  const [freeView, setFreeView] = useState(false);
  const [materializedMappings, setMaterializedMappings] = useState(true);
  const [selectedMapping, setSelectedMapping] = useState<string>();
  const [isEditingMapping, setisEditingMapping] = useState(false);
  const [zoom, setZoom] = useState(1);
  const [isPanning, setIsPanning] = useState(false);
  const [{ x: panX, y: panY }, setPan] = useState<Coords>({ x: 0, y: 0 });

  const [dimensionsRef, { width, height }, measure] = useDimensions();

  const resetPan = useCallback(() => {
    setPan({ x: 0, y: 0 });
  }, [setPan]);
  const bind = useGesture({
    onDrag: ({ movement: [x, y], first, last, memo = [panX, panY] }) => {
      if (freeView) {
        if (first) setIsPanning(true);
        if (last) setIsPanning(false);
        setPan({ x: x + memo[0], y: y + memo[1] });
      }
      return memo;
    },
    onWheel: ({ delta }) => {
      if (freeView) {
        updateZoom(delta[1] * -0.001);
      }
    },
  });

  const updateZoom = useCallback(
    (tick: number) => {
      setZoom(currentZoom => Math.max(0.2, Math.min(2, currentZoom + tick)));
    },
    [setZoom]
  );

  const handleZoomIn = useCallback(() => {
    updateZoom(0.2);
  }, [updateZoom]);
  const handleZoomOut = useCallback(() => {
    updateZoom(-0.2);
  }, [updateZoom]);
  const handleViewReset = useCallback(() => {
    setZoom(1);
    resetPan();
  }, [setZoom, resetPan]);

  const closeMappingDetails = useCallback(() => {
    setSelectedMapping(undefined);
    setisEditingMapping(false)
  }, [setSelectedMapping, setisEditingMapping]);

  const selectMapping = useCallback(
    (mapping: string) => {
      if (!isEditingMapping) {
        setSelectedMapping(mapping);
      }
    },
    [setSelectedMapping, isEditingMapping]
  );

  const deselectMapping = useCallback(
    () => {
      setSelectedMapping(undefined);
    },
    [setSelectedMapping]
  );

  const editMapping = useCallback(
    () => {
      if (selectedMapping) {
        setisEditingMapping(true)
      }
    },
    [selectedMapping, setisEditingMapping]
  );

  const sideBar = (
    <MappingDetails show={isEditingMapping} onClose={closeMappingDetails}>
      {selectedMapping}
    </MappingDetails>
  );

  useEffect(() => {
    const timeout = setTimeout(measure, 150);
    return () => clearTimeout(timeout);
  }, [measure, selectedMapping]);

  const contextToolbar = useMemo(() => <MapperContextToolbar importAtlasFile={importAtlasFile}/>, []);
  const toggleFreeView = useCallback(() => setFreeView(!freeView), [
    freeView,
    setFreeView,
  ]);
  const toggleMaterializedMappings = useCallback(
    () => setMaterializedMappings(!materializedMappings),
    [setMaterializedMappings, materializedMappings]
  );
  const viewToolbar = useMemo(
    () => (
      <MapperViewToolbar
        freeView={freeView}
        toggleFreeView={toggleFreeView}
        materializedMappings={materializedMappings}
        toggleMaterializedMappings={toggleMaterializedMappings}
      />
    ),
    [freeView, materializedMappings, toggleFreeView, toggleMaterializedMappings]
  );
  const controlBar = useMemo(
    () => (
      <MapperControlBar
        onZoomIn={handleZoomIn}
        onZoomOut={handleZoomOut}
        onZoomReset={handleViewReset}
      />
    ),
    [handleViewReset, handleZoomIn, handleZoomOut]
  );
  return (
    <TopologyView
      contextToolbar={contextToolbar}
      viewToolbar={viewToolbar}
      controlBar={freeView ? controlBar : undefined}
      sideBar={sideBar}
      sideBarOpen={isEditingMapping}
    >
      <div
        ref={dimensionsRef}
        style={{ height: '100%', flex: '1' }}
        {...bind()}
      >
        {width && (
          <Canvas
            width={width}
            height={height}
            zoom={freeView ? zoom : 1}
            panX={freeView ? panX : 0}
            panY={freeView ? panY : 0}
            allowPanning={freeView}
            isPanning={freeView ? isPanning : false}
          >
            <SourceTargetMapper
              sources={sources}
              mappings={mappings}
              targets={targets}
              freeView={freeView}
              materializedMappings={materializedMappings}
              selectedMapping={selectedMapping}
              selectMapping={selectMapping}
              deselectMapping={deselectMapping}
              editMapping={editMapping}
              addToMapping={addToMapping}
            />
          </Canvas>
        )}
      </div>
    </TopologyView>
  );
};
