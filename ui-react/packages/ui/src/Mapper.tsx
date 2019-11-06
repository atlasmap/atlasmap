import { MapperControlBar } from '@src/MapperControlBar';
import React, {
  FunctionComponent,
  useCallback, useEffect,
  useState,
  WheelEvent,
} from 'react';
import { TopologyView } from '@patternfly/react-topology';
import { SourceTargetMapper } from '@src/SourceTargetMapper';
import { useDimensions } from '@src/useDimensions';
import { MappingDetails } from '@src/MappingDetails';
import { MapperProvider } from '@src/MapperContext';
import { MapperViewToolbar } from '@src/MapperViewToolbar';
import { MapperContextToolbar } from '@src/MapperContextToolbar';

export interface IMapperProps {}

export const Mapper: FunctionComponent<IMapperProps> = () => {
  const [ref, { width, height }, measure] = useDimensions();
  const [mappingDetails, setMappingDetails] = useState<string | null>(null);

  const [zoom, setZoom] = useState(1);

  const updateZoom = useCallback(
    (tick: number) => {
      setZoom(currentZoom => currentZoom + tick);
    },
    [zoom, setZoom]
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
  const handleZoomReset = useCallback(() => {
    setZoom(1);
  }, [setZoom]);

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
    return () => clearTimeout(timeout)
  }, [measure, mappingDetails]);

  return (
    <MapperProvider showMappingDetails={showMappingDetails}>
      <TopologyView
        contextToolbar={<MapperContextToolbar />}
        viewToolbar={<MapperViewToolbar />}
        controlBar={
          <MapperControlBar
            onZoomIn={handleZoomIn}
            onZoomOut={handleZoomOut}
            onZoomReset={handleZoomReset}
          />
        }
        sideBar={sideBar}
        sideBarOpen={!!mappingDetails}
      >
        <div
          ref={ref}
          style={{ height: '100%', flex: '1' }}
          onWheel={handleWheel}
        >
          {width && (
            <SourceTargetMapper width={width} height={height} zoom={zoom} />
          )}
        </div>
      </TopologyView>
    </MapperProvider>
  );
};
