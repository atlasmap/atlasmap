import { Atlasmap } from "@atlasmap/atlasmap";
import { Brand, Page, PageHeader, PageSection } from "@patternfly/react-core";
import React  from "react";
import atlasmapLogo from "./logo-horizontal-darkbg.png";

const App: React.FC = () => {
  return (
    <Page
      header={
        <PageHeader
          logo={
            <>
              <Brand src={atlasmapLogo} alt="AtlasMap Data Mapper UI test2" height="40" />
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
