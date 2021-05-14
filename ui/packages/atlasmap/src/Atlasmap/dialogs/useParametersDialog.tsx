import { IParameter, ParametersDialog } from "../../UI";
import React, { ReactElement, useCallback, useState } from "react";

import { useToggle } from "../../Atlasmap/utils";

type ParametersCallback = (parameters: IParameter[]) => void;

export function useParametersDialog(
  title: string,
): [ReactElement, (cb: ParametersCallback, parameters?: IParameter[]) => void] {
  const [onParametersCb, setOnParametersCb] =
    useState<ParametersCallback | null>(null);
  const [parameters, setParameters] = useState<IParameter[]>([]);
  const { state, toggleOn, toggleOff } = useToggle(false);
  const onConfirm = useCallback(
    (parameters: IParameter[]) => {
      if (onParametersCb) {
        onParametersCb(parameters);
        toggleOff();
      }
    },
    [onParametersCb, toggleOff],
  );
  const dialog = (
    <ParametersDialog
      title={title}
      isOpen={state}
      onCancel={toggleOff}
      onConfirm={onConfirm}
      parameters={parameters}
    />
  );
  const onOpenParametersDialog = useCallback(
    (callback: ParametersCallback, parameters?: IParameter[]) => {
      setOnParametersCb(() => callback);
      if (parameters) {
        setParameters(parameters);
      }
      toggleOn();
    },
    [toggleOn],
  );
  return [dialog, onOpenParametersDialog];
}
