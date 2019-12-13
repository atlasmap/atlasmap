import React, { FunctionComponent } from 'react';
import { useAtlasmapUI } from './AtlasmapUIProvider';
import { TopologySideBar } from '@patternfly/react-topology';

export const AtlasmapSidebar: FunctionComponent = () => {
  const {
    isEditingMapping,
    currentMapping,
    renderMappingDetails,
    closeMappingDetails,
  } = useAtlasmapUI();

  const show = isEditingMapping && currentMapping !== null;

  return (
    <TopologySideBar show={show} onClose={closeMappingDetails}>
      {show && renderMappingDetails(currentMapping!)}
    </TopologySideBar>
  );
};
