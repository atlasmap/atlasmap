import React, { FunctionComponent } from 'react';
import { ExpressionContent } from './ExpressionContent';

export interface IConditionalExpressionProps {
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
  onGetMappingExpression: () => string;
  trailerId: string;
}

export const ConditionalExpression: FunctionComponent<IConditionalExpressionProps> = ({
  executeFieldSearch,
  mappingExpressionAddField,
  mappingExpressionClearText,
  mappingExpressionEmpty,
  mappingExpressionInit,
  mappingExpressionInsertText,
  mappingExpressionObservable,
  mappingExpressionRemoveField,
  onGetMappingExpression,
  trailerId,
}) => {
  return (
    <ExpressionContent
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
  );
};
