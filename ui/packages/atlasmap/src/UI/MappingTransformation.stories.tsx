import React from "react";

import { boolean } from "@storybook/addon-knobs";
import { action } from "@storybook/addon-actions";

import { MappingTransformation } from "./MappingTransformation";

export default {
  title: "UI|Mapping Details/Transformation",
};

const transformationsOptions = [
  { name: "Transformation foo", value: "foo" },
  { name: "Transformation bar", value: "bar" },
];

const transformationsArguments = [
  {
    label: "Argument foo",
    name: "foo",
    value: "foo",
  },
  {
    label: "Argument bar",
    name: "bar",
    value: "bar",
  },
  {
    label: "Delimiter",
    name: "baz",
    value: "baz",
    delimitersOptions: [
      {
        displayName: ",",
        delimiterValue: ",",
      },
      {
        displayName: "|",
        delimiterValue: "|",
      },
    ],
    delimiter: ",",
  },
];

export const example = () => (
  <MappingTransformation
    name={"Sample transformation"}
    transformationsOptions={transformationsOptions}
    transformationsArguments={transformationsArguments}
    noPaddings={boolean("Hide paddings", true)}
    onTransformationArgumentChange={action("onActionArgumentChange")}
    onTransformationChange={action("onActionChange")}
    onRemoveTransformation={action("onRemoveTransformation")}
    mappingExpressionEnabled={false}
  />
);

export const nonRemovable = () => (
  <MappingTransformation
    name={"Sample transformation"}
    transformationsOptions={transformationsOptions}
    transformationsArguments={transformationsArguments}
    noPaddings={boolean("Hide paddings", true)}
    onTransformationArgumentChange={action("onActionArgumentChange")}
    onTransformationChange={action("onActionChange")}
    mappingExpressionEnabled={false}
  />
);
