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
import React, { ReactElement, useCallback, useState } from 'react';

import { AboutDialog } from '../../UI';
import { useAtlasmap } from '../AtlasmapProvider';
import { useToggle } from '../utils';

export function useAboutDialog(): [ReactElement, () => void] {
  const { getRuntimeVersion, getUIVersion } = useAtlasmap();
  const [runtimeVersion, setRuntimeVersion]: [
    string,
    (version: string) => void,
  ] = useState('0.0');
  const { state, toggleOn, toggleOff } = useToggle(false);

  const dialog = (
    <AboutDialog
      title="AtlasMap Data Mapper"
      isOpen={state}
      onClose={toggleOff}
      uiVersion={getUIVersion()}
      runtimeVersion={runtimeVersion}
    />
  );

  const onAboutDialog = useCallback(() => {
    getRuntimeVersion()
      .then((body: any) => {
        setRuntimeVersion(body);
        toggleOn();
      })
      .catch((error) => {
        setRuntimeVersion(error);
        toggleOn();
      });
  }, [toggleOn, getRuntimeVersion]);
  return [dialog, onAboutDialog];
}
