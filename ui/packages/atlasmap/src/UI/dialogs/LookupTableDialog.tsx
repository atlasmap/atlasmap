import React, { FunctionComponent, useState } from "react";

import {
  Form,
  FormGroup,
  FormSelect,
  FormSelectOption,
  Label,
} from "@patternfly/react-core";

import { IConfirmationDialogProps } from "./ConfirmationDialog";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  iGroup: {
    marginBottom: "1.0rem",
    marginLeft: "0.5rem",
    width: 500,
  },
  iSelectBody: {
    fontSize: 14,
    width: 250,
  },
  iSelectLabel: {
    fontSize: 14,
    marginTop: "0.2rem",
    width: 225,
    marginRight: "0.5rem",
  },
});

export type LookupTableData = {
  sourceEnumValue: string;
  targetEnumValues: string[];
  selectedTargetEnumValue: string;
};
export interface ILookupTableDialogProps {
  enumerationValue: LookupTableData;
  sourceKey: number;
  isOpen: IConfirmationDialogProps["isOpen"];
}

export const LookupTableDialog: FunctionComponent<ILookupTableDialogProps> = ({
  enumerationValue,
  sourceKey,
  isOpen,
}) => {
  const [targetEnum, setTargetEnum] = useState(
    enumerationValue.selectedTargetEnumValue,
  );

  const onChangeTargetEnum = (
    enumValue: string,
    _event: React.FormEvent<HTMLSelectElement>,
  ) => {
    setTargetEnum(enumValue);
    enumerationValue.selectedTargetEnumValue = enumValue;
  };

  return (
    <Form>
      <FormGroup className={css(styles.iGroup)} fieldId={"lookup-table-row"}>
        <Label className={css(styles.iSelectLabel)}>
          {enumerationValue.sourceEnumValue}
        </Label>
        <FormSelect
          className={css(styles.iSelectBody)}
          value={targetEnum}
          aria-label={"enum-map"}
          autoFocus={true}
          onChange={onChangeTargetEnum}
          data-testid={"enum-map-select"}
          key={`${targetEnum}-${sourceKey}`}
        >
          {isOpen &&
            enumerationValue.targetEnumValues &&
            enumerationValue.targetEnumValues.map((value, idx) => (
              <FormSelectOption
                key={`tgtenum-${idx}`}
                value={value}
                label={value}
              />
            ))}
        </FormSelect>
      </FormGroup>
    </Form>
  );
};
