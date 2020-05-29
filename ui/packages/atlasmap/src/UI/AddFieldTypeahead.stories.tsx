import React from "react";

import { action } from "@storybook/addon-actions";

import {
  AddFieldTypeahead,
  IAddFieldTypeaheadField,
} from "./AddFieldTypeahead";

export default {
  title: "UI|Mapping Details/Add field typeahead",
};

const onAddAction = action("onAdd");

const fields: IAddFieldTypeaheadField[] = [
  { group: "Foo group", label: "Aaa", onAdd: onAddAction },
  { group: "Foo group", label: "Bbbb", onAdd: onAddAction },
  { group: "Foo group", label: "Cccc", onAdd: onAddAction },
  { group: "Bar group", label: "Aaa", onAdd: onAddAction },
  { group: "Bar group", label: "Bbbb", onAdd: onAddAction },
  { group: "Bar group", label: "Cccc", onAdd: onAddAction },
  { group: "Baz group", label: "Aaa", onAdd: onAddAction },
  { group: "Baz group", label: "Bbbb", onAdd: onAddAction },
  { group: "Baz group", label: "Cccc", onAdd: onAddAction },
  {
    group: "Stretch group",
    label:
      "Lorem ipsum dolor sit amet consectetur adipisicing elit. Reprehenderit expedita animi facere optio sit eaque, aut numquam, asperiores ratione earum natus iste in. Nemo cupiditate praesentium, amet accusamus impedit iure.",
    onAdd: onAddAction,
  },
  {
    group: "Stretch group",
    label:
      "Lorem ipsum dolor sit amet consectetur adipisicing elit. Reprehenderit expedita animi facere optio sit eaque, aut numquam, asperiores ratione earum natus iste in. Nemo cupiditate praesentium, amet accusamus impedit iure.",
    onAdd: onAddAction,
  },
  {
    group: "Stretch group",
    label:
      "Lorem ipsum dolor sit amet consectetur adipisicing elit. Reprehenderit expedita animi facere optio sit eaque, aut numquam, asperiores ratione earum natus iste in. Nemo cupiditate praesentium, amet accusamus impedit iure.",
    onAdd: onAddAction,
  },
];

export const example = () => (
  <AddFieldTypeahead
    ariaLabelTypeAhead={"example"}
    fields={fields}
    placeholderText={"Placeholder"}
  />
);
