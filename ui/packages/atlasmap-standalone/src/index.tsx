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
import './index.css';
import * as serviceWorker from './serviceWorker';
import App from './App';
import { AtlasmapProvider } from '@atlasmap/atlasmap';
import React from 'react';
import ReactDOM from 'react-dom';

ReactDOM.render(
  <AtlasmapProvider
    baseAtlasServiceUrl={'/v2/atlas/'}
    baseCSVInspectionServiceUrl={'/v2/atlas/csv/'}
    baseJavaInspectionServiceUrl={'/v2/atlas/java/'}
    baseJSONInspectionServiceUrl={'/v2/atlas/json/'}
    baseKafkaConnectInspectionServiceUrl={'/v2/atlas/kafkaconnect/'}
    baseXMLInspectionServiceUrl={'/v2/atlas/xml/'}
    logLevel={'debug'}
  >
    <App />
  </AtlasmapProvider>,
  document.getElementById('root'),
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
