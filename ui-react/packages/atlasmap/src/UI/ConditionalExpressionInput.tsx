import React, { FunctionComponent } from "react";
import {
  ToolbarGroup,
  ToolbarItem,
  Button,
  Tooltip,
} from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";
import { ExpressionContent } from "./ExpressionContent";

const styles = StyleSheet.create({
  toolbarItem: { flex: 1 },
});
export interface IConditionalExpressionInputProps {
  executeFieldSearch: (searchFilter: string, isSource: boolean) => string[][];
  mappingExpressionAddField: (
    selectedField: string,
    newTextNode: any,
    atIndex: number,
    isTrailer: boolean,
  ) => void;
  mappingExpressionClearText: (
    nodeId?: string,
    startOffset?: number,
    endOffset?: number,
  ) => any;
  isMappingExpressionEmpty: boolean;
  mappingExpressionInit: () => void;
  mappingExpressionInsertText: (
    str: string,
    nodeId?: string | undefined,
    offset?: number | undefined,
  ) => void;
  mappingExpressionObservable: () => any;
  mappingExpressionRemoveField: (
    tokenPosition?: string,
    offset?: number,
    removeNext?: boolean,
  ) => void;
  onConditionalMappingExpressionEnabled: () => boolean;
  onToggleExpressionMode: () => void;
  mappingExpression?: string;
  trailerId: string;
}

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
  onConditionalMappingExpressionEnabled,
  onToggleExpressionMode,
  trailerId,
}) => {
  function onToggle() {
    onToggleExpressionMode();
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
            onToggleExpressionMode={onToggleExpressionMode}
            trailerId={trailerId}
          />
        )}
      </ToolbarItem>
    </ToolbarGroup>
  );
};
