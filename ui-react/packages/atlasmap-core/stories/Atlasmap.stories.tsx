import { Page, PageHeader, PageSection } from "@patternfly/react-core";
import React, { useCallback } from "react";
import { Atlasmap, AtlasmapProvider } from "../src";
import { useAtlasmapDialogs } from "../src/react/useAtlasmapDialogs";

export default {
  title: 'Atlasmap',
};

export const Standalone = () => {
  const { handlers, dialogs } = useAtlasmapDialogs({ modalContainer: document.getElementById('modals')!})
  return (
    <AtlasmapProvider
      baseJavaInspectionServiceUrl={'http://localhost:8585/v2/atlas/java/'}
      baseXMLInspectionServiceUrl={'http://localhost:8585/v2/atlas/xml/'}
      baseJSONInspectionServiceUrl={'http://localhost:8585/v2/atlas/json/'}
      baseMappingServiceUrl={'http://localhost:8585/v2/atlas/'}
    >
      <Page
        header={
          <PageHeader
            logo={
              <>
                <strong>Atlasmap</strong>&nbsp;Data Mapper UI
              </>
            }
            style={{ minHeight: 40 }}
          />
        }
      >
        <PageSection variant={"light"} noPadding={true} isFilled={true}>
          <Atlasmap {...handlers} />
        </PageSection>
      </Page>
      {dialogs}
    </AtlasmapProvider>
  );
};


export const Syndesis = () => {
  const { handlers, dialogs } = useAtlasmapDialogs({ modalContainer: document.getElementById('modals')!})

  return (
    <AtlasmapProvider
      baseJavaInspectionServiceUrl={'http://localhost:8585/v2/atlas/java/'}
      baseXMLInspectionServiceUrl={'http://localhost:8585/v2/atlas/xml/'}
      baseJSONInspectionServiceUrl={'http://localhost:8585/v2/atlas/json/'}
      baseMappingServiceUrl={'http://localhost:8585/v2/atlas/'}
    >
      <Atlasmap {...handlers} />
      {dialogs}
    </AtlasmapProvider>
  );
};

