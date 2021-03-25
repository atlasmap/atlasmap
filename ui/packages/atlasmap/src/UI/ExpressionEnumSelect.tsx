import { FunctionComponent } from "react";
import React from "react";
import {
  Select,
  SelectOption,
  SelectOptionObject,
} from "@patternfly/react-core";
import { StyleSheet, css } from "@patternfly/react-styles";
import { useToggle } from "../Atlasmap/utils";
import { EnumValue } from "../../src/Atlasmap/utils";

const styles = StyleSheet.create({
  document: {
    fontWeight: "bold",
  },
  field: {
    marginLeft: 20,
  },
  enumSelectMenu: {
    margin: "var(--pf-global--spacer--form-element) 0",
    width: "40%",
  },
});

export interface IExpressionEnumSelectProps {
  selectedNodeId: string;
  enumCandidates: EnumValue[];
  clearEnumSelect: () => void;
  onEnumSelect: (selectedNodeId: string, selectedIndex: number) => void;
}
let selectValue = "";

export const ExpressionEnumSelect: FunctionComponent<IExpressionEnumSelectProps> = ({
  selectedNodeId,
  enumCandidates,
  clearEnumSelect,
  onEnumSelect,
}) => {
  const id = `expr-enum-select-${selectValue}`;
  const { state, toggle, toggleOff } = useToggle(true, onToggleEnumSelect);

  function onToggleEnumSelect(toggled: boolean): any {
    if (!toggled) {
      enumCandidates = [];
      clearEnumSelect();
    }
  }

  function selectionChanged(
    event: any,
    value: string | SelectOptionObject,
    _isPlaceholder?: boolean | undefined,
  ): void {
    selectValue = value as string;
    onEnumSelect(selectedNodeId, event.currentTarget.id.split("-").pop());
    onToggleEnumSelect(false);
    toggleOff();
  }

  function createSelectOption(selectField: string, idx: number): any {
    if (selectField[1].length === 0) {
      return (
        <SelectOption
          isDisabled={true}
          label={selectField}
          value={selectField}
          key={idx}
          className={css(styles.document)}
        />
      );
    } else {
      return (
        <SelectOption
          label={selectField}
          value={selectField}
          key={idx}
          className={css(styles.field)}
        />
      );
    }
  }

  return (
    <div
      aria-label="Expression Enumeration"
      className={css(styles.enumSelectMenu)}
      data-testid={"expression-enumeration-select"}
    >
      <Select
        onToggle={toggle}
        isExpanded={state}
        value={selectValue}
        id={id}
        onSelect={selectionChanged}
        data-testid={id}
      >
        {enumCandidates.map((s, idx: number) =>
          createSelectOption(s.name, idx),
        )}
      </Select>
    </div>
  );
};
