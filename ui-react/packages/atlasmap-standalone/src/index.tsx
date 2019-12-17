import './index.css';
import { AtlasmapProvider } from '@atlasmap/core';
import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import * as serviceWorker from './serviceWorker';

ReactDOM.render(
  <AtlasmapProvider
    baseJavaInspectionServiceUrl={'/v2/atlas/java/'}
    baseXMLInspectionServiceUrl={'/v2/atlas/xml/'}
    baseJSONInspectionServiceUrl={'/v2/atlas/json/'}
    baseMappingServiceUrl={'/v2/atlas/'}
  >
    <App />
  </AtlasmapProvider>,
  document.getElementById('root')
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
