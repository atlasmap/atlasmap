import { text, boolean } from "@storybook/addon-knobs";
import React from "react";
import { Atlasmap } from "./Atlasmap";
import { AtlasmapProvider, IAtlasmapProviderProps } from "./AtlasmapProvider";
import { action } from "@storybook/addon-actions";
import { html } from "../stories/htmlKnob";

const sampleExternalDocument = JSON.stringify(
  {
    documentId: "i-M5XxdCeWJ837juDzeM3z",
    initialMappings:
      '{"AtlasMapping":{"jsonType":"io.atlasmap.v2.AtlasMapping","dataSource":[{"jsonType":"io.atlasmap.json.v2.JsonDataSource","id":"i-M5XxQUHWJ837juDzeLrz","uri":"atlas:json:i-M5XxQUHWJ837juDzeLrz","dataSourceType":"SOURCE"},{"jsonType":"io.atlasmap.v2.DataSource","id":"-M5XxkSP4RiiMiYC85vx","uri":"atlas:java?className=io.syndesis.connector.slack.SlackPlainMessage","dataSourceType":"TARGET"}],"mappings":{"mapping":[{"jsonType":"io.atlasmap.v2.Mapping","id":"mapping.900920","inputField":[{"jsonType":"io.atlasmap.json.v2.JsonField","name":"name","path":"/body/name","fieldType":"STRING","docId":"i-M5XxQUHWJ837juDzeLrz","userCreated":false}],"outputField":[{"jsonType":"io.atlasmap.java.v2.JavaField","name":"message","path":"/message","fieldType":"STRING","docId":"-M5XxkSP4RiiMiYC85vx"}]}]},"name":"UI.0","lookupTables":{"lookupTable":[]},"constants":{"constant":[]},"properties":{"property":[]}}}',
    inputDocuments: [
      {
        dataShape: {
          name: "Request",
          description: "API request payload",
          kind: "json-schema",
          specification:
            '{"$schema":"http://json-schema.org/schema#","type":"object","$id":"io:syndesis:wrapped","properties":{"body":{"type":"object","description":"","properties":{"name":{"type":"string"}},"example":{"name":"gary"}}}}',
          metadata: { unified: "true" },
        },
        description: "API request payload",
        id: "i-M5XxQUHWJ837juDzeLrz",
        inspectionResult: "",
        inspectionSource:
          '{"$schema":"http://json-schema.org/schema#","type":"object","$id":"io:syndesis:wrapped","properties":{"body":{"type":"object","description":"","properties":{"name":{"type":"string"}},"example":{"name":"gary"}}}}',
        name: "1 - Request",
        showFields: true,
        documentType: "JSON",
        inspectionType: "SCHEMA",
      },
    ],
    outputDocument: {
      dataShape: {
        name: "Message",
        kind: "java",
        type: "io.syndesis.connector.slack.SlackPlainMessage",
        specification:
          '{"JavaClass":{"jsonType":"io.atlasmap.java.v2.JavaClass","collectionType":"NONE","path":"/","fieldType":"COMPLEX","modifiers":{"modifier":["PUBLIC"]},"className":"io.syndesis.connector.slack.SlackPlainMessage","canonicalClassName":"io.syndesis.connector.slack.SlackPlainMessage","primitive":false,"synthetic":false,"javaEnumFields":{"javaEnumField":[]},"javaFields":{"javaField":[{"jsonType":"io.atlasmap.java.v2.JavaField","path":"/message","status":"SUPPORTED","fieldType":"STRING","modifiers":{"modifier":["PRIVATE"]},"name":"message","className":"java.lang.String","canonicalClassName":"java.lang.String","getMethod":"getMessage","setMethod":"setMessage","primitive":true,"synthetic":false}]},"packageName":"io.syndesis.connector.slack","annotation":false,"annonymous":false,"enumeration":false,"localClass":false,"memberClass":false,"uri":"atlas:java?className=io.syndesis.connector.slack.SlackPlainMessage","interface":false}}',
      },
      description: "",
      id: "-M5XxkSP4RiiMiYC85vx",
      inspectionResult:
        '{"JavaClass":{"jsonType":"io.atlasmap.java.v2.JavaClass","collectionType":"NONE","path":"/","fieldType":"COMPLEX","modifiers":{"modifier":["PUBLIC"]},"className":"io.syndesis.connector.slack.SlackPlainMessage","canonicalClassName":"io.syndesis.connector.slack.SlackPlainMessage","primitive":false,"synthetic":false,"javaEnumFields":{"javaEnumField":[]},"javaFields":{"javaField":[{"jsonType":"io.atlasmap.java.v2.JavaField","path":"/message","status":"SUPPORTED","fieldType":"STRING","modifiers":{"modifier":["PRIVATE"]},"name":"message","className":"java.lang.String","canonicalClassName":"java.lang.String","getMethod":"getMessage","setMethod":"setMessage","primitive":true,"synthetic":false}]},"packageName":"io.syndesis.connector.slack","annotation":false,"annonymous":false,"enumeration":false,"localClass":false,"memberClass":false,"uri":"atlas:java?className=io.syndesis.connector.slack.SlackPlainMessage","interface":false}}',
      inspectionSource: "io.syndesis.connector.slack.SlackPlainMessage",
      name: "2 - Message",
      showFields: true,
      documentType: "JAVA",
      inspectionType: "JAVA_CLASS",
    },
  },
  null,
  2,
);

export default {
  title: "AtlasMap|Demo",
};

export const wiredToTheBackend = () => (
  <AtlasmapProvider
    baseJavaInspectionServiceUrl={text(
      "baseJavaInspectionServiceUrl",
      "http://localhost:8585/v2/atlas/java/",
    )}
    baseXMLInspectionServiceUrl={text(
      "baseXMLInspectionServiceUrl",
      "http://localhost:8585/v2/atlas/xml/",
    )}
    baseJSONInspectionServiceUrl={text(
      "baseJSONInspectionServiceUrl",
      "http://localhost:8585/v2/atlas/json/",
    )}
    baseCSVInspectionServiceUrl={text(
      "baseCSVInspectionServiceUrl",
      "http://localhost:8585/v2/atlas/csv/",
    )}
    baseMappingServiceUrl={text(
      "baseMappingServiceUrl",
      "http://localhost:8585/v2/atlas/",
    )}
    onMappingChange={action("onMappingChange")}
  >
    <Atlasmap
      allowImport={boolean("allow Import", true)}
      allowExport={boolean("allow Export", true)}
      allowReset={boolean("allow Reset", true)}
      allowDelete={boolean("allow Delete", true)}
      allowCustomJavaClasses={boolean("allow Custom Java Classes", true)}
      toolbarOptions={{
        showToggleMappingPreviewToolbarItem: boolean(
          "showToggleMappingPreviewToolbarItem",
          true,
        ),
        showToggleMappingColumnToolbarItem: boolean(
          "showToggleMappingColumnToolbarItem",
          false,
        ),
        showMappingTableViewToolbarItem: boolean(
          "showToggleMappingTableToolbarItem",
          true,
        ),
        showNamespaceTableViewToolbarItem: boolean(
          "showToggleNamespaceTableToolbarItem",
          true,
        ),
        showToggleTypesToolbarItem: boolean("showToggleTypesToolbarItem", true),
        showToggleMappedFieldsToolbarItem: boolean(
          "showToggleMappedFieldsToolbarItem",
          true,
        ),
        showToggleUnmappedFieldsToolbarItem: boolean(
          "showToggleUnmappedFieldsToolbarItem",
          true,
        ),
        showFreeViewToolbarItem: boolean(
          "showToggleFreeViewToolbarItem",
          false,
        ),
      }}
    />
  </AtlasmapProvider>
);

export const embeddedInSyndesis = () => {
  const externalDocumentFromKnob = html(
    "External document",
    sampleExternalDocument,
  );
  let externalDocument: IAtlasmapProviderProps["externalDocument"];
  try {
    externalDocument = JSON.parse(externalDocumentFromKnob);
  } catch (e) {
    // do nothing
  }
  return (
    <AtlasmapProvider
      baseJavaInspectionServiceUrl={text(
        "baseJavaInspectionServiceUrl",
        "http://localhost:8585/v2/atlas/java/",
      )}
      baseXMLInspectionServiceUrl={text(
        "baseXMLInspectionServiceUrl",
        "http://localhost:8585/v2/atlas/xml/",
      )}
      baseJSONInspectionServiceUrl={text(
        "baseJSONInspectionServiceUrl",
        "http://localhost:8585/v2/atlas/json/",
      )}
      baseCSVInspectionServiceUrl={text(
        "baseCSVInspectionServiceUrl",
        "http://localhost:8585/v2/atlas/csv/",
      )}
      baseMappingServiceUrl={text(
        "baseMappingServiceUrl",
        "http://localhost:8585/v2/atlas/",
      )}
      externalDocument={externalDocument}
      onMappingChange={action("onMappingChange")}
    >
      <Atlasmap
        allowImport={false}
        allowExport={false}
        allowReset={false}
        allowDelete={false}
        allowCustomJavaClasses={false}
      />
    </AtlasmapProvider>
  );
};
