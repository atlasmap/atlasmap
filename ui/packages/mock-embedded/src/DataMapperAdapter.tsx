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
import * as React from 'react';

import {
  Atlasmap,
  AtlasmapProvider,
  IAtlasmapProviderProps,
  IExternalDocumentProps,
  ParametersDialog,
} from '@atlasmap/atlasmap';
import { getCsvParameterOptions } from '@atlasmap/core';

export interface IDataMapperAdapterProps {
  documentId: string;
  inputDocuments: IExternalDocumentProps[];
  outputDocument: IExternalDocumentProps;
  initialMappings?: string;
  baseMappingServiceUrl: string;
  baseJavaInspectionServiceUrl: string;
  baseXMLInspectionServiceUrl: string;
  baseJSONInspectionServiceUrl: string;
  baseCSVInspectionServiceUrl: string;
  onMappings(mappings: string): void;
}

export interface IParameter {
  name: string;
  label: string;
  value: string;
  boolean?: boolean;
  options?: IParameterOption[];
  enabled?: boolean;
  required?: boolean;
}

export const DataMapperAdapter: React.FunctionComponent<
  IDataMapperAdapterProps
> = ({
  documentId,
  inputDocuments,
  outputDocument,
  initialMappings,
  baseMappingServiceUrl,
  baseJavaInspectionServiceUrl,
  baseXMLInspectionServiceUrl,
  baseJSONInspectionServiceUrl,
  baseCSVInspectionServiceUrl,
  onMappings,
}) => {
  const externalDocument = React.useMemo(
    () =>
      ({
        documentId,
        initialMappings,
        inputDocuments,
        outputDocument,
      } as IAtlasmapProviderProps['externalDocument']),
    [initialMappings, documentId, inputDocuments, outputDocument],
  );
  return (
    <AtlasmapProvider
      logLevel={'warn'}
      baseMappingServiceUrl={baseMappingServiceUrl}
      baseJSONInspectionServiceUrl={baseJSONInspectionServiceUrl}
      baseJavaInspectionServiceUrl={baseJavaInspectionServiceUrl}
      baseXMLInspectionServiceUrl={baseXMLInspectionServiceUrl}
      baseCSVInspectionServiceUrl={baseCSVInspectionServiceUrl}
      externalDocument={externalDocument}
      onMappingChange={onMappings}
    >
      <Atlasmap
        allowImport={false}
        allowExport={false}
        allowDelete={false}
        allowCustomJavaClasses={false}
      />
    </AtlasmapProvider>
  );
};

export interface IParameterOption {
  label: string;
  value: string;
}

export interface IParameterDefinition {
  name: string;
  label: string;
  value: string;
  boolean?: boolean;
  options?: IParameterOption[];
  hidden?: boolean;
  required?: boolean;
  enabled?: boolean;
}

export interface IParameters {
  [name: string]: string;
}

export const DataShapeParametersDialog: React.FunctionComponent<{
  title: string;
  shown: boolean;
  parameterDefinition: IParameterDefinition[];
  parameters?: IParameters;
  onConfirm: (parameters: IParameters) => void;
  onCancel: () => void;
}> = ({
  title,
  shown,
  parameterDefinition,
  parameters,
  onConfirm,
  onCancel,
}) => {
  const parametersToParameterArray = (given?: IParameters): IParameter[] => {
    if (given === undefined) {
      return [];
    }

    return parameterDefinition.reduce((acc, defn) => {
      if (defn.name in given) {
        acc.push({ ...defn, value: given[defn.name] });
      }

      return acc;
    }, [] as IParameter[]);
  };

  const parameterArrayToParams = (given: IParameter[]): IParameters => {
    return given.reduce((acc: any, param) => {
      acc[param.name] = param.value;

      return acc;
    }, {});
  };

  // we wish to maintain the interface between usage of DataShapeParametersDialog
  // and AtlasMap, and hide any idiosyncrasies, to `onConfirm` we wish to provide
  // only key-value IParameters choosen by the user, while maintaining the state
  // of ParametersDialog in AtlasMap, as noted above
  const handleConfirm = (given: IParameter[]) => {
    setParams(parameterArrayToParams(given));
    onConfirm(parameterArrayToParams(given));
  };

  const [params, setParams] = React.useState(parameters);

  return (
    <ParametersDialog
      isOpen={shown}
      title={title}
      onCancel={onCancel}
      onConfirm={handleConfirm}
      initialParameters={parametersToParameterArray(params)}
      parameters={parameterDefinition}
    />
  );
};

export const atlasmapCSVParameterOptions = getCsvParameterOptions;
