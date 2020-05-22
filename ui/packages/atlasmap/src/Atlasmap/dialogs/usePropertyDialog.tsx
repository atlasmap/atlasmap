import { propertyTypes } from "@atlasmap/core";

import React, { useCallback, ReactElement, useState } from "react";

import { useToggle, PropertyDialog, IProperty } from "../../UI";

type PropertyCallback = (property: IProperty) => void;

export function usePropertyDialog(
  title: string,
): [ReactElement, (cb: PropertyCallback, property?: IProperty) => void] {
  const [onPropertyCb, setOnPropertyCb] = useState<PropertyCallback | null>(
    null,
  );
  const [initialProperty, setInitialProperty] = useState<IProperty | null>(
    null,
  );
  const { state, toggleOn, toggleOff } = useToggle(false);
  const onConfirm = useCallback(
    (property: IProperty) => {
      if (onPropertyCb) {
        onPropertyCb(property);
        toggleOff();
      }
    },
    [onPropertyCb, toggleOff],
  );
  const dialog = (
    <PropertyDialog
      title={title}
      valueTypeOptions={propertyTypes.map(([value, label]) => ({
        value,
        label,
      }))}
      isOpen={state}
      onCancel={toggleOff}
      onConfirm={onConfirm}
      {...(initialProperty || {})}
    />
  );
  const onOpenPropertyDialog = useCallback(
    (callback: PropertyCallback, property?: IProperty) => {
      // we use a closure to set the state here else React will think that callback
      // is the function to retrieve the state and will call it immediately.
      setOnPropertyCb(() => callback);
      if (property) {
        setInitialProperty(property);
      }
      toggleOn();
    },
    [toggleOn],
  );
  return [dialog, onOpenPropertyDialog];
}
