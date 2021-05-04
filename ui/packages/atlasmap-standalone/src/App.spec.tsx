import * as serviceWorker from "./serviceWorker";

import App from "./App";
import { AtlasmapProvider } from "@atlasmap/atlasmap";
import React from "react";
import ReactDOM from "react-dom";

describe("AtlasMap standalone", () => {
  it("renders without crashing", () => {
    const root = document.createElement("div");
    root.id = "root";
    document.body.appendChild(root);
    const modals = document.createElement("div");
    modals.id = "modals";
    document.body.appendChild(modals);
    ReactDOM.render(
      <AtlasmapProvider
        baseJavaInspectionServiceUrl={"/v2/atlas/java/"}
        baseXMLInspectionServiceUrl={"/v2/atlas/xml/"}
        baseJSONInspectionServiceUrl={"/v2/atlas/json/"}
        baseCSVInspectionServiceUrl={"/v2/atlas/csv/"}
        baseMappingServiceUrl={"/v2/atlas/"}
      >
        <App />
      </AtlasmapProvider>,
      root
    );
    serviceWorker.unregister();
    ReactDOM.unmountComponentAtNode(root);
  });
});
