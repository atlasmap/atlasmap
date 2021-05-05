import React, { ReactElement, useCallback, useState } from "react";

import { AboutDialog } from "../../UI";
import { useAtlasmap } from "../AtlasmapProvider";
import { useToggle } from "../../Atlasmap/utils";

export function useAboutDialog(): [ReactElement, () => void] {
  const { getRuntimeVersion, getUIVersion } = useAtlasmap();
  const uiVersion = getUIVersion();
  const [runtimeVersion, setRuntimeVersion]: [
    string,
    (version: string) => void,
  ] = useState("0.0");
  const { state, toggleOn, toggleOff } = useToggle(false);

  const dialog = (
    <AboutDialog
      title="AtlasMap Data Mapper"
      isOpen={state}
      onClose={toggleOff}
      uiVersion={uiVersion}
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
