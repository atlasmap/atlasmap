import React, { ReactElement, useCallback, useState } from "react";
import {
  ConfirmationDialog,
  LookupTableDialog,
  LookupTableData,
  useToggle,
} from "../../UI";
import { getEnumerationValues, updateEnumerationValues } from "../utils";

type LookupTableCallback = () => void;

/**
 * Enumeration mapping occurs through a "lookup" table.
 */
export function useEditMappingEnumerationDialog(): [
  ReactElement,
  (cb: LookupTableCallback) => void,
] {
  const { state, toggleOn, toggleOff } = useToggle(false);

  const [enumerationValues, setEnumerationValues] = useState<
    LookupTableData[] | null
  >([]);

  const getEnumValues = () => {
    setEnumerationValues(getEnumerationValues());
  };

  const onConfirm = useCallback(() => {
    updateEnumerationValues(enumerationValues!);
    toggleOff();
  }, [enumerationValues, toggleOff]);

  const dialog = (
    <ConfirmationDialog
      title={"Map Enumeration Values"}
      description={"Map enumeration source values to target values."}
      onCancel={toggleOff}
      onConfirm={onConfirm}
      isOpen={state}
    >
      {state &&
        enumerationValues &&
        enumerationValues.map((value, idx) => (
          <div key={idx}>
            <LookupTableDialog
              enumerationValue={value}
              sourceKey={idx}
              isOpen={state}
            />
          </div>
        ))}
    </ConfirmationDialog>
  );

  const onOpenDialog = useCallback(() => {
    getEnumValues();
    toggleOn();
  }, [toggleOn]);

  return [dialog, onOpenDialog];
}
