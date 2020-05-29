import React, { FunctionComponent } from "react";
import { ToolbarGroup, ToolbarItem } from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";
import {
  ExpressionContent,
  IExpressionContentProps,
} from "./ExpressionContent";

const styles = StyleSheet.create({
  toolbarItem: { flex: 1 },
});

export interface IConditionalExpressionInputProps
  extends IExpressionContentProps {}

export const ConditionalExpressionInput: FunctionComponent<IConditionalExpressionInputProps> = ({
  executeFieldSearch,
  mappingExpressionAddField,
  mappingExpressionClearText,
  isMappingExpressionEmpty,
  mappingExpressionInit,
  mappingExpressionInsertText,
  mappingExpressionObservable,
  mappingExpressionRemoveField,
  mappingExpression,
  trailerId,
  disabled,
  onToggle,
}) => {
  return (
    <ToolbarGroup className={css(styles.toolbarItem)} role={"form"}>
      <ToolbarItem className={css(styles.toolbarItem)}>
        <ExpressionContent
          disabled={disabled}
          executeFieldSearch={executeFieldSearch}
          mappingExpressionAddField={mappingExpressionAddField}
          mappingExpressionClearText={mappingExpressionClearText}
          isMappingExpressionEmpty={isMappingExpressionEmpty}
          mappingExpressionInit={mappingExpressionInit}
          mappingExpressionInsertText={mappingExpressionInsertText}
          mappingExpressionObservable={mappingExpressionObservable}
          mappingExpressionRemoveField={mappingExpressionRemoveField}
          mappingExpression={mappingExpression}
          trailerId={trailerId}
          onToggle={onToggle}
        />
      </ToolbarItem>
    </ToolbarGroup>
  );
};
