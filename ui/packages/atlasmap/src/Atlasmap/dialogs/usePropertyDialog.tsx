import { propertyTypes } from "@atlasmap/core";

import React, { useCallback, ReactElement, useState } from "react";

import { useToggle, PropertyDialog, IProperty } from "../../UI";
import { IAtlasmapDocument } from "../../Views";

type PropertyCallback = (property: IProperty) => void;

export function usePropertyDialog(
  title: string,
  scopeOptions: {
    value: string;
    label: string;
  }[],
): [
  ReactElement,
  (
    cb: PropertyCallback,
    properties: IAtlasmapDocument | null,
    property?: IProperty,
  ) => void,
] {
  const [onPropertyCb, setOnPropertyCb] = useState<PropertyCallback | null>(
    null,
  );
  const [initialProperty, setInitialProperty] = useState<IProperty | null>({
    name: "",
    valueType: propertyTypes[0][0],
    scope: scopeOptions[0].value,
  });
  const [properties, setProperties] = useState<IAtlasmapDocument | null>(null);
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
  function onValidation(name: string, scope: string): boolean {
    if (properties) {
      // Ensure proerty name/scope combination is unique
      const fields = properties.fields.filter(
        (fieldOrGroup) =>
          fieldOrGroup.name === name && fieldOrGroup.amField.scope === scope,
      );
      return (
        (name === initialProperty?.name && scope === initialProperty.scope) ||
        fields.length === 0
      );
    }
    return true;
  }
  const dialog = (
    <PropertyDialog
      title={title}
      valueTypeOptions={propertyTypes.map(([value, label]) => ({
        value,
        label,
      }))}
      scopeOptions={scopeOptions}
      isOpen={state}
      onCancel={toggleOff}
      onConfirm={onConfirm}
      onValidation={onValidation}
      {...(initialProperty || {})}
    />
  );
  const onOpenPropertyDialog = useCallback(
    (
      callback: PropertyCallback,
      properties: IAtlasmapDocument | null,
      property?: IProperty,
    ) => {
      // we use a closure to set the state here else React will think that callback
      // is the function to retrieve the state and will call it immediately.
      setOnPropertyCb(() => callback);
      if (property) {
        setInitialProperty(property);
      }
      // Set properties even if null to reset values when switching panels.
      setProperties(properties);
      toggleOn();
    },
    [toggleOn],
  );
  return [dialog, onOpenPropertyDialog];
}
