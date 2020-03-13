import React, { FunctionComponent } from 'react';
import {
  Button,
  Toolbar,
  ToolbarGroup,
  ToolbarItem,
} from '@patternfly/react-core';
import { css, StyleSheet } from '@patternfly/react-styles';
import { ConditionalExpression } from './ConditionalExpression';

const styles = StyleSheet.create({
  toolbar: { borderBottom: '1px solid #ccc' },
  toolbarItem: { flex: 1 },
});

export interface ICanvasViewToolbarProps {
  expressionTokens: string[];
  onConditionalMappingExpressionEnabled: () => boolean;
  onGetMappingExpressionStr: () => string;
  onToggleExpressionMode: () => void;
}

export const CanvasViewToolbar: FunctionComponent<ICanvasViewToolbarProps> = ({
  expressionTokens,
  onConditionalMappingExpressionEnabled,
  onGetMappingExpressionStr,
  onToggleExpressionMode,
}) => {
  return (
    <Toolbar
      className={css('view-toolbar pf-u-px-md pf-u-py-md', styles.toolbar)}
    >
      <ToolbarGroup className={css(styles.toolbarItem)}>
        <ToolbarItem>
          <Button
            variant={'plain'}
            aria-label="Enable/ Disable conditional mapping expression"
            onClick={onToggleExpressionMode}
            disabled={!onConditionalMappingExpressionEnabled()}
          >
            <i>
              f<sub>(x)</sub>
            </i>
          </Button>
        </ToolbarItem>
        {onConditionalMappingExpressionEnabled() && (
          <ToolbarItem className={css(styles.toolbarItem)}>
            <ConditionalExpression
              expressionTokens={expressionTokens}
              onConditionalMappingExpressionEnabled={
                onConditionalMappingExpressionEnabled
              }
              onGetMappingExpressionStr={onGetMappingExpressionStr}
            />
          </ToolbarItem>
        )}
      </ToolbarGroup>
    </Toolbar>
  );
};
