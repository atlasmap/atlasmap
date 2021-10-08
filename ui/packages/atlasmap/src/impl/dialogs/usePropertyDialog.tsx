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
import { IProperty, PropertyDialog } from '../../UI';
import React, { ReactElement, useCallback, useState } from 'react';

import { IAtlasmapDocument } from '../../Views';
import { propertyTypes } from '@atlasmap/core';
import { useToggle } from '../utils';

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
    name: '',
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
    let fieldNameExists = false;
    if (properties) {
      // Ensure proerty name/scope combination is unique
      const fields = properties.fields.filter(
        (fieldOrGroup) =>
          fieldOrGroup.name === name && fieldOrGroup.amField.scope === scope,
      );
      fieldNameExists = fields.length > 0;
    }
    return (
      (name === initialProperty?.name && scope === initialProperty.scope) ||
      !fieldNameExists
    );
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
