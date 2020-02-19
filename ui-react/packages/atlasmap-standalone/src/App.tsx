import { Atlasmap, useAtlasmapDialogs } from "@atlasmap/core";
import { Page, PageHeader, PageSection } from "@patternfly/react-core";
import React  from "react";

const App: React.FC = () => {
  const { handlers, dialogs } = useAtlasmapDialogs({ modalContainer: document.getElementById('modals')!})

  return (
    <>
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
    </>
  );
};

export default App;
