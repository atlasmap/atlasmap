import { CustomClassDialog, ICustomClass } from "../../UI";
import React, { ReactElement, useCallback, useState } from "react";

import { collectionTypes } from "@atlasmap/core";
import { getCustomClassNameOptions } from "../utils/document";
import { useToggle } from "../../Atlasmap/utils";

type CustomClassCallback = (constant: ICustomClass) => void;

export function useCustomClassDialog(
  title: string,
): [ReactElement, (cb: CustomClassCallback, constant?: ICustomClass) => void] {
  const [onCustomClassCb, setOnCustomClassCb] =
    useState<CustomClassCallback | null>(null);

  const [initialCustomClass, setInitialCustomClass] =
    useState<ICustomClass | null>(null);

  const [customClassNames, setCustomClassNames] = useState<string[] | null>([]);

  const { state, toggleOn, toggleOff } = useToggle(false);
  const onConfirm = useCallback(
    (constant: ICustomClass) => {
      if (onCustomClassCb) {
        onCustomClassCb(constant);
        toggleOff();
      }
    },
    [onCustomClassCb, toggleOff],
  );

  const getCustomClassNames = async () => {
    setCustomClassNames(await getCustomClassNameOptions());
  };

  const dialog = (
    <CustomClassDialog
      title={title}
      isOpen={state}
      customClassName={customClassNames ? customClassNames[0] : ""}
      customClassNames={customClassNames}
      collectionTypeOptions={collectionTypes.map(([value, label]) => ({
        value,
        label,
      }))}
      onCancel={toggleOff}
      onConfirm={onConfirm}
      {...(initialCustomClass || { collectionType: "NONE" })}
    />
  );
  const onOpenCustomClassDialog = useCallback(
    (callback: CustomClassCallback, constant?: ICustomClass) => {
      // we use a closure to set the state here else React will think that callback
      // is the function to retrieve the state and will call it immediately.
      setOnCustomClassCb(() => callback);
      if (constant) {
        setInitialCustomClass(constant);
      }
      getCustomClassNames();
      toggleOn();
    },
    [toggleOn],
  );
  return [dialog, onOpenCustomClassDialog];
}
