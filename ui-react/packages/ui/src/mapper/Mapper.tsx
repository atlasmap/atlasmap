import { TopologyView } from '@patternfly/react-topology';
import { Canvas } from '@src/canvas';
import { IFieldsGroup, IMappings, Coords } from '@src/models';
import { SourceTargetMapper } from '@src/views';
import { useDimensions } from '@src/common/useDimensions';
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
} from 'react';
import { useGesture } from 'react-use-gesture';

export interface IMapperProps {
  sources: IFieldsGroup[];
  targets: IFieldsGroup[];
  mappings: IMappings[];
}

export const Mapper: FunctionComponent<IMapperProps> = ({
  sources,
  mappings,
  targets,
}) => {
  const [freeView, setFreeView] = useState(false);
  const [dimensionsRef, { width, height }, measure] = useDimensions();
  const [mappingDetails, setMappingDetails] = useState<string | null>(null);

  const [zoom, setZoom] = useState(1);

  const [isPanning, setIsPanning] = useState(false);
  const [{ x: panX, y: panY }, setPan] = useState<Coords>({ x: 0, y: 0 });
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
  const toggleFreeView = useCallback(() => setFreeView(!freeView), [freeView, setFreeView]);
  const viewToolbar = useMemo(
    () => (
      <MapperViewToolbar freeView={freeView} toggleFreeView={toggleFreeView} />
    ),
    [freeView, toggleFreeView]
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
    <MapperProvider showMappingDetails={showMappingDetails}>
      <TopologyView
        contextToolbar={contextToolbar}
        viewToolbar={viewToolbar}
        controlBar={freeView ? controlBar : undefined}
        sideBar={sideBar}
        sideBarOpen={!!mappingDetails}
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
              isPanning={freeView ? isPanning : false}
            >
              <SourceTargetMapper
                sources={sources}
                mappings={mappings}
                targets={targets}
                freeView={freeView}
              />
            </Canvas>
          )}
        </div>
      </TopologyView>
    </MapperProvider>
  );
};
