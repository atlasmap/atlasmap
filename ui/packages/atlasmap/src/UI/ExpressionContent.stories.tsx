import React from "react";

import { boolean, text } from "@storybook/addon-knobs";
import { action } from "@storybook/addon-actions";

import { ExpressionContent } from "./ExpressionContent";
import { EnumValue } from "../../src/Atlasmap/utils";

export default {
  title: "UI|Mapping expression",
};

const executeFieldSearch = (): string[][] => {
  return [
    ["foo", "Foo"],
    ["bar", "Bar"],
    ["baz", "Baz"],
  ];
};

const getFieldEnums = (): EnumValue[] => {
  return [
    { name: "[ None ]", ordinal: 0 },
    { name: "rat", ordinal: 1 },
    { name: "cat", ordinal: 2 },
    { name: "bat", ordinal: 3 },
  ];
};

const setSelectedEnumValue = (): void => {};

export const disabled = () => (
  <ExpressionContent
    executeFieldSearch={executeFieldSearch}
    getFieldEnums={getFieldEnums}
    mappingExpressionAddField={action("mappingExpressionAddField")}
    mappingExpressionClearText={() => ({ str: "", uuid: "123" })}
    isMappingExpressionEmpty={boolean("isMappingExpressionEmpty", true)}
    mappingExpressionInit={action("mappingExpressionInit")}
    mappingExpressionInsertText={action("mappingExpressionInsertText")}
    mappingExpressionObservable={() => null}
    mappingExpressionRemoveField={action("mappingExpressionRemoveField")}
    mappingExpression={text("Mapping expression", "")}
    trailerId={text("Trailer id", "expression-trailer")}
    disabled={boolean("Disabled", true)}
    onToggle={action("onToggle")}
    setSelectedEnumValue={setSelectedEnumValue}
  />
);

export const enabledWithExpression = () => (
  <ExpressionContent
    executeFieldSearch={executeFieldSearch}
    getFieldEnums={getFieldEnums}
    mappingExpressionAddField={action("mappingExpressionAddField")}
    mappingExpressionClearText={() => ({ str: "", uuid: "123" })}
    isMappingExpressionEmpty={boolean("isMappingExpressionEmpty", true)}
    mappingExpressionInit={action("mappingExpressionInit")}
    mappingExpressionInsertText={action("mappingExpressionInsertText")}
    mappingExpressionObservable={() => null}
    mappingExpressionRemoveField={action("mappingExpressionRemoveField")}
    mappingExpression={text(
      "Mapping expression",
      "if (prop-city== Boston, city, state)",
    )}
    trailerId={text("Trailer id", "expression-trailer")}
    disabled={boolean("Disabled", false)}
    onToggle={action("onToggle")}
    setSelectedEnumValue={setSelectedEnumValue}
  />
);

export const enabledWithoutExpression = () => (
  <ExpressionContent
    executeFieldSearch={executeFieldSearch}
    getFieldEnums={getFieldEnums}
    mappingExpressionAddField={action("mappingExpressionAddField")}
    mappingExpressionClearText={() => ({ str: "", uuid: "123" })}
    isMappingExpressionEmpty={boolean("isMappingExpressionEmpty", true)}
    mappingExpressionInit={action("mappingExpressionInit")}
    mappingExpressionInsertText={action("mappingExpressionInsertText")}
    mappingExpressionObservable={() => null}
    mappingExpressionRemoveField={action("mappingExpressionRemoveField")}
    mappingExpression={text("Mapping expression", "")}
    trailerId={text("Trailer id", "expression-trailer")}
    disabled={boolean("Disabled", false)}
    onToggle={action("onToggle")}
    setSelectedEnumValue={setSelectedEnumValue}
  />
);
