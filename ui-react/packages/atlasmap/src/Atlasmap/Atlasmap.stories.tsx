import { text, boolean } from "@storybook/addon-knobs";
import React from "react";
import { Atlasmap } from "./Atlasmap";
import { AtlasmapProvider } from "./AtlasmapProvider";

export default {
  title: "Atlasmap|Demo",
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
    baseMappingServiceUrl={text(
      "baseMappingServiceUrl",
      "http://localhost:8585/v2/atlas/",
    )}
  >
    <Atlasmap
      showImportAtlasFileToolbarItem={boolean(
        "showImportAtlasFileToolbarItem",
        true,
      )}
      showExportAtlasFileToolbarItem={boolean(
        "showExportAtlasFileToolbarItem",
        true,
      )}
      showResetToolbarItem={boolean("showResetToolbarItem", true)}
      showToggleMappingPreviewToolbarItem={boolean(
        "showToggleMappingPreviewToolbarItem",
        true,
      )}
      showToggleMappingColumnToolbarItem={boolean(
        "showToggleMappingColumnToolbarItem",
        true,
      )}
      showMappingTableViewToolbarItem={boolean(
        "showToggleMappingTableToolbarItem",
        true,
      )}
      showNamespaceTableViewToolbarItem={boolean(
        "showToggleNamespaceTableToolbarItem",
        true,
      )}
      showToggleTypesToolbarItem={boolean("showToggleTypesToolbarItem", true)}
      showToggleMappedFieldsToolbarItem={boolean(
        "showToggleMappedFieldsToolbarItem",
        true,
      )}
      showToggleUnmappedFieldsToolbarItem={boolean(
        "showToggleUnmappedFieldsToolbarItem",
        true,
      )}
      showFreeViewToolbarItem={boolean("showToggleFreeViewToolbarItem", true)}
    />
  </AtlasmapProvider>
);

// import { action } from "@storybook/addon-actions";
// import { boolean } from "@storybook/addon-knobs";
// import React, { createElement, useState } from "react";
// import { mappings as sampleMappings, sources, targets } from "stories/sampleData";
// import { Button } from "@patternfly/react-core";
// import {
//   AtlasmapCanvasView,
//   AtlasmapCanvasViewMappings,
//   AtlasmapCanvasViewSource,
//   AtlasmapCanvasViewTarget,
//   AtlasmapUIProvider,
//   IAtlasmapField,
//   IMapping,
// } from "UI";

// export default {
//   title: "Atlasmap|Mapper",
// };

// export const sample = () =>
//   createElement(() => {
//     const [mappings, setMappings] = useState<IMapping[]>(sampleMappings);
//     const addToMapping = (node: IAtlasmapField, mapping: IMapping) => {
//       const updatedMappings = mappings.map((m) => {
//         if (m.id === mapping.id) {
//           if (node.id.includes("source")) {
//             m.sourceFields = [...m.sourceFields, { id: node.id }];
//           } else {
//             m.targetFields = [...m.targetFields, { id: node.id }];
//           }
//         }
//         return m;
//       });
//       setMappings(updatedMappings);
//     };

//     const createMapping = (source: IAtlasmapField, target: IAtlasmapField) => {
//       setMappings([
//         ...mappings,
//         {
//           id: `${Date.now()}`,
//           name: "One to One (mock)",
//           sourceFields: [{ id: source.id }],
//           targetFields: [{ id: target.id }],
//         },
//       ]);
//     };

//     const conditionalMappingExpressionEnabled = () => {
//       return false;
//     };

//     const getMappingExpression = () => {
//       return "";
//     };
//     const mappingExprClearText = (
//       nodeId?: string,
//       startOffset?: number,
//       endOffset?: number,
//     ) => {
//       if (nodeId || startOffset || endOffset) {
//         return "startOffset";
//       } else {
//         return "";
//       }
//     };
//     const mappingExprEmpty = () => {
//       return false;
//     };
//     const mappingExprInit = () => {};
//     const mappingExpressionInsertText = (
//       str: string,
//       nodeId?: string,
//       offset?: number,
//     ) => {
//       if (nodeId || offset) {
//         console.log(str);
//       }
//     };
//     const mappingExpressionObservable = () => {};
//     const mappingExpressionRemoveField = () => {};
//     const trailerID = "";

//     return (
//       <AtlasmapUIProvider
//         error={boolean("error", false)}
//         pending={boolean("pending", false)}
//         sources={sources}
//         targets={targets}
//         mappings={mappings}
//         onActiveMappingChange={action("onActiveMappingChange")}
//         renderMappingDetails={({ mapping, closeDetails }) => (
//           <>
//             {mapping.name}
//             <Button onSelect={closeDetails}>Close</Button>
//           </>
//         )}
//       >
//         <AtlasmapCanvasView
//           onShowMappingPreview={action("AtlasmapCanvasView")}
//           onShowMappedFields={action("onShowMappedFields")}
//           onShowUnmappedFields={action("onShowUnmappedFields")}
//           onExportAtlasFile={action("onExportAtlasFile")}
//           onImportAtlasFile={action("onImportAtlasFile")}
//           onResetAtlasmap={action("onResetAtlasmap")}
//           onAddMapping={action("onAddMapping")}
//           onConditionalMappingExpressionEnabled={
//             conditionalMappingExpressionEnabled
//           }
//           onGetMappingExpression={getMappingExpression}
//           mappingExpressionClearText={mappingExprClearText}
//           mappingExpressionEmpty={mappingExprEmpty}
//           mappingExpressionInit={mappingExprInit}
//           mappingExpressionInsertText={mappingExpressionInsertText}
//           mappingExpressionObservable={mappingExpressionObservable}
//           mappingExpressionRemoveField={mappingExpressionRemoveField}
//           onToggleExpressionMode={action("onToggleExpressionMode")}
//           trailerId={trailerID}
//         >
//           {({ showTypes, showMappingPreview }) => (
//             <>
//               <AtlasmapCanvasViewSource
//                 onAddToMapping={addToMapping}
//                 onCreateConstant={action("onCreateConstant")}
//                 onDeleteConstant={action("onDeleteConstant")}
//                 onEditConstant={action("onEditConstant")}
//                 onCreateProperty={action("onCreateProperty")}
//                 onCreateMapping={createMapping}
//                 onDeleteProperty={action("onDeleteProperty")}
//                 onEditProperty={action("onEditProperty")}
//                 onDeleteDocument={action("onDeleteDocument")}
//                 onFieldPreviewChange={action("onFieldPreviewChange")}
//                 onImportDocument={action("onImportDocument")}
//                 onSearch={action("onSearch")}
//                 showMappingPreview={showMappingPreview}
//                 showTypes={showTypes}
//                 sources={sources}
//               />

//               <AtlasmapCanvasViewMappings />

//               <AtlasmapCanvasViewTarget
//                 onAddToMapping={addToMapping}
//                 onCreateMapping={createMapping}
//                 onDeleteDocument={action("onDeleteDocument")}
//                 onImportDocument={action("onImportDocument")}
//                 onSearch={action("onSearch")}
//                 showMappingPreview={showMappingPreview}
//                 showTypes={showTypes}
//                 targets={targets}
//               />
//             </>
//           )}
//         </AtlasmapCanvasView>
//       </AtlasmapUIProvider>
//     );
//   });
