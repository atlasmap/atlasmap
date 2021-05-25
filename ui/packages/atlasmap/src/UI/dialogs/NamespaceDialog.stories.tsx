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
import { boolean, text } from '@storybook/addon-knobs';

import { NamespaceDialog } from './NamespaceDialog';
import React from 'react';
import { action } from '@storybook/addon-actions';

export default {
  title: 'UI|Dialogs',
  component: NamespaceDialog,
};

export const namespaceDialog = () => (
  <NamespaceDialog
    title={text('Title', 'Namespace dialog title')}
    isOpen={boolean('Is open', true)}
    onCancel={action('onCancel')}
    onConfirm={action('onConfirm')}
    alias={text('Initial alias', '')}
    uri={text('Initial uri', '')}
    locationUri={text('Initial locationUri', '')}
    targetNamespace={boolean('Initial targetNamespace', false)}
  />
);
