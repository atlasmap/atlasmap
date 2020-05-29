import React from "react";

import { boolean, text } from "@storybook/addon-knobs";
import { action } from "@storybook/addon-actions";

import { ExpressionContent } from "./ExpressionContent";

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

export const disabled = () => (
  <ExpressionContent
    executeFieldSearch={executeFieldSearch}
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
  />
);

export const enabledWithExpression = () => (
  <ExpressionContent
    executeFieldSearch={executeFieldSearch}
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
  />
);

export const enabledWithoutExpression = () => (
  <ExpressionContent
    executeFieldSearch={executeFieldSearch}
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
  />
);
