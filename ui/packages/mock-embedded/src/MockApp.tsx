/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import { Brand, Page, PageHeader, PageSection } from '@patternfly/react-core';
import { DocumentType, InspectionType } from '@atlasmap/core';

import { DataMapperAdapter } from './DataMapperAdapter';
import React from 'react';
import atlasMappingJson from './files/atlasmapping-UI.0.json';
import atlasmapLogo from './logo-horizontal-darkbg.png';
import jsonSchemaSourceJson from './files/JSONSchemaSource.json';
import twitter4jStatusInspectedJson from './files/atlasmap-inspection-twitter4j.Status.json';
import xmlSchemaSourceInspectedJson from './files/inspected-XMLSchemaSource.json';

let receivedMappings: string;

const MockApp: React.FC = () => {
  return (
    <Page
      header={
        <PageHeader
          logo={
            <>
              <Brand
                src={atlasmapLogo}
                alt="AtlasMap Data Mapper UI"
                height="40"
              />
            </>
          }
          style={{ minHeight: 40 }}
        />
      }
    >
      <PageSection
        variant={'light'}
        padding={{ default: 'noPadding' }}
        isFilled={true}
      >
        <DataMapperAdapter
          baseJavaInspectionServiceUrl={'/v2/atlas/java/'}
          baseXMLInspectionServiceUrl={'/v2/atlas/xml/'}
          baseJSONInspectionServiceUrl={'/v2/atlas/json/'}
          baseCSVInspectionServiceUrl={'/v2/atlas/csv/'}
          baseMappingServiceUrl={'/v2/atlas/'}
          onMappings={function (mappings: string): void {
            receivedMappings = mappings;
          }}
          documentId={'MockDocuments'}
          inputDocuments={[
            {
              id: 'JSONSchemaSource-a3192b3f-190e-46ca-b679-4222ebde5355',
              name: 'JSONSchemaSource',
              description: 'Source document JSONSchemaSource type: JSON',
              documentType: DocumentType.JSON,
              inspectionType: InspectionType.SCHEMA,
              inspectionSource: JSON.stringify(jsonSchemaSourceJson),
              inspectionParameters: {},
              inspectionResult: '',
              showFields: true,
            },
            {
              id: 'XMLSchemaSource-2ef7b947-fe0a-4871-b682-49611a58084d',
              name: 'XMLSchemaSource',
              description: 'Source document XMLSchemaSource type: XML',
              documentType: DocumentType.XML,
              inspectionType: InspectionType.SCHEMA,
              inspectionSource: ``,
              inspectionParameters: {},
              inspectionResult: JSON.stringify(xmlSchemaSourceInspectedJson),
              showFields: true,
            },
            {
              id: 'twitter4j.Status',
              name: 'Status',
              description: 'Java document class twitter4j.Status',
              documentType: DocumentType.JAVA,
              inspectionType: InspectionType.JAVA_CLASS,
              inspectionSource: 'twitter4j.Status',
              inspectionParameters: {},
              inspectionResult: JSON.stringify(twitter4jStatusInspectedJson),
              showFields: true,
            },
          ]}
          outputDocument={{
            id: 'JSONSchemaSource-d7df54bc-e51d-43f2-a715-d9a77d425a26',
            name: 'JSONSchemaSource',
            description: 'Target document JSONSchemaSource type: JSON',
            documentType: DocumentType.JSON,
            inspectionType: InspectionType.SCHEMA,
            inspectionSource: JSON.stringify(jsonSchemaSourceJson),
            inspectionParameters: {},
            inspectionResult: '',
            showFields: true,
          }}
          initialMappings={JSON.stringify(atlasMappingJson)}
        />
      </PageSection>
      <input type="hidden" value={receivedMappings} />
    </Page>
  );
};

export default MockApp;
