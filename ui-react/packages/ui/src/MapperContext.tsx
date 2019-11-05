import React from 'react';
import { createContext, FunctionComponent, useContext } from 'react';

export interface IMapperContext {
  showMappingDetails: (mapping: string) => void
}
const MapperContext = createContext<IMapperContext | null>(null);

export const MapperProvider: FunctionComponent<IMapperContext> = ({ showMappingDetails, children }) => (
  <MapperContext.Provider value={{ showMappingDetails }}>
    {children}
  </MapperContext.Provider>
);

export function useMappingDetails() {
  const ctx = useContext(MapperContext);
  if (!ctx) {
    throw new Error(`Couldn't find a MapperProvider.`);
  }

  return ctx.showMappingDetails;
}