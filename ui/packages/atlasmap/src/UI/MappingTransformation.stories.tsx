import { Form } from "@patternfly/react-core";
import { MappingTransformation } from "./MappingTransformation";
import React from "react";
import { action } from "@storybook/addon-actions";
import { boolean } from "@storybook/addon-knobs";

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
    onTransformationArgumentChange={action("onActionArgumentChange")}
    onTransformationChange={action("onActionChange")}
    onRemoveTransformation={action("onRemoveTransformation")}
    disableTransformation={boolean("Expression not enabled", false)}
  />
);

export const nonRemovable = () => (
  <MappingTransformation
    name={"Sample transformation"}
    transformationsOptions={transformationsOptions}
    transformationsArguments={transformationsArguments}
    onTransformationArgumentChange={action("onActionArgumentChange")}
    onTransformationChange={action("onActionChange")}
    disableTransformation={boolean("Expression not enabled", false)}
  />
);

export const stacked = () => (
  <Form>
    <MappingTransformation
      name={"Sample transformation"}
      transformationsOptions={transformationsOptions}
      transformationsArguments={transformationsArguments}
      onTransformationArgumentChange={action("onActionArgumentChange")}
      onTransformationChange={action("onActionChange")}
      onRemoveTransformation={action("onRemoveTransformation")}
      disableTransformation={boolean("Expression not enabled", false)}
    />
    <MappingTransformation
      name={"Sample transformation"}
      transformationsOptions={transformationsOptions}
      transformationsArguments={transformationsArguments}
      onTransformationArgumentChange={action("onActionArgumentChange")}
      onTransformationChange={action("onActionChange")}
      onRemoveTransformation={action("onRemoveTransformation")}
      disableTransformation={boolean("Expression not enabled", false)}
    />
    <MappingTransformation
      name={"Sample transformation"}
      transformationsOptions={transformationsOptions}
      transformationsArguments={transformationsArguments}
      onTransformationArgumentChange={action("onActionArgumentChange")}
      onTransformationChange={action("onActionChange")}
      onRemoveTransformation={action("onRemoveTransformation")}
      disableTransformation={boolean("Expression not enabled", false)}
    />
  </Form>
);
