import { Atlasmap } from "@atlasmap/atlasmap";
import { Page, PageHeader, PageSection } from "@patternfly/react-core";
import React  from "react";

const App: React.FC = () => {
  return (
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
        <Atlasmap />
      </PageSection>
    </Page>
  );
};

export default App;
