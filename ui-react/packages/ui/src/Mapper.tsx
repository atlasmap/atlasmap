import React, { FunctionComponent } from 'react';
import {
  TopologyView,
} from '@patternfly/react-topology';
import { MapperControlBar } from '@src/MapperControlBar';
import { MapperToolbar } from '@src/MapperToolbar';

export interface IMapperProps {

}

export const Mapper: FunctionComponent<IMapperProps> = () => {
  return (
    <TopologyView
      controlBar={<MapperControlBar />}
      viewToolbar={<MapperToolbar />}
    >
      todo
    </TopologyView>
  )
};