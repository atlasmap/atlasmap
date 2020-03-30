import React, { FunctionComponent } from 'react';
import { ExpressionContent } from './ExpressionContent';

export interface IConditionalExpressionProps {
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
  onGetMappingExpression: () => string;
  trailerId: string;
}

export const ConditionalExpression: FunctionComponent<IConditionalExpressionProps> = ({
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
