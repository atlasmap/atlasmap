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
import * as serviceWorker from './serviceWorker';

import MockApp from './MockApp';
import React from 'react';
import ReactDOM from 'react-dom';

describe('AtlasMap embedded usage mockup', () => {
  it('renders without crashing', () => {
    const root = document.createElement('div');
    root.id = 'root';
    document.body.appendChild(root);
    const modals = document.createElement('div');
    modals.id = 'modals';
    document.body.appendChild(modals);
    ReactDOM.render(<MockApp />, root);
    serviceWorker.unregister();
    ReactDOM.unmountComponentAtNode(root);
  });
});
