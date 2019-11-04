import React, {
  FunctionComponent,
  useCallback,
  useState,
  WheelEvent,
} from 'react';
import { TopologyView } from '@patternfly/react-topology';
import { MapperControlBar } from '@src/MapperControlBar';
import { MapperToolbar } from '@src/MapperToolbar';
import { MapperCanvas } from '@src/MapperCanvas';
import { useDimensions } from '@src/useDimensions';

export interface IMapperProps {}

export const Mapper: FunctionComponent<IMapperProps> = () => {
  const [ref, { width, height }] = useDimensions();
  const [zoom, setZoom] = useState(1);

  const updateZoom = useCallback((tick: number) => {
    setZoom((currentZoom) => currentZoom + tick)
  }, [zoom, setZoom]);

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

  return (
    <TopologyView
      controlBar={
        <MapperControlBar
          onZoomIn={handleZoomIn}
          onZoomOut={handleZoomOut}
          onZoomReset={handleZoomReset}
        />
      }
      viewToolbar={<MapperToolbar />}
      onWheel={handleWheel}
    >
      <div ref={ref} style={{ height: '100%', flex: '1' }}>
        {width && <MapperCanvas width={width} height={height} zoom={zoom} />}
      </div>
    </TopologyView>
  );
};
