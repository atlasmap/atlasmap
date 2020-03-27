import React, { FunctionComponent, useState } from 'react';
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
  mappingExpressionClearText: (
    nodeId?: string,
    startOffset?: number,
    endOffset?: number
  ) => any;
  mappingExpressionEmpty: () => boolean;
  mappingExpressionInit: () => void;
  mappingExpressionInsertText: (
    str: string,
    nodeId?: string,
    offset?: number
  ) => void;
  mappingExpressionObservable: () => any;
  mappingExpressionRemoveField: (
    tokenPosition?: string,
    offset?: number
  ) => void;
  onConditionalMappingExpressionEnabled: () => boolean;
  onGetMappingExpression: () => string;
  onToggleExpressionMode: () => void;
  trailerId: string;
}

export const CanvasViewToolbar: FunctionComponent<ICanvasViewToolbarProps> = ({
  mappingExpressionClearText,
  mappingExpressionEmpty,
  mappingExpressionInit,
  mappingExpressionInsertText,
  mappingExpressionObservable,
  mappingExpressionRemoveField,
  onConditionalMappingExpressionEnabled,
  onGetMappingExpression,
  onToggleExpressionMode,
  trailerId,
}) => {
  const [, setCondExprEnabled] = useState<boolean>();

  return (
    <Toolbar
      className={css('view-toolbar pf-u-px-md pf-u-py-md', styles.toolbar)}
    >
      <ToolbarGroup className={css(styles.toolbarItem)}>
        <ToolbarItem>
          <Button
            variant={'plain'}
            aria-label="Enable/ Disable conditional mapping expression"
            onClick={() => {
              onToggleExpressionMode();
              setCondExprEnabled(onConditionalMappingExpressionEnabled());
            }}
            disabled={false}
          >
            <i>
              f<sub>(x)</sub>
            </i>
          </Button>
        </ToolbarItem>
        <ToolbarItem className={css(styles.toolbarItem)}>
          <ConditionalExpression
            mappingExpressionClearText={mappingExpressionClearText}
            mappingExpressionEmpty={mappingExpressionEmpty}
            mappingExpressionInit={mappingExpressionInit}
            mappingExpressionInsertText={mappingExpressionInsertText}
            mappingExpressionObservable={mappingExpressionObservable}
            mappingExpressionRemoveField={mappingExpressionRemoveField}
            onGetMappingExpression={onGetMappingExpression}
            trailerId={trailerId}
          />
        </ToolbarItem>
      </ToolbarGroup>
    </Toolbar>
  );
};
