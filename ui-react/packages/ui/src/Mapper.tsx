import { MapperProvider } from '@src/MapperContext';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useState,
} from 'react';
import { TopologyView } from '@patternfly/react-topology';
import { MapperToolbar } from '@src/MapperToolbar';
import { SourceTargetMapper } from '@src/SourceTargetMapper';
import { useDimensions } from '@src/useDimensions';
import { MappingDetails } from '@src/MappingDetails';

export interface IMapperProps {}

export const Mapper: FunctionComponent<IMapperProps> = () => {
  const [ref, { width, height }, measure] = useDimensions();
  const [mappingDetails, setMappingDetails] = useState<string | null>(null);

  const closeMappingDetails = useCallback(() => setMappingDetails(null), [
    setMappingDetails,
  ]);
  const showMappingDetails = useCallback(
    (mapping: string) => setMappingDetails(mapping),
    [setMappingDetails]
  );
  const sideBar = (
    <MappingDetails show={!!mappingDetails} onClose={closeMappingDetails}>
      {mappingDetails}
    </MappingDetails>
  );

  useEffect(() => {
    const timeout = setTimeout(() => measure(), 150);
    return () => {
      clearTimeout(timeout);
    };
  }, [measure, mappingDetails]);

  return (
    <MapperProvider showMappingDetails={showMappingDetails}>
      <TopologyView
        viewToolbar={<MapperToolbar />}
        sideBar={sideBar}
        sideBarOpen={!!mappingDetails}
      >
        <div ref={ref} style={{ height: '100%', flex: '1' }}>
          {width && <SourceTargetMapper width={width} height={height} />}
        </div>
      </TopologyView>
    </MapperProvider>
  );
};
