import React, {
  FunctionComponent,
  ChangeEvent,
  useCallback,
  ReactElement,
} from "react";
import {
  Select,
  SelectOption,
  SelectProps,
  SelectGroup,
} from "@patternfly/react-core";
import { useToggle } from "./useToggle";

export interface IAddFieldTypeaheadField {
  label: string;
  group: string;
  onAdd: () => void;
}

export interface IAddFieldTypeaheadProps {
  fields: IAddFieldTypeaheadField[];
  ariaLabelTypeAhead: string;
  placeholderText: string;
}

export const AddFieldTypeahead: FunctionComponent<IAddFieldTypeaheadProps> = ({
  fields,
  ariaLabelTypeAhead,
  placeholderText,
  ...props
}) => {
  const { state, toggle, toggleOff } = useToggle(false);

  const renderOptions = (fields: IAddFieldTypeaheadField[]) => {
    const groups = fields.reduce<{ [group: string]: ReactElement[] }>(
      (groups, f) => {
        groups[f.group] = [
          ...(groups[f.group] || []),
          <SelectOption
            key={f.label}
            value={{
              ...f,
              toString: () => f.label,
              compareTo: (c) =>
                f.label.localeCompare((c as IAddFieldTypeaheadField).label) ===
                0,
            }}
            data-testid={`add-field-option-${f.label}`}
          >
            {f.label}
          </SelectOption>,
        ];
        return groups;
      },
      {},
    );
    return Object.entries<ReactElement[]>(groups).map(
      ([groupName, elements]) => (
        <SelectGroup label={groupName} key={groupName}>
          {elements}
        </SelectGroup>
      ),
    );
  };

  const filterFields = useCallback(
    (e: ChangeEvent<HTMLInputElement>) => {
      try {
        const searchValueRX = new RegExp(e.target.value, "i");
        return renderOptions(fields.filter((f) => searchValueRX.test(f.label)));
      } catch (err) {}
      return renderOptions(fields);
    },
    [fields],
  );

  const onSelect: SelectProps["onSelect"] = useCallback(
    (_e, f) => {
      (f as IAddFieldTypeaheadField).onAdd();
      toggleOff();
    },
    [toggleOff],
  );

  return (
    <div {...props}>
      <Select
        variant={"typeahead"}
        ariaLabelTypeAhead={ariaLabelTypeAhead}
        onToggle={toggle}
        isExpanded={state}
        placeholderText={placeholderText}
        onFilter={filterFields}
        onSelect={onSelect}
        maxHeight={300}
      >
        {renderOptions(fields)}
      </Select>
    </div>
  );
};
