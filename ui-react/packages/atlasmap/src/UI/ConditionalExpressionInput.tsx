import React, { FunctionComponent, useState } from "react";
import {
  ToolbarGroup,
  ToolbarItem,
  Button,
  Tooltip,
} from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";
import {
  ExpressionContent,
  IExpressionContentProps,
} from "./ExpressionContent";

const styles = StyleSheet.create({
  toolbarItem: { flex: 1 },
});

export const ConditionalExpressionInput: FunctionComponent<IExpressionContentProps> = ({
  executeFieldSearch,
  mappingExpressionAddField,
  mappingExpressionClearText,
  isMappingExpressionEmpty,
  mappingExpressionInit,
  mappingExpressionInsertText,
  mappingExpressionObservable,
  mappingExpressionRemoveField,
  mappingExpression,
  onConditionalMappingExpressionEnabled,
  onToggleExpressionMode,
  trailerId,
}) => {
  const [, setCondExprEnabled] = useState<boolean>();
  function onToggle() {
    onToggleExpressionMode();
    setCondExprEnabled(onConditionalMappingExpressionEnabled());
  }
  return (
    <ToolbarGroup className={css(styles.toolbarItem)} role={"form"}>
      <ToolbarItem>
        <Tooltip
          content={"Enable/ Disable conditional mapping expression."}
          enableFlip={true}
          entryDelay={1000}
          position={"left"}
        >
          <Button
            variant={"plain"}
            aria-label="Enable/ Disable conditional mapping expression"
            tabIndex={-1}
            onClick={onToggle}
            disabled={!onConditionalMappingExpressionEnabled()}
            data-testid={"enable-disable-conditional-mapping-expression-button"}
          >
            <i>
              f<sub>(x)</sub>
            </i>
          </Button>
        </Tooltip>
      </ToolbarItem>
      <ToolbarItem className={css(styles.toolbarItem)}>
        {onConditionalMappingExpressionEnabled() && (
          <ExpressionContent
            executeFieldSearch={executeFieldSearch}
            mappingExpressionAddField={mappingExpressionAddField}
            mappingExpressionClearText={mappingExpressionClearText}
            isMappingExpressionEmpty={isMappingExpressionEmpty}
            mappingExpressionInit={mappingExpressionInit}
            mappingExpressionInsertText={mappingExpressionInsertText}
            mappingExpressionObservable={mappingExpressionObservable}
            mappingExpressionRemoveField={mappingExpressionRemoveField}
            mappingExpression={mappingExpression}
            onConditionalMappingExpressionEnabled={
              onConditionalMappingExpressionEnabled
            }
            onToggleExpressionMode={onToggleExpressionMode}
            trailerId={trailerId}
          />
        )}
      </ToolbarItem>
    </ToolbarGroup>
  );
};
