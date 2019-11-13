import { TopologyView } from '@patternfly/react-topology';
import { Canvas } from '@src/canvas';
import { MappingGroup, Mapping, Coords } from '@src/models';
import { SourceTargetMapper } from '@src/views';
import { useDimensions } from '@src/useDimensions';
import { MappingDetails } from '@src/MappingDetails';
import { MapperControlBar } from '@src/mapper/MapperControlBar';
import { MapperProvider } from '@src/mapper/MapperContext';
import { MapperViewToolbar } from '@src/mapper/MapperViewToolbar';
import { MapperContextToolbar } from '@src/mapper/MapperContextToolbar';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useMemo,
  useState,
  WheelEvent,
} from 'react';
import { useDrag } from 'react-use-gesture';

export interface IMapperProps {
  sources: MappingGroup[];
  targets: MappingGroup[];
  mappings: Mapping[];
}

export const Mapper: FunctionComponent<IMapperProps> = ({
  sources,
  mappings,
  targets,
}) => {
  const [dimensionsRef, { width, height }, measure] = useDimensions();
  const [mappingDetails, setMappingDetails] = useState<string | null>(null);

  const [zoom, setZoom] = useState(1);

  const [isPanning, setIsPanning] = useState(false);
  const [{ x: panX, y: panY }, setPan] = useState<Coords>({ x: 0, y: 0 });
  const resetPan = useCallback(() => {
    setPan({ x: 0, y: 0 });
  }, [setPan]);
  const bind = useDrag(
    ({ movement: [x, y], first, last, args: [panX, panY] }) => {
      if (first) setIsPanning(true);
      if (last) setIsPanning(false);
      setPan({ x: x + panX, y: y + panY });
    }
  );

  const updateZoom = useCallback(
    (tick: number) => {
      setZoom(currentZoom => Math.max(0.2, Math.min(2, currentZoom + tick)));
    },
    [setZoom]
  );

  const handleWheel = useCallback(
    (e: WheelEvent) => {
      updateZoom(e.deltaY * -0.001);
      e.stopPropagation();
    },
    [updateZoom]
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
    setMappingDetails(null);
  }, [setMappingDetails]);
  const showMappingDetails = useCallback(
    (mapping: string) => {
      setMappingDetails(mapping);
    },
    [setMappingDetails]
  );
  const sideBar = (
    <MappingDetails show={!!mappingDetails} onClose={closeMappingDetails}>
      {mappingDetails}
    </MappingDetails>
  );

  useEffect(() => {
    const timeout = setTimeout(measure, 150);
    return () => clearTimeout(timeout);
  }, [measure, mappingDetails]);

  const contextToolbar = useMemo(() => <MapperContextToolbar />, []);
  const viewToolbar = useMemo(() => <MapperViewToolbar />, []);
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
    <MapperProvider showMappingDetails={showMappingDetails}>
      <TopologyView
        contextToolbar={contextToolbar}
        viewToolbar={viewToolbar}
        controlBar={controlBar}
        sideBar={sideBar}
        sideBarOpen={!!mappingDetails}
      >
        <div
          ref={dimensionsRef}
          style={{ height: '100%', flex: '1' }}
          onWheel={handleWheel}
          {...bind(panX, panY)}
        >
          {width && (
            <Canvas
              width={width}
              height={height}
              zoom={zoom}
              panX={panX}
              panY={panY}
              isPanning={isPanning}
            >
              <SourceTargetMapper
                sources={sources}
                mappings={mappings}
                targets={targets}
              />
            </Canvas>
          )}
        </div>
      </TopologyView>
    </MapperProvider>
  );
};
