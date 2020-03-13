import React, { FunctionComponent } from 'react';
import { TextInput } from '@patternfly/react-core';

export interface IConditionalExpressionProps {
  expressionTokens: string[];
  onConditionalMappingExpressionEnabled: () => boolean;
  onGetMappingExpressionStr: () => string;
}

export const ConditionalExpression: FunctionComponent<IConditionalExpressionProps> = ({
  // expressionTokens,
  onConditionalMappingExpressionEnabled,
  onGetMappingExpressionStr,
}) => {
  return (
    <TextInput
      aria-label={'Conditional mapping expression'}
      // value={expressionTokens[0]}
      value={onGetMappingExpressionStr()}
      isDisabled={!onConditionalMappingExpressionEnabled()}
      // onChange={onExpressionChange}
    />
  );
};
