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
  executeFieldSearch: (searchFilter: string, isSource: boolean) => any;
  mappingExpressionAddField: (
    selectedField: any,
    newTextNode: any,
    atIndex: number,
    isTrailer: boolean
  ) => void;
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
    offset?: number,
    removeNext?: boolean
  ) => void;
  onConditionalMappingExpressionEnabled: () => boolean;
  onGetMappingExpression: () => string;
  onToggleExpressionMode: () => void;
  trailerId: string;
}

export const CanvasViewToolbar: FunctionComponent<ICanvasViewToolbarProps> = ({
  executeFieldSearch,
  mappingExpressionAddField,
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
              // TODO - factor expression enabled
              setCondExprEnabled(onConditionalMappingExpressionEnabled());
            }}
            disabled={false}
            data-testid={'enable-disable-conditional-mapping-expression-button'}
          >
            <i>
              f<sub>(x)</sub>
            </i>
          </Button>
        </ToolbarItem>
        <ToolbarItem className={css(styles.toolbarItem)}>
          <ConditionalExpression
            executeFieldSearch={executeFieldSearch}
            mappingExpressionAddField={mappingExpressionAddField}
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
