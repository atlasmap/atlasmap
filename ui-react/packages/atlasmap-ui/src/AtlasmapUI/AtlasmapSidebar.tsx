import React, { FunctionComponent } from 'react';
import { useAtlasmapUI } from './AtlasmapUIProvider';
import { TopologySideBar } from '@patternfly/react-topology';
import { css, StyleSheet } from '@patternfly/react-styles';

const styles = StyleSheet.create({
  sidebar: {
    height: '100%',
    overflow: 'hidden',
    '& > .pf-topology-side-bar__body': {
      height: '100%',
    },
  },
});

export const AtlasmapSidebar: FunctionComponent = () => {
  const {
    isEditingMapping,
    currentMapping,
    renderMappingDetails,
    closeMappingDetails,
  } = useAtlasmapUI();

  const show = isEditingMapping && currentMapping !== null;

  return (
    <TopologySideBar show={show} className={css(styles.sidebar)}>
      {show && renderMappingDetails({
        mapping: currentMapping!,
        closeDetails: closeMappingDetails
      })}
    </TopologySideBar>
  );
};
